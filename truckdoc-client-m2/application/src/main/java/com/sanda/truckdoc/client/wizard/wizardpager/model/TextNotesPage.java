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

import android.text.TextUtils;

import com.sanda.truckdoc.client.wizard.wizardpager.ui.TextNotesFragment_;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

/**
 * A page asking for a name and an email.
 */
public class TextNotesPage extends Page {

    public static final String TEXT_NOTES = "text";

    public TextNotesPage(ModelCallbacks callbacks, String key, String title) {
        super(callbacks, key, title);
    }

    @Override
    public Fragment createFragment() {
        return TextNotesFragment_.builder().key(getKey()).build();
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem(getTitle(), mData.getString(TEXT_NOTES), getKey(), true));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(TEXT_NOTES));
    }
}
