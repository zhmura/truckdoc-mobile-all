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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents a single line item on the final review page.
 *
 * @see com.sanda.truckdoc.client.wizard.wizardpager.ui.ReviewFragment
 */
public class ReviewItem {

    public static final int DEFAULT_WEIGHT = 0;

    private final int mWeight;
    private final String mTitle;
    @NonNull
    private final Object mDisplayValue;
    private final String mPageKey;
    private final Boolean isForTextReview;

    public ReviewItem(String title, Object displayValue, String pageKey, boolean isForTextReview) {
        this(title, displayValue, pageKey, DEFAULT_WEIGHT, isForTextReview);
    }

    public ReviewItem(String title, @Nullable Object displayValue, String pageKey, int weight, boolean isForTextReview) {
        this.mTitle = title;
        this.mDisplayValue = displayValue == null ? "" : displayValue;
        this.mPageKey = pageKey;
        this.mWeight = weight;
        this.isForTextReview = isForTextReview;

    }

    @NonNull
    public Object getDisplayValue() {
        if (mDisplayValue instanceof String[]) {
            return ((Object[]) mDisplayValue).length;
        } else {
            return mDisplayValue;
        }
    }

    public String getPageKey() {
        return this.mPageKey;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public int getWeight() {
        return this.mWeight;
    }

    public Boolean isBranch() {
        return this.isForTextReview;
    }
}
