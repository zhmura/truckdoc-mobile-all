package app.instructions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.instructions_leaf_listitem.view.*

class InstructionsAdapter(val helper: InstructionsHelper) : ListAdapter<InstructionDb, InstructionsAdapter.ViewHolder>(object : DiffUtil.ItemCallback<InstructionDb?>() {
    override fun areItemsTheSame(oldItem: InstructionDb, newItem: InstructionDb): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: InstructionDb, newItem: InstructionDb): Boolean = oldItem == newItem
}) {

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(entry: InstructionDb)
    }

    private class NodeVH(itemView: View) : ViewHolder(itemView) {
        override fun bind(entry: InstructionDb) {
            itemView.textView.text = entry.displayName
            itemView.updateLayoutParams<FlexboxLayoutManager.LayoutParams> {
                flexBasisPercent = 0.5f
                alignSelf = AlignItems.STRETCH
            }
            val c = itemView.context
            itemView.setOnClickListener {
                InstructionsActivity.start(c, entry)
            }
        }
    }

    private inner class LeafVH(itemView: View) : ViewHolder(itemView) {
        override fun bind(entry: InstructionDb) {
            itemView.textView.text = entry.displayName
            itemView.updateLayoutParams<FlexboxLayoutManager.LayoutParams> {
                flexBasisPercent = 0.5f
                alignSelf = AlignItems.STRETCH
            }
            val c = itemView.context
            itemView.isEnabled = helper.exists(entry.file)
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

    override fun getItemViewType(position: Int): Int =
            if (getItem(position).file == null)
                0
            else
                1
}
