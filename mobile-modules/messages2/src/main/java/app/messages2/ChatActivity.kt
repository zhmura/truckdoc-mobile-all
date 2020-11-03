package app.messages2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding.widget.RxTextView
import com.sanda.truckdoc.client.data.MessagesDatabaseService
import com.sanda.truckdoc.client.data.model.AttachmentInfo
import com.sanda.truckdoc.client.data.model.DbContactRecord
import com.sanda.truckdoc.client.data.model.ServerMessage
import common.LiveDataObserveDsl
import common.RxObserveDsl
import common.serializable
import kotlinx.android.synthetic.main.messages_chat_activity.*
import javax.inject.Inject

class ChatActivity : AppCompatActivity(R.layout.messages_chat_activity), RxObserveDsl, LiveDataObserveDsl {

    private val ACTION_SEND_TEXT_MESSAGE = "com.sanda.truckdoc.client.service.action.UPLOAD_FILE"

    private val contact by serializable<DbContactRecord>()

    @Inject
    lateinit var db: MessagesDatabaseService

    private lateinit var onMessagesMenu: OnMessagesMenu
    private lateinit var onMessageClick: OnMessageClicked

    @Inject
    lateinit var provider: MessageDependenciesProvider

    var showHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as Messages2InjectorProvider).appComponent().inject(this)
        onMessagesMenu = provider.provideOnMessageMenu(this)
        onMessageClick = provider.provideOnMessageClicked(this)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.title = contact.label

        sendBtn.isEnabled = false
        RxTextView.textChanges(messageTxt).observe {
            sendBtn.isEnabled = messageTxt.text.toString().isNotBlank()
        }

        sendBtn.setOnClickListener {
            sendMessageToOperator(messageTxt.text.toString(), contact)
        }

        val adapter = MessageAdapter(::onMessageClicked, ::onDismiss)
        recyclerView.adapter = adapter
        adapter.attachSwipeCallback(recyclerView)

        db.getMessagesLive(contact.id.toInt()).observe {
            adapter.submitList(it)
            recyclerView.scrollToPosition(0)
        }
    }

    private fun onDismiss(serverMessage: ServerMessage): Boolean {
        db.markHidden(serverMessage)
        return showHidden
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.inbox_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu!!.findItem(R.id.menu_show_hidden).isChecked = showHidden
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_delete_all -> onMessagesMenu.deleteAll()
            R.id.menu_clear_app_data -> onMessagesMenu.clearAppData()
            R.id.menu_settings -> onMessagesMenu.showSettings()
            R.id.menu_refresh -> onMessagesMenu.refresh()
            R.id.menu_export -> onMessagesMenu.export()
            R.id.menu_log_report -> onMessagesMenu.logReport()
            R.id.menu_show_hidden -> Unit
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onMessageClicked(serverMessage: ServerMessage) {
        val attachments = db.findAttachments(serverMessage)
        onMessageClick.show(serverMessage, attachments) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(messageTxt, 0)
        }
    }
}

interface OnMessagesMenu {
    fun deleteAll()
    fun clearAppData()
    fun showSettings()
    fun refresh()
    fun export()
    fun logReport()
}

interface OnMessageClicked {
    fun show(sm: ServerMessage, attachments: List<AttachmentInfo>, onReply: () -> Unit)
}
