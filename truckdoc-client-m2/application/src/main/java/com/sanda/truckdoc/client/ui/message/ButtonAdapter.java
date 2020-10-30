package com.sanda.truckdoc.client.ui.message;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.naixx.BaseAdapter;
import com.github.naixx.BaseViewHolder;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.data.model.DbContactRecord;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.ViewHolder> {

    public ButtonAdapter(BaseAdapter.InteractionListener<DbContactRecord> listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_message_button_send, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private final BaseAdapter.InteractionListener<DbContactRecord> listener;

    class ViewHolder extends BaseViewHolder<DbContactRecord> {

        private final Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            button = (Button) itemView;
        }

        @Override
        public void bind(DbContactRecord item, int position) {
            Context context = itemView.getContext();
            Drawable wrap = context.getResources().getDrawable(R.drawable.service_button_selector);
            //noinspection ConstantConditions
            wrap.setColorFilter(item.getColor(), Mode.MULTIPLY);
            button.setBackgroundDrawable(wrap.mutate());
            button.setText(item.getLabel());
            button.setOnClickListener(v -> listener.onClick(item));
        }
    }
}
