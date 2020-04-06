package com.sanda.truckdoc.client.ui.floating;

import android.view.View;
import android.view.ViewGroup;

import com.sanda.truckdoc.client.R;

import java.util.Arrays;

import androidx.viewpager.widget.PagerAdapter;

class WizardPagerAdapter extends PagerAdapter {

    private static final Integer[] PAGES = new Integer[]{R.id.apn_page1, R.id.apn_page2, R.id.apn_page3, //
            R.id.apn_page4, R.id.apn_page_restart, R.id.apn_page_confirmation};

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        return collection.findViewById(PAGES[position]);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    public int page(int resId) {
        return Arrays.asList(PAGES).indexOf(resId);
    }

    @Override
    public int getCount() {
        return PAGES.length;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
}
