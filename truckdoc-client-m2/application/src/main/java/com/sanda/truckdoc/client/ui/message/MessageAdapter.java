package com.sanda.truckdoc.client.ui.message;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanda.truckdoc.client.Consts;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.databinding.ListitemMessageInBinding;
import com.sanda.truckdoc.client.databinding.ListitemMessageOutBinding;
import com.sanda.truckdoc.client.ui.utils.BaseViewHolder;
import com.sanda.truckdoc.client.util.RoleTypeMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

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
        private final ListitemMessageInBinding binding;

        public InViewHolder(View itemView) {
            super(itemView);
            binding = ListitemMessageInBinding.bind(itemView);
        }

        @Override
        public void bind(ServerMessage item) {
            Context context = itemView.getContext();
            Pair<String, Integer> params = getParams(context, item, RoleTypeMapper.IN);

            binding.target.setText(params.first);
            binding.avatar.setImageDrawable(getColoredAvatar(context, params.second));
            binding.subject.setText(item.getText());
            binding.date.setText(item.getSavedDate().toString(Consts.DATE_TIME_FORMAT));
            binding.attachment.setVisibility(item.getAttachments().size() > 0 ? VISIBLE : GONE);
            itemView.setOnClickListener(v -> listener.onServiceMessageClicked(item));
            binding.hidden.setVisibility(item.isHidden() ? VISIBLE : GONE);
        }
    }

    class OutViewHolder extends BaseViewHolder<ServerMessage> {
        private final ListitemMessageOutBinding binding;

        public OutViewHolder(View itemView) {
            super(itemView);
            binding = ListitemMessageOutBinding.bind(itemView);
        }

        @Override
        public void bind(ServerMessage item) {
            Context context = itemView.getContext();
            Pair<String, Integer> params = getParams(context, item, RoleTypeMapper.OUT);
            binding.target.setText(params.first);
            binding.avatar.setImageDrawable(getColoredAvatar(context, params.second));
            binding.subject.setText(item.getText());
            binding.date.setText(item.getSavedDate().toString(Consts.DATE_TIME_FORMAT));
            binding.status.setText(item.isSent() ? R.string.status_sent : R.string.status_wait);
            binding.hidden.setVisibility(item.isHidden() ? VISIBLE : GONE);
        }
    }

    private Pair<String, Integer> getParams(Context context, ServerMessage message, int direction) {
        String label;
        if (direction == RoleTypeMapper.IN) {
            long senderVirtualGroupId = message.getSenderVirtualGroupId() == null ? -1L : message.getSenderVirtualGroupId();
            long senderUserId = message.getSenderUserId() == null ? -1L : message.getSenderUserId();

            Optional<DbContactRecord> match = contacts.stream().filter(record -> record.getRecipientId() == senderUserId).findFirst()
                    .or(() -> contacts.stream().filter(record -> record.getRecipientId() == senderVirtualGroupId).findFirst());
            label = match.map(DbContactRecord::getLabel).orElse(RoleTypeMapper.convert(direction, message.getSenderRoleId() != null ? message.getSenderRoleId().intValue() : null, context));

            int color = match.map(DbContactRecord::getColor).orElse(getColorRef(context, R.color.message_primary_text));
            return Pair.create(label, color);
        } else {
            long recipientId = message.getRecipientId() == null ? -1L : message.getRecipientId();
            Optional<DbContactRecord> match = contacts.stream().filter(record -> record.getRecipientId() == recipientId).findFirst();
            label = match.map(DbContactRecord::getLabel).orElse(RoleTypeMapper.convert(direction, message.getSenderRoleId() != null ? message.getSenderRoleId().intValue() : null, context));
            int color = match.map(DbContactRecord::getColor).orElse(getColorRef(context, R.color.message_primary_text));
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
    private List<DbContactRecord> contacts = new ArrayList<>();
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
        this.contacts = data.second;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isOutgoing() ? 1 : 0;
    }

    @Override
    public BaseViewHolder<ServerMessage> onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 0:
                return new InViewHolder(inflater.inflate(R.layout.listitem_message_in, parent, false));
            case 1:
                return new OutViewHolder(inflater.inflate(R.layout.listitem_message_out, parent, false));
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
