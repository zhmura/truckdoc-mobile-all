package app.messages2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.sanda.truckdoc.client.data.MessagesDatabaseService
import com.sanda.truckdoc.client.data.model.DbContactRecord
import common.LiveDataObserveDsl
import common.startActivity
import kotlinx.android.synthetic.main.messages_contact_list_activity.*
import kotlinx.android.synthetic.main.messages_contact_list_listitem.view.*
import javax.inject.Inject

class ContactListActivity : AppCompatActivity(R.layout.messages_contact_list_activity), LiveDataObserveDsl {

    @Inject
    lateinit var db: MessagesDatabaseService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (applicationContext as Messages2InjectorProvider).appComponent().inject(this)

        val adapter = Adapter(::onItem)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL))

        db.getContacts().observe {
            adapter.submitList(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun onItem(item: DbContactRecord) {
        startActivity<ChatActivity> {
            putExtra("contact", item)
        }
    }
}

class Adapter(val onCLick: (DbContactRecord) -> Unit) : SimpleListAdapter<DbContactRecord>() {
    override val layoutId: Int = R.layout.messages_contact_list_listitem

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.apply {
            name.text = item.label
            setOnClickListener { onCLick(item) }
        }
    }
}
