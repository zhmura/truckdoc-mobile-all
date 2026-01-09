package com.sanda.truckdoc.client.ui.message;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.naixx.BaseViewHolder;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.data.model.DbContactRecord;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.ViewHolder> {

    public interface InteractionListener<T> {
        void onClick(T item);
    }

    private List<DbContactRecord> items = new ArrayList<>();
    private final InteractionListener<DbContactRecord> listener;

    public ButtonAdapter(InteractionListener<DbContactRecord> listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void swapItems(List<DbContactRecord> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_message_button_send, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getRecordId();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends BaseViewHolder<DbContactRecord> {

        private final Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            button = (Button) itemView;
        }

        @Override
        public void bind(DbContactRecord item) {
            Context context = itemView.getContext();
            Drawable wrap = ResourcesCompat.getDrawable(context.getResources(),
                    R.drawable.service_button_selector,
                    null);
            //noinspection ConstantConditions
            wrap.setColorFilter(item.getColor(), Mode.MULTIPLY);
            button.setBackground(wrap.mutate());
            button.setText(item.getLabel());
            button.setOnClickListener(v -> listener.onClick(item));
        }
    }
}
