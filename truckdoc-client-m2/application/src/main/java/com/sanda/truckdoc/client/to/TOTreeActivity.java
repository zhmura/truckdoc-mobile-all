package com.sanda.truckdoc.client.to;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistResult;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistResultNode;
import com.sanda.truckdoc.client.to.data.Model;
import com.sanda.truckdoc.client.to.data.ToNode;
import com.sanda.truckdoc.client.to.service.NewMntService;
import com.sanda.truckdoc.client.ui.DashboardActivity;
import com.sanda.truckdoc.client.util.NotificationHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by k.natallie on 28.01.2016.
 */
public class TOTreeActivity extends BaseActivity {

    private FloatingActionButton sendTo;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to);
        viewPager = findViewById(R.id.viewpager);

        sendTo = findViewById(R.id.fab);
        sendTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model.isFullFilled()) {
                    NotificationHelper.showNotificationMessage(getResources().getString(R.string.mnt_sending_start), getApplicationContext());
                    sendTO();
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(getApplicationContext(), UntrackedToItemsActivity.class));

                }
            }
        });
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void sendTO() {
        ChecklistResult result = model.getResult();
        if (result != null) {
            if (result.getCompletionDate() == null) {
                model.getResult().setCompletionDate(Calendar.getInstance().getTimeInMillis());
                model.updateTOProgress();
            }
            Intent intent = NewMntService.intent(getApplicationContext());
            intent.setAction(NewMntService.ACTION_MESSAGE_SEND);
            startService(intent);
        } else {
            Log.e("error", "no result");
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        List<? extends ChecklistResultNode> configNodes = Model.getInstance(getApplicationContext()).getNodes();
        for (ChecklistResultNode node : configNodes) {
            adapter.addFragment(MaintenanceFragment.newInstance((ToNode) node), node.getName());
        }
        viewPager.setAdapter(adapter);
        sendTo.setEnabled(configNodes.size() != 0);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
