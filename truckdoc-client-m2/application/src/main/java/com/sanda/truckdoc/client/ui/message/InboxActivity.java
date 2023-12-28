package com.sanda.truckdoc.client.ui.message;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.sanda.truckdoc.client.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import static com.sanda.truckdoc.client.R.id.pager;

/**
 * Created by astra on 09.06.2015.
 */
public class InboxActivity extends AppCompatActivity {
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        Toolbar toolbar = findViewById(R.id.toolbar);

        TabLayout tabLayout = findViewById(R.id.tabLayout);


        tabLayout.setTabTextColors(ResourcesCompat.getColorStateList(
                this.getResources(),
                R.color.dark_tab_color,
                null));
        viewPager = findViewById(pager);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        viewPager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Drawable email = ResourcesCompat.getDrawable(this.getResources(),
                    R.drawable.ic_email_white_36dp,
                    null);
            Drawable create = ResourcesCompat.getDrawable(this.getResources(),
                    R.drawable.ic_create_white_36dp,
                    null);
            Drawable wrapped = DrawableCompat.wrap(email);
            Drawable wrapped2 = DrawableCompat.wrap(create);
            DrawableCompat.setTintList(wrapped, tabLayout.getTabTextColors());//getResources().getColorStateList(R.color.tab_color));
            DrawableCompat.setTintList(wrapped2, tabLayout.getTabTextColors());//getResources().getColorStateList(R.color.tab_color));
            tabLayout.getTabAt(0).setIcon(wrapped);
            tabLayout.getTabAt(1).setIcon(wrapped2);
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return InboxFragment_.builder().build();
                case 1:
                default:
                    return NewMessageFragment_.builder().build();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.messages);
                case 1:
                default:
                    return getString(R.string.write_new);
            }
        }
    }

    public void setCurrentTab(int number, Bundle args) {
        ViewPager viewPager = findViewById(pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment newFragment = new NewMessageFragment();
        newFragment.setArguments(args);
        transaction.replace(R.id.new_message_fragment, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        viewPager.setCurrentItem(number);

    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPager.setCurrentItem(1);

        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(0);
            }
        }, 100);
    }


}
