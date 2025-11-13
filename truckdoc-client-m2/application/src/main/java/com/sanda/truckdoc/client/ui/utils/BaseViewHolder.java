package com.sanda.truckdoc.client.ui.utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by astra on 28.05.2015.
 */
public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(T item);
}
