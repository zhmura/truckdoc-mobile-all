package app.instructions

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSetNode
import kotlinx.android.synthetic.main.instructions_leaf_listitem.view.*

class InstructionsAdapter(val helper: InstructionsHelper) : ListAdapter<InstructionDb, InstructionsAdapter.ViewHolder>(object : DiffUtil.ItemCallback<InstructionDb?>() {
    override fun areItemsTheSame(oldItem: InstructionDb, newItem: InstructionDb): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: InstructionDb, newItem: InstructionDb): Boolean = oldItem == newItem
}) {

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(entry: InstructionDb)
    }

    private class BranchVH(itemView: View) : ViewHolder(itemView) {
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
            if (helper.exists(entry.file))
                itemView.setOnClickListener {
                    try {
                        c.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(helper.getUri(entry.file), entry.file.mimeType))
                    } catch (e: Exception) {
                        Toast.makeText(c, c.getString(R.string.app_for_file_not_found, entry.file.fileId), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.instructions_leaf_listitem, parent, false)

        return if (viewType == 0)
            BranchVH(itemView)
        else
            LeafVH(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int =
            if (getItem(position).type == InstructionSetNode.Type.BRANCH)
                0
            else
                1
}
