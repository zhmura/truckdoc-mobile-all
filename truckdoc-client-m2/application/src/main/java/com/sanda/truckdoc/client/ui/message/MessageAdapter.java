package com.sanda.truckdoc.client.ui.message;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanda.truckdoc.client.Consts;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.ui.utils.BaseViewHolder;
import com.sanda.truckdoc.client.util.RoleTypeMapper;

import net.tribe7.common.base.Optional;
import net.tribe7.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sanda.truckdoc.client.ui.utils.ViewUtils.getColorRef;

/**
 * Created by astra on 28.05.2015.
 */
public class MessageAdapter extends RecyclerView.Adapter<BaseViewHolder<ServerMessage>> {

    interface ServiceMessageClickListener {

        void onServiceMessageClicked(ServerMessage sm);

        boolean onServiceMessageDismissed(ServerMessage sm);
    }

    class InViewHolder extends BaseViewHolder<ServerMessage> {

        @BindView(R.id.target)
        TextView target;
        @BindView(R.id.subject)
        TextView subject;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.hidden)
        TextView hidden;
        @BindView(R.id.attachment)
        ImageView attachment;
        @BindView(R.id.avatar)
        ImageView avatar;

        public InViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(ServerMessage item) {
            Context context = itemView.getContext();
            Pair<String, Integer> params = getParams(context, item, RoleTypeMapper.IN);

            target.setText(params.first);
            avatar.setImageDrawable(getColoredAvatar(context, params.second));
            subject.setText(item.getText());
            date.setText(item.getSavedDate().toString(Consts.DATE_TIME_FORMAT));
            attachment.setVisibility(item.getAttachments().size() > 0 ? VISIBLE : GONE);
            itemView.setOnClickListener(v -> listener.onServiceMessageClicked(item));
            hidden.setVisibility(item.isHidden() ? VISIBLE : GONE);
        }
    }

    class OutViewHolder extends BaseViewHolder<ServerMessage> {

        @BindView(R.id.target)
        TextView target;
        @BindView(R.id.subject)
        TextView subject;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.status)
        TextView status;
        @BindView(R.id.hidden)
        TextView hidden;
        @BindView(R.id.avatar)
        ImageView avatar;

        public OutViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(ServerMessage item) {
            Context context = itemView.getContext();
            Pair<String, Integer> params = getParams(context, item, RoleTypeMapper.OUT);
            target.setText(params.first);
            avatar.setImageDrawable(getColoredAvatar(context, params.second));
            subject.setText(item.getText());
            date.setText(item.getSavedDate().toString(Consts.DATE_TIME_FORMAT));
            status.setText(item.isSent() ? R.string.status_sent : R.string.status_wait);
            hidden.setVisibility(item.isHidden() ? VISIBLE : GONE);
        }
    }

    private Pair<String, Integer> getParams(Context context, ServerMessage message, int direction) {
        String label;
        if (direction == RoleTypeMapper.IN) {
            long senderVirtualGroupId = message.getSenderVirtualGroupId() == null ? -1L : message.getSenderVirtualGroupId();
            long senderUserId = message.getSenderUserId() == null ? -1L : message.getSenderUserId();

            Optional<DbContactRecord> match = contacts.firstMatch(record -> record.getRecipientId() == senderUserId)
                    .or(contacts.firstMatch(record -> record.getRecipientId() == senderVirtualGroupId));
            label = match.transform(DbContactRecord::getLabel)
                    .or((message.getSenderName() != null && !message.getSenderName().equals(""))
                            ? message.getSenderName()
                            : RoleTypeMapper.convert(direction, message.getSenderRoleId(), context));

            int color = match.transform(DbContactRecord::getColor).or(getColorRef(context, R.color.message_primary_text));
            return Pair.create(label, color);
        } else {
            long recipientId = message.getRecipientId() == null ? -1L : message.getRecipientId();
            Optional<DbContactRecord> match = contacts.firstMatch(record -> record.getRecipientId() == recipientId);
            label = match.transform(DbContactRecord::getLabel).or(RoleTypeMapper.convert(direction, message.getSenderRoleId(), context));
            int color = match.transform(DbContactRecord::getColor).or(getColorRef(context, R.color.message_primary_text));
            return Pair.create(label, color);
        }
    }

    @NonNull
    private Drawable getColoredAvatar(Context context, int color) {
        Drawable image = DrawableCompat.wrap(Objects.requireNonNull(ResourcesCompat.getDrawable(context.getResources(),
                R.drawable.avatar_placeholder,
                null)));
        DrawableCompat.setTint(image.mutate(), color);
        return image;
    }

    private List<ServerMessage> items = new ArrayList<>();
    private FluentIterable<DbContactRecord> contacts = FluentIterable.from(Collections.emptyList());
    private final ServiceMessageClickListener listener;

    public MessageAdapter(ServiceMessageClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void attachSwipeCallback(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback swipeItemCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getPosition();
                ServerMessage sm = items.get(position);
                if (listener.onServiceMessageDismissed(sm)) {
                    notifyItemChanged(position);
                } else {
                    items.remove(sm);
                    notifyItemRemoved(position);
                }
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getPosition();
                ServerMessage sm = items.get(position);
                int swipeFlags = 0;
                if (!sm.isHidden()) {
                    swipeFlags = ItemTouchHelper.RIGHT;
                }
                return makeMovementFlags(0, swipeFlags);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeItemCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void swapItems(Pair<List<ServerMessage>, List<DbContactRecord>> data) {
        this.items = data.first;
        this.contacts = FluentIterable.from(data.second);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isOutgoing() ? 1 : 0;
    }

    @Override
    public BaseViewHolder<ServerMessage> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new InViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_message_in, parent, false));
            case 1:
                return new OutViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_message_out, parent, false));
            default:
                throw new IllegalArgumentException("Should never happen");
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<ServerMessage> viewHolder, int i) {
        ServerMessage sm = items.get(i);
        viewHolder.bind(sm);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
