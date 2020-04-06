package com.sanda.truckdoc.client.to;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistResultNode;
import com.sanda.truckdoc.client.to.adapters.MntItemsAdapter;
import com.sanda.truckdoc.client.to.data.Model;
import com.sanda.truckdoc.client.to.data.ToNode;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by k.natallie on 28.01.2016.
 */
public class MaintenanceFragment extends Fragment implements MntItemsAdapter.OnItemSelected {

    private Model model;
    private List<? extends ChecklistResultNode> configNodes;
    private ToNode parentNode;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MntItemsAdapter adapter;

    public MaintenanceFragment() {

    }

    public static MaintenanceFragment newInstance(ToNode node) {
        MaintenanceFragment fragment = new MaintenanceFragment();

        // arguments
        Bundle arguments = new Bundle();
        arguments.putSerializable("TO_NODE", node);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        parentNode = (ToNode) arguments.getSerializable("TO_NODE");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        model = Model.getInstance(activity.getApplicationContext());

        configNodes = model.getNodes();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_to_select_main_part, container, false);
        //set the properties for button

        recyclerView = view.findViewById(R.id.to_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        updateAdapter();
        recyclerView.setAdapter(adapter);

        return view;

    }

    private void updateAdapter() {
        List<? extends ChecklistResultNode> list = model.getListOfNodesWithAllChildren(model.getNodes(), parentNode);
        adapter = new MntItemsAdapter(list, this, getActivity().getApplicationContext(), false);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


    @Override
    public void onItemSelected(int position) {
        recyclerView.scrollToPosition(position);
        //   this.position = position;
    }

}
