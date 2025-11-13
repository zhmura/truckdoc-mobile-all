package com.sanda.truckdoc.client.wizard.wizardpager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.databinding.ListitemImageBinding;
import com.sanda.truckdoc.client.databinding.ListitemImageButtonBinding;
import com.sanda.truckdoc.client.ui.utils.BaseViewHolder;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ImagesAdapter extends RecyclerView.Adapter<BaseViewHolder<File>> {

    interface AddImageClickListener {
        void onAddImageClicked();
    }

    class ImageViewHolder extends BaseViewHolder<File> {
        private final ListitemImageBinding binding;

        public ImageViewHolder(View itemView) {
            super(itemView);
            binding = ListitemImageBinding.bind(itemView);
        }

        @Override
        public void bind(File item) {
            Picasso.get().load(item).fit().into(binding.image);
            binding.text.setText(item.getName());
        }
    }

    class ButtonViewHolder extends BaseViewHolder<File> {
        private final ListitemImageButtonBinding binding;

        public ButtonViewHolder(View itemView) {
            super(itemView);
            binding = ListitemImageButtonBinding.bind(itemView);
        }

        @Override
        public void bind(File item) {
            if (items.isEmpty()) {
                binding.text.setText(R.string.wizard_photo_start);
            } else {
                binding.text.setText(R.string.wizard_photo_continue);
            }

            binding.text.setOnClickListener(v -> listener.onAddImageClicked());
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 0:
                return new ImageViewHolder(inflater.inflate(R.layout.listitem_image, parent, false));
            case 1:
                return new ButtonViewHolder(inflater.inflate(R.layout.listitem_image_button, parent, false));
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
