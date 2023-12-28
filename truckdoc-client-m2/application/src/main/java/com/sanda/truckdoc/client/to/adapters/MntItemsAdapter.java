package com.sanda.truckdoc.client.to.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistResultNode;
import com.sanda.truckdoc.client.to.OnClickToListener;
import com.sanda.truckdoc.client.to.PerformMntActivity;
import com.sanda.truckdoc.client.to.data.Model;
import com.sanda.truckdoc.client.to.data.ToNode;

import java.util.List;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by k.natallie on 03.02.2016.
 * Adapter for TO item
 */
public class MntItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int PARENT_NODE = 0;
    private static final int CHILD_NODE = 1;
    protected List<? extends ChecklistResultNode> mDataset;
    private boolean mUntracked = false;
    private OnItemSelected mOnItemSelected;
    private Context mContext;
    private Model model;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public TextView status;
        public View line;
        public View separator;
        public ImageView icon;
        public ImageView status_icon;
        public ImageView comment_icon;
        public ImageView photo_icon;

        public ViewHolder(View itemView) {
            super(itemView);
            line = itemView;
            mTextView = itemView.findViewById(R.id.info_text);
            status = itemView.findViewById(R.id.status_text);
            separator = itemView.findViewById(R.id.separator);
            icon = itemView.findViewById(R.id.icon);
            status_icon = itemView.findViewById(R.id.status_image);
            comment_icon = itemView.findViewById(R.id.comment_icon);
            photo_icon = itemView.findViewById(R.id.photo_icon);
        }

    }

    private static class ParentViewHolder extends RecyclerView.ViewHolder {
        public TextView mParentName;

        public ParentViewHolder(View itemView) {
            super(itemView);
            mParentName = itemView.findViewById(R.id.parent_text);

        }

    }

    public MntItemsAdapter(List<? extends ChecklistResultNode> myDataset, OnItemSelected onItemSelected, Context context,
                           boolean untracked) {
        mDataset = myDataset;
        mOnItemSelected = onItemSelected;
        mContext = context;
        model = Model.getInstance(mContext);
        mUntracked = untracked;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View v;
        switch (viewType) {
            case PARENT_NODE:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_parent, parent, false);
                return new ParentViewHolder(v);

            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_card, parent, false);
                return new ViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getChildren() != null && mDataset.get(position).getChildren().size() > 0 ? 0 : 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder1, int position) {

        final ToNode item = (ToNode) mDataset.get(holder1.getAdapterPosition());
        int viewType = getItemViewType(holder1.getAdapterPosition());
        switch (viewType) {
            case PARENT_NODE:
                ParentViewHolder parentHolder = (ParentViewHolder) holder1;
                parentHolder.mParentName.setText(item.getName());

                break;
            case CHILD_NODE:
                ViewHolder holder = (ViewHolder) holder1;
                holder.mTextView.setText(
                        mUntracked ? ((item.getParent() != null) ? item.getParent().getTitleText() : " ") + ">..>" + item.getName() :
                                item.getName());
                String resName = model.getNameOfIconForNode(model.getConfigNodes(), item);
                int drawableResourceId = mContext.getResources().getIdentifier("mnt_icon_" + resName, "drawable", mContext.getPackageName());
                if (drawableResourceId > 0) {
                    holder.icon.setImageResource(drawableResourceId);
                } else {
                    holder.icon.setImageResource(R.drawable.mnt_icon_unknown);
                }
                if (item.isLastChild()) {
                    holder.separator.setVisibility(View.VISIBLE);
                } else {
                    holder.separator.setVisibility(View.GONE);
                }
                if (TextUtils.isEmpty(item.getValue())) {
                    holder.status.setTextColor(mContext.getResources().getColor(R.color.mnt_group_title));
                    holder.icon.clearColorFilter();
                    holder.icon.setColorFilter(mContext.getResources().getColor(R.color.mnt_group_title), PorterDuff.Mode.SRC_ATOP);
                    holder.status.setText(R.string.mnt_not_checked);
                    holder.status_icon.setVisibility(View.GONE);
                    holder.comment_icon.setVisibility(View.GONE);
                    holder.photo_icon.setVisibility(View.GONE);
                } else {
                    holder.status_icon.setVisibility(View.VISIBLE);
                    switch (item.getValue()) {
                        case "OK":
                            holder.icon.clearColorFilter();

                            holder.icon.setColorFilter(mContext.getResources().getColor(R.color.mnt_ok), PorterDuff.Mode.SRC_ATOP);
                            holder.status.setTextColor(mContext.getResources().getColor(R.color.mnt_ok));
                            holder.status.setText(Boolean.TRUE.equals(item.getValidated()) ? "Сохранено" : mContext.getResources().getString(R.string.mtn_ok));
                            holder.status_icon.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.mnt_ok, null));
                            holder.status_icon.setColorFilter(mContext.getResources().getColor(R.color.mnt_ok), PorterDuff.Mode.SRC_ATOP);
                            holder.comment_icon.setVisibility(View.GONE);
                            holder.photo_icon.setVisibility(View.GONE);
                            break;
                        case "NOT_OK":
                            holder.icon.clearColorFilter();
                            holder.icon.setColorFilter(mContext.getResources().getColor(R.color.mnt_problem), PorterDuff.Mode.SRC_ATOP);
                            holder.status.setTextColor(mContext.getResources().getColor(R.color.mnt_problem));
                            holder.status_icon.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.mnt_problems, null));
                            holder.status_icon.setColorFilter(mContext.getResources().getColor(R.color.mnt_problem), PorterDuff.Mode.SRC_ATOP);
                            holder.status.setText(R.string.mtn_problem);
                            if (!TextUtils.isEmpty(item.getComment())) {
                                holder.comment_icon.setVisibility(View.VISIBLE);
                            } else {
                                holder.comment_icon.setVisibility(View.GONE);
                            }

                            Log.d("item", item.getName() + item.getAttachedFiles().isEmpty());
                            if (item.getAttachedFiles().isEmpty()) {
                                holder.photo_icon.setVisibility(View.GONE);
                            } else {
                                holder.photo_icon.setVisibility(View.VISIBLE);
                            }
                            break;
                        case "UNDEFINED":
                            holder.icon.clearColorFilter();
                            holder.icon.setColorFilter(mContext.getResources().getColor(R.color.mnt_neutral, null), PorterDuff.Mode.SRC_ATOP);
                            holder.status.setTextColor(mContext.getResources().getColor(R.color.mnt_neutral, null));
                            holder.status_icon.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.mnt_icon_unknown, null));
                            holder.status_icon.setColorFilter(mContext.getResources().getColor(R.color.mnt_neutral), PorterDuff.Mode.SRC_ATOP);
                            holder.status.setText("Значение не определено");
                            if (!TextUtils.isEmpty(item.getComment())) {
                                holder.comment_icon.setVisibility(View.VISIBLE);
                            } else {
                                holder.comment_icon.setVisibility(View.GONE);
                            }

                            Log.d("item", item.getName() + item.getAttachedFiles().isEmpty());
                            if (item.getAttachedFiles().isEmpty()) {
                                holder.photo_icon.setVisibility(View.GONE);
                            } else {
                                holder.photo_icon.setVisibility(View.VISIBLE);
                            }
                            break;
                        default:
                    }
                }


                holder.line.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemSelected.onItemSelected(holder.getAdapterPosition());
                        Model.getInstance(mContext).setCurrentNode(item);
                        if (item.getChildren() == null || item.getChildren().isEmpty()) {
                            (v.getContext()).startActivity(new Intent(v.getContext(), PerformMntActivity.class));
                        } else {
                            ((OnClickToListener) v.getContext()).OnClickTo(item);

                        }

                    }
                });
        }
    }

    @Override
    public int getItemCount() {
        if (mDataset == null) {
            return 0;
        }
        return mDataset.size();
    }


    public interface OnItemSelected {
        void onItemSelected(int position);
    }

    public List<? extends ChecklistResultNode> getDataset() {
        return mDataset;
    }

    public void setDataset(List<? extends ChecklistResultNode> dataset) {
        mDataset = dataset;
    }
}
