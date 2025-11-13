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

package com.sanda.truckdoc.client.wizard.wizardpager.model;

import com.sanda.truckdoc.client.wizard.wizardpager.ui.ImagesFragment;

import java.util.ArrayList;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

/**
 * A page offering the user a number of mutually exclusive choices.
 */
public class ImagesChoicePage extends Page {

    private final int requiredCount;
    private boolean fixed = false;

    public ImagesChoicePage(ModelCallbacks callbacks, String key, String title, int requiredCount) {
        super(callbacks, key, title);
        this.requiredCount = requiredCount;
    }

    public ImagesChoicePage(ModelCallbacks callbacks, String key, String title) {
        this(callbacks, key, title, 1);
    }

    public ImagesChoicePage setFixed(boolean fixed) {
        this.fixed = fixed;
        return this;
    }

    public boolean isFixed() {
        return fixed;
    }

    @Override
    public Fragment createFragment() {
        ImagesFragment fragment = new ImagesFragment();
        Bundle args = new Bundle();
        args.putString("key", getKey());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem(getTitle(), mData.getStringArray(SIMPLE_DATA_KEY), getKey(), false));
    }

    @Override
    public boolean isCompleted() {
        String[] files = mData.getStringArray(SIMPLE_DATA_KEY);
        return files != null && files.length >= requiredCount;
    }
}
