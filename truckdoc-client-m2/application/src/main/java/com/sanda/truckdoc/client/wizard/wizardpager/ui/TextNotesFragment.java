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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.wizard.wizardpager.model.TextNotesPage;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import androidx.fragment.app.Fragment;

@EFragment(R.layout.fragment_page_text_notes)
public class TextNotesFragment extends Fragment {

    private PageFragmentCallbacks mCallbacks;
    private TextNotesPage mPage;

    @FragmentArg
    String key;
    @ViewById(android.R.id.title)
    TextView title;
    @ViewById(R.id.text_notes)
    TextView textNotes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = (TextNotesPage) mCallbacks.onGetPage(key);
    }

    @AfterViews
    void afterViews() {
        title.setText(mPage.getTitle());

        textNotes.setText(mPage.getData().getString(TextNotesPage.TEXT_NOTES));
        textNotes.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                onTextNotesChanged(s);
            }
        });
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

    void onTextNotesChanged(Editable editable) {
        mPage.getData().putString(TextNotesPage.TEXT_NOTES, (editable != null) ? editable.toString() : null);
        mPage.notifyDataChanged();
    }
}
