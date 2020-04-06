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

package com.sanda.truckdoc.client.wizard.wizardpager.ui;

import android.app.Activity;
import android.widget.TextView;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.wizard.wizardpager.model.ImagesChoicePage;
import com.sanda.truckdoc.client.wizard.wizardpager.model.Page;

import net.tribe7.common.collect.Lists;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OnActivityResult.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.IntegerRes;

import java.io.File;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static net.tribe7.common.collect.FluentIterable.from;

@EFragment(R.layout.fragment_page_photos)
public class ImagesFragment extends Fragment implements ImagesAdapter.AddImageClickListener {

    public static final int REQUEST_CODE = 17;
    private PageFragmentCallbacks mCallbacks;
    private List<String> mChoices;
    private ImagesChoicePage mPage;
    private ImagesAdapter adapter;

    @FragmentArg
    String key;
    @ViewById(android.R.id.title)
    TextView title;
    @ViewById(android.R.id.list)
    RecyclerView recyclerView;
    @IntegerRes
    int wizard_grid_cells_count;

    @AfterInject
    void afterInject() {
        mPage = (ImagesChoicePage) mCallbacks.onGetPage(key);
        if (mPage.isFixed()) {
            adapter = new FixedImagesAdapter(this);
        } else {
            adapter = new ImagesAdapter(this);
        }

//        mChoices = new ArrayList<>();
//        for (int i = 0; i < fixedChoicePage.getOptionCount(); i++) {
//            mChoices.add(fixedChoicePage.getOptionAt(i));
//        }
    }

    @AfterViews
    void afterViews() {
        title.setText(mPage.getTitle());

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), wizard_grid_cells_count);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        String[] files = mPage.getData().getStringArray(Page.SIMPLE_DATA_KEY);
        for (File file : from(Lists.newArrayList(files != null ? files : new String[0])).transform(File::new)) {
            adapter.add(file);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onAddImageClicked() {
        CameraActivity_.intent(this).description(mPage.getTitle()).startForResult(REQUEST_CODE);
    }

    @OnActivityResult(REQUEST_CODE)
    void onResult(int resultCode, @Extra(CameraActivity.IMAGE) File imagePath) {
        if (resultCode == Activity.RESULT_OK) {
            adapter.add(imagePath);
            String[] list = from(adapter.getItems()).transform(File::getAbsolutePath).toArray(String.class);
            mPage.getData().putStringArray(Page.SIMPLE_DATA_KEY, list);
            mPage.notifyDataChanged();
        }
    }
}
