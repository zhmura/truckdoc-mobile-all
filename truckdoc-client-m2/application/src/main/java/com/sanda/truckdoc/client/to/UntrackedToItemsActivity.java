package com.sanda.truckdoc.client.to;

import android.os.Bundle;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistResultNode;
import com.sanda.truckdoc.client.to.adapters.MntItemsAdapter;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by k.natallie on 16.02.2016.
 */
public class UntrackedToItemsActivity extends BaseActivity implements MntItemsAdapter.OnItemSelected {

    private RecyclerView untrackedItemsList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_untracked_to_items);
        untrackedItemsList = findViewById(R.id.items);
        untrackedItemsList.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        untrackedItemsList.setLayoutManager(layoutManager);
        List<? extends ChecklistResultNode> nodes = model.getListOfUnfinishedNodes(model.getNodes());
        untrackedItemsList.setAdapter(new MntItemsAdapter(nodes, this, getApplicationContext(), true));
        untrackedItemsList.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<? extends ChecklistResultNode> nodes = model.getListOfUnfinishedNodes(model.getNodes());

        if (nodes.size() == 0) {
            finish();
        }
        ((MntItemsAdapter) untrackedItemsList.getAdapter()).setDataset(nodes);
        untrackedItemsList.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void onItemSelected(int position) {
        untrackedItemsList.scrollToPosition(position);

    }
}
