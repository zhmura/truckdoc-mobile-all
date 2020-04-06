package com.sanda.truckdoc.client.to.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sanda.truckdoc.client.to.data.TrailerType;

/**
 * Created by k.natallie on 27.03.2016.
 */
public class TrailerTypeAdapter extends ArrayAdapter<TrailerType> {
    private TrailerType[] data;


    public TrailerTypeAdapter(Context context, int resource, TrailerType[] objects) {
        super(context, resource, objects);
        data = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            row = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

            holder = new ViewHolder();
            holder.name = row.findViewById(android.R.id.text1);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        TrailerType type = data[position];
        holder.name.setText(type.getName());

        return row;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            row = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);

            holder = new ViewHolder();
            holder.name = row.findViewById(android.R.id.text1);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        TrailerType type = data[position];
        holder.name.setText(type.getName());

        return row;
    }

    private static class ViewHolder {
        TextView name;
    }
}
