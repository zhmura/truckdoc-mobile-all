/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sanda.truckdoc.client.wizard.wizardpager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.databinding.ActivityWizardBinding;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.util.BundleUtils;
import com.sanda.truckdoc.client.util.ConnectionUtils;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.commons.FileUtils;
import com.sanda.truckdoc.client.wizard.AccidentModel;
import com.sanda.truckdoc.client.wizard.wizardpager.model.AbstractWizardModel;
import com.sanda.truckdoc.client.wizard.wizardpager.model.ModelCallbacks;
import com.sanda.truckdoc.client.wizard.wizardpager.model.Page;
import com.sanda.truckdoc.client.wizard.wizardpager.model.ReviewItem;
import com.sanda.truckdoc.client.wizard.wizardpager.ui.PageFragmentCallbacks;
import com.sanda.truckdoc.client.wizard.wizardpager.ui.ReviewFragment;
import com.sanda.truckdoc.client.wizard.wizardpager.ui.StepPagerStrip;

import com.google.common.base.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.sanda.truckdoc.client.HiltEntryPoint;

public class WizardActivity extends AppCompatActivity implements PageFragmentCallbacks, ReviewFragment.Callbacks, ModelCallbacks {

    private ActivityWizardBinding binding;
    private MyPagerAdapter mPagerAdapter;
    private boolean mEditingAfterReview;
    private AbstractWizardModel mWizardModel;
    private boolean mConsumePageSelectedEvent;
    private List<Page> mCurrentPageSequence;
    private boolean forceFinishing = false;
    private Prefs prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        binding = ActivityWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Use Hilt entry point pattern
        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(this);
        prefs = entryPoint.prefs();
        mWizardModel = new AccidentModel(this);
        if (savedInstanceState != null) {
            mWizardModel.load(savedInstanceState.getBundle("model"));
        } else if (!Strings.isNullOrEmpty(prefs.accidentState())) {
            mWizardModel.load(BundleUtils.deserialize(prefs.accidentState()));
        }

        initModel();

        if (savedInstanceState == null && !Strings.isNullOrEmpty(prefs.accidentState())) {
            binding.pager.setCurrentItem(prefs.accidentSelectedPage());
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.nextButton.setOnClickListener(v -> onNextButton());
        binding.prevButton.setOnClickListener(v -> onPrevButton());
        binding.deleteButton.setOnClickListener(v -> onDeleteButton());
    }

    private void initModel() {
        mWizardModel.registerListener(this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        binding.pager.setAdapter(mPagerAdapter);
        binding.strip.setOnPageSelectedListener(position -> {
            position = Math.min(mPagerAdapter.getCount() - 1, position);
            if (binding.pager.getCurrentItem() != position) {
                binding.pager.setCurrentItem(position);
            }
        });

        binding.pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                binding.strip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });
        onPageTreeChanged();
        updateBottomBar();
    }

    private void onNextButton() {
        if (binding.pager.getCurrentItem() == mCurrentPageSequence.size()) {
            if (sendMessages()) {
                prefs.accidentState("");
                prefs.accidentSelectedPage(0);
                mWizardModel = new AccidentModel(this);
                forceFinishing = true;
                finish();
            } else {
                Toast.makeText(this, R.string.connection_failure, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mEditingAfterReview) {
                binding.pager.setCurrentItem(mPagerAdapter.getCount() - 1);
            } else {
                binding.pager.setCurrentItem(binding.pager.getCurrentItem() + 1);
            }
        }
    }

    private void onPrevButton() {
        binding.pager.setCurrentItem(binding.pager.getCurrentItem() - 1);
    }

    private void onDeleteButton() {
        mWizardModel = new AccidentModel(this);
        prefs.accidentState("");
        prefs.accidentSelectedPage(0);
        initModel();
        binding.strip.setCurrentPage(0);
        try {
            FileUtils.deleteDirectory(FileHelper.getOutcomeDirForAccidents(false));
        } catch (IOException e) {
            Toast.makeText(this, R.string.cant_delete_accidents_dir, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        binding.strip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 = review step
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void updateBottomBar() {
        int position = binding.pager.getCurrentItem();
        if (position == mCurrentPageSequence.size()) {
            binding.nextButton.setText(R.string.finish);
            binding.nextButton.setBackgroundResource(R.drawable.finish_background);
            binding.nextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
        } else {
            binding.nextButton.setText(mEditingAfterReview ? R.string.review : R.string.next);
            binding.nextButton.setBackgroundResource(R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            binding.nextButton.setTextAppearance(this, v.resourceId);
            binding.nextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
        }

        binding.prevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private boolean sendMessages() {
        if (!ConnectionUtils.checkIfHaveInternetConnection(WizardActivity.this)) {
            return false;
        }
        ArrayList<ReviewItem> reviewItems = new ArrayList<>();
        for (Page page : mWizardModel.getCurrentPageSequence()) {
            page.getReviewItems(reviewItems);
        }
        Collections.sort(reviewItems,
                (a, b) -> a.getWeight() > b.getWeight() ? +1 : a.getWeight() < b.getWeight() ? -1 : 0);
        StringBuilder reviewInText = new StringBuilder("Произошло ДТП. Ревью проишествия: \n");
        if (reviewItems.size() > 0) {
            for (ReviewItem reviewItem : reviewItems) {
                if (reviewItem.isBranch()) {
                    String value = String.valueOf(reviewItem.getDisplayValue());
                    if (TextUtils.isEmpty(value)) {
                        value = "(None)";
                    }
                    reviewInText.append(reviewItem.getTitle()).append(": ").append(value).append('\n');
                }
            }
        }
        //отправка файлов
        try {
            FileUtils.copyDirectory(FileHelper.getOutcomeDirForAccidents(true),
                    FileHelper.getOutcomeDirForAccidents(false));
            FileUtils.deleteDirectory(FileHelper.getOutcomeDirForAccidents(true));
        } catch (IOException e) {
            throw new RuntimeException("Can not move temp directory for accident files", e);
        }
        //отправка сообщения с ревью
        boolean connected = ConnectionUtils.checkIfHaveInternetConnection(WizardActivity.this);
        if (connected) {
            final Intent intent = new Intent(MessageCheckService.ACTION_SEND_TEXT_MESSAGE,
                    null,
                    WizardActivity.this,
                    MessageCheckService.class);
            Bundle b = new Bundle();
            b.putString("com/sanda/truckdoc/client/message", reviewInText.toString());
            b.putLong("mail.group", 1);
            intent.putExtras(b);
            startService(intent);
        }

        final Intent intent = new Intent(MessageCheckService.ACTION_SEND_TEXT_MESSAGE,
                null,
                WizardActivity.this,
                MessageCheckService.class);
        Bundle b = new Bundle();
        b.putString("com/sanda/truckdoc/client/message", reviewInText.toString());
        b.putLong("mail.group", 1);
        intent.putExtras(b);
        startService(intent);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
        binding = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!forceFinishing) {
            prefs.accidentState(BundleUtils.serialize(mWizardModel.save()));
            prefs.accidentSelectedPage(binding.pager.getCurrentItem());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                binding.pager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    @Override
    public void onPageDataChanged(Page page) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }

            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
            return Math.min(mCutOffPage + 1, mCurrentPageSequence.size() + 1);
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }
}
