package app.instructions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.sanda.truckdoc.client.api.v3.sync.instructions.Entry
import com.sanda.truckdoc.client.api.v3.sync.instructions.Instructions
import kotlinx.android.synthetic.main.instructions_activity.*
import kotlinx.android.synthetic.main.instructions_leaf_listitem.view.*
import org.codehaus.jackson.map.ObjectMapper

private val json = """
    {
      "version": 1,
      "entries": [
        {
          "type": "node",
          "id": "aps_safe",
          "displayName": "APS-Safe",
          "icon": "menu/aps_safe.png",
          "entries": [
            {
              "type": "leaf",
              "id": "by_stops",
              "displayName": "Беларусь",
              "icon": "countries/by.png",
              "current": {
                "fileId": 1233,
                "fileName": "belarus_stops.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            },
            {
              "type": "leaf",
              "id": "pl_stops",
              "displayName": "Poland",
              "icon": "countries/pl.png",
              "current": {
                "fileId": 1234,
                "fileName": "poland_stops.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            }
          ]
        },
        {
          "type": "node",
          "id": "instructions",
          "displayName": "Instructions",
          "icon": "menu/instructions.png",
          "entries": [
            {
              "type": "leaf",
              "id": "road_accidents",
              "displayName": "Действия при ДТП",
              "icon": "instructions/ra.png",
              "current": {
                "fileId": 1235,
                "fileName": "ra.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            },
            {
              "type": "leaf",
              "id": "health_issues",
              "displayName": "Действия при расстройстве здоровья, травме",
              "icon": "instructions/hi.png",
              "current": {
                "fileId": 1236,
                "fileName": "hi.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            }
          ]
        },
        {
          "type": "node",
          "id": "country_info",
          "displayName": "Инфо страны",
          "icon": "menu/country_info.png",
          "entries": [
            {
              "type": "leaf",
              "id": "info_by",
              "displayName": "Беларусь",
              "icon": "countries/by.png",
              "current": {
                "fileId": 1237,
                "fileName": "by_info.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            },
            {
              "type": "leaf",
              "id": "info_ge",
              "displayName": "Германия",
              "icon": "countries/ge.png",
              "current": {
                "fileId": 1238,
                "fileName": "ge_info.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            }
          ]
        }
      ]
    }
""".trimIndent()

private val mock: InstructionsProvider by lazy {
    object : InstructionsProvider {
        override fun getInstructions(): Instructions? {
            return ObjectMapper().readValue(json, Instructions::class.java)
        }
    }
}

class InstructionsActivity : AppCompatActivity(R.layout.instructions_activity) {

    private val instructionsProvider by lazy { mock }
    private val entries: List<Entry> by lazy {
        intent.getSerializableExtra("entries") as? ArrayList<Entry> ?: instructionsProvider.getInstructions()!!.entries
    }

    private val title by lazy { intent.getStringExtra("title") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView.adapter = InstructionsAdapter(entries)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title?.let { supportActionBar?.title = it }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

interface InstructionsProvider {
    fun getInstructions(): Instructions?
}

class InstructionsAdapter(entries: List<Entry>) : ListAdapter<Entry, InstructionsAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Entry?>() {
    override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean = oldItem == newItem
}) {
    init {
        submitList(entries)
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(entry: Entry)
    }

    private class NodeVH(itemView: View) : ViewHolder(itemView) {
        override fun bind(entry: Entry) {
            itemView.textView.text = entry.displayName
            itemView.updateLayoutParams<FlexboxLayoutManager.LayoutParams> {
                flexBasisPercent = 0.5f
                alignSelf = AlignItems.STRETCH
            }
            val c = itemView.context
            itemView.setOnClickListener {
                c.startActivity(Intent(c, InstructionsActivity::class.java).putExtra("entries", arrayListOf(*entry.entries.toTypedArray())).putExtra("title", entry.displayName))
            }
        }
    }

    private class LeafVH(itemView: View) : ViewHolder(itemView) {
        override fun bind(entry: Entry) {
            itemView.textView.text = entry.displayName
            itemView.updateLayoutParams<FlexboxLayoutManager.LayoutParams> {
                flexBasisPercent = 0.5f
                alignSelf = AlignItems.STRETCH
            }
            val c = itemView.context
            itemView.setOnClickListener {
                //c.startActivity(Intent(c, Ins))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.instructions_leaf_listitem, parent, false)

        return if (viewType == 0)
            NodeVH(itemView)
        else
            LeafVH(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).type) {
            Entry.Type.NODE -> 0
            Entry.Type.LEAF -> 1
        }
    }
}
