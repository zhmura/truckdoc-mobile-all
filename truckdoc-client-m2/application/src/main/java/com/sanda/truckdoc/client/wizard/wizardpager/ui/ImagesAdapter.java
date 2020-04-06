package com.sanda.truckdoc.client.wizard.wizardpager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.ui.utils.BaseViewHolder;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class ImagesAdapter extends RecyclerView.Adapter<BaseViewHolder<File>> {

    interface AddImageClickListener {

        void onAddImageClicked();
    }

    class ImageViewHolder extends BaseViewHolder<File> {

        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.text)
        TextView text;

        public ImageViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(File item) {
            Picasso.with(itemView.getContext()).load(item).fit().into(image);
            text.setText(item.getName());
        }
    }

    class ButtonViewHolder extends BaseViewHolder<File> {

        @BindView(R.id.text)
        Button text;

        public ButtonViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(File item) {
            if (items.isEmpty()) {
                text.setText(R.string.wizard_photo_start);
            } else {
                text.setText(R.string.wizard_photo_continue);
            }

            text.setOnClickListener(v -> listener.onAddImageClicked());
        }
    }

    private List<File> items = new ArrayList<>();
    private final AddImageClickListener listener;

    public ImagesAdapter(AddImageClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void add(File item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
        notifyItemChanged(items.size());
    }

    public List<File> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        return position == items.size() ? 1 : 0;
    }

    @Override
    public BaseViewHolder<File> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_image, parent, false));
            case 1:
                return new ButtonViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_image_button, parent, false));
            default:
                throw new IllegalArgumentException("Should never happen");
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<File> viewHolder, int position) {
        if (getItemViewType(position) == 0) {
            File sm = items.get(position);
            viewHolder.bind(sm);
        } else {
            viewHolder.bind(null);
        }
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == 0) {
            return items.get(position).hashCode();
        } else {
            return -7;
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }
}
