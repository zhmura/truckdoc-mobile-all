package app.messages2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding.widget.RxTextView
import com.sanda.truckdoc.client.data.MessagesDatabaseService
import com.sanda.truckdoc.client.data.model.DbContactRecord
import com.sanda.truckdoc.client.data.model.ServerMessage
import common.LiveDataObserveDsl
import common.RxObserveDsl
import common.serializable
import kotlinx.android.synthetic.main.messages_chat_activity.*
import kotlinx.android.synthetic.main.messages_chat_out_listitem.view.*
import javax.inject.Inject

class ChatActivity : AppCompatActivity(R.layout.messages_chat_activity), RxObserveDsl, LiveDataObserveDsl {

    val ACTION_SEND_TEXT_MESSAGE = "com.sanda.truckdoc.client.service.action.UPLOAD_FILE"

    private val contact by serializable<DbContactRecord>()

    @Inject
    lateinit var db: MessagesDatabaseService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as Messages2InjectorProvider).appComponent().inject(this)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.title = contact.label

        sendBtn.isEnabled = false
        RxTextView.textChanges(messageTxt).observe {
            sendBtn.isEnabled = messageTxt.toString().isNotBlank()
        }

        sendBtn.setOnClickListener {
            sendMessageToOperator(messageTxt.text.toString(), contact)
        }

        val adapter = MessageAdapter()
        recyclerView.adapter = adapter

        db.getMessagesLive(true).observe {
            adapter.submitList(it)
            recyclerView.scrollToPosition(0)
        }
    }

    override fun getContext(): Context? = this

    private fun sendMessageToOperator(message: String, item: DbContactRecord) {
        val intent = Intent(ACTION_SEND_TEXT_MESSAGE)
        val b = Bundle()
        b.putString("com/sanda/truckdoc/client/message", message)
        b.putLong("mail.group", item.recipientId)
        b.putString("mail.group.type", item.recipientIdType)
        intent.putExtras(b)
        intent.setPackage(packageName)
        ContextCompat.startForegroundService(this, intent)
        messageTxt.text = null
    }
}

private class MessageAdapter() : RecyclerView.Adapter<MessageAdapter.ViewHolder>(/**/) {
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
