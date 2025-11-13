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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.databinding.FragmentPagePhotosBinding;
import com.sanda.truckdoc.client.wizard.wizardpager.model.ImagesChoicePage;
import com.sanda.truckdoc.client.wizard.wizardpager.model.Page;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.google.common.collect.FluentIterable.from;

public class ImagesFragment extends Fragment implements ImagesAdapter.AddImageClickListener {

    private static final String ARG_KEY = "key";
    public static final int REQUEST_CODE = 17;
    private PageFragmentCallbacks mCallbacks;
    private List<String> mChoices;
    private ImagesChoicePage mPage;
    private ImagesAdapter adapter;
    private FragmentPagePhotosBinding binding;

    public static ImagesFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);
        ImagesFragment fragment = new ImagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String key = getArguments().getString(ARG_KEY);
        mPage = (ImagesChoicePage) mCallbacks.onGetPage(key);
        if (mPage.isFixed()) {
            adapter = new FixedImagesAdapter(this);
        } else {
            adapter = new ImagesAdapter(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPagePhotosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        binding.getRoot().findViewById(android.R.id.title).setVisibility(View.GONE);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.wizard_grid_cells_count));
        binding.list.setLayoutManager(layoutManager);
        binding.list.setAdapter(adapter);

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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAddImageClicked() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        intent.putExtra(CameraActivity.DESCRIPTION, mPage.getTitle());
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            File imagePath = (File) data.getSerializableExtra(CameraActivity.IMAGE);
            if (imagePath != null) {
                adapter.add(imagePath);
                String[] list = from(adapter.getItems()).transform(File::getAbsolutePath).toArray(String.class);
                mPage.getData().putStringArray(Page.SIMPLE_DATA_KEY, list);
                mPage.notifyDataChanged();
            }
        }
    }
}
