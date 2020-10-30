package app.messages2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sanda.truckdoc.client.data.model.ServerMessage
import kotlinx.android.synthetic.main.messages_chat_in_listitem.view.*
import java.util.*

internal class MessageAdapter() : RecyclerView.Adapter<MessageAdapter.ViewHolder>(/**/) {
    private val items = mutableListOf<ServerMessage>()

    private object DiffCallback : DiffUtil.ItemCallback<ServerMessage?>() {
        override fun areItemsTheSame(oldItem: ServerMessage, newItem: ServerMessage): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ServerMessage, newItem: ServerMessage): Boolean = oldItem == newItem
    }

    init {
        setHasStableIds(true)
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(item: ServerMessage) {
            itemView.apply {
                subject.text = item.text

                subject.text = item.text
                date.setText(item.savedDate?.toString("yyyy-MMM-dd HH:mm:ss", Locale.getDefault()))
                status.setText(if (item.isSent) R.string.status_sent else R.string.status_wait)
                hidden.visibility = if (item.isHidden) View.VISIBLE else View.GONE
            }
        }
    }

    class OutViewHolder(itemView: View) : ViewHolder(itemView)
    class InViewHolder(itemView: View) : ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder =
            if (type == 0)
                InViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.messages_chat_in_listitem, parent, false))
            else
                OutViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.messages_chat_out_listitem, parent, false))

    override fun getItemViewType(position: Int): Int = if (getItem(position).isOutgoing) 1 else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getItem(position: Int) = items[position]

    //    override fun getItemId(position: Int): Long = super.getItem(position).id.toLong()
    override fun getItemCount(): Int = items.size

    fun submitList(list: List<ServerMessage>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size

            override fun getNewListSize(): Int = list.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    DiffCallback.areItemsTheSame(items[oldItemPosition], list[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    DiffCallback.areContentsTheSame(items[oldItemPosition], list[newItemPosition])
        })
        items.clear()
        items += list
//        notifyDataSetChanged()
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }
}
