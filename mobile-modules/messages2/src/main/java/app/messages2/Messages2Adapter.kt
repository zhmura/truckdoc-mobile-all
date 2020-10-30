package app.messages2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sanda.truckdoc.client.data.ServerMessageWithAttachmentCount
import kotlinx.android.synthetic.main.messages_chat_in_listitem.view.*
import java.util.*

internal class MessageAdapter() : PagingDataAdapter<ServerMessageWithAttachmentCount, MessageAdapter.ViewHolder>(DiffCallback) {
//    private val items = mutableListOf<ServerMessageWithAttachmentCount>()

    private object DiffCallback : DiffUtil.ItemCallback<ServerMessageWithAttachmentCount>() {
        override fun areItemsTheSame(oldItem: ServerMessageWithAttachmentCount,
                                     newItem: ServerMessageWithAttachmentCount): Boolean = oldItem.message.id == newItem.message.id

        override fun areContentsTheSame(oldItem: ServerMessageWithAttachmentCount,
                                        newItem: ServerMessageWithAttachmentCount): Boolean = oldItem == newItem
    }

    init {
//        setHasStableIds(true)
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(item: ServerMessageWithAttachmentCount) {
            itemView.apply {
                subject.text = item.message.text

                subject.text = item.message.text
                date.setText(item.message.savedDate?.toString("yyyy-MMM-dd HH:mm:ss", Locale.getDefault()))
                status.setText(if (item.message.isSent) R.string.status_sent else R.string.status_wait)
                hidden.visibility = if (item.message.isHidden) View.VISIBLE else View.GONE
            }
        }
    }

    class OutViewHolder(itemView: View) : ViewHolder(itemView)

    class InViewHolder(itemView: View) : ViewHolder(itemView) {
        override fun bind(item: ServerMessageWithAttachmentCount) {
            super.bind(item)
            itemView.attachment.isVisible = item.count > 0
            itemView.count.isVisible = item.count > 0
            itemView.count.text = item.count.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder =
            if (type == 0)
                InViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.messages_chat_in_listitem, parent, false))
            else
                OutViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.messages_chat_out_listitem, parent, false))

    override fun getItemViewType(position: Int): Int = if (getItem(position)!!.message.isOutgoing) 1 else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

//    fun getItem(position: Int) = items[position]

    //    override fun getItemId(position: Int): Long = super.getItem(position).id.toLong()
//    override fun getItemCount(): Int = items.size

   /* fun submitList(list: List<ServerMessageWithAttachmentCount>) {
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
    }*/

/*    override fun getItemId(position: Int): Long {
        return items[position].message.id.toLong()
    }*/
}
