package com.sanda.truckdoc.client.wizard.wizardpager.ui;

public class FixedImagesAdapter extends ImagesAdapter {

    public FixedImagesAdapter(AddImageClickListener listener) {
        super(listener);
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == 0) {
            return getItems().get(position).hashCode();
        } else {
            return -7;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItems().isEmpty()) {
            return position == getItems().size() ? 1 : 0;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        if (getItems().isEmpty()) {
            return getItems().size() + 1;
        } else {
            return getItems().size();
        }
    }
}
