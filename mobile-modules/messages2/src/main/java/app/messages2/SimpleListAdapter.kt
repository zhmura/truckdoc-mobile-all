package app.messages2

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class SimpleListAdapter<T>(diffCallback: DiffUtil.ItemCallback<T?> = SimpleDiffCallback())
    : ListAdapter<T, SimpleListAdapter.ViewHolder>(diffCallback) {
    //we use kotlin android extensions for views
    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    abstract val layoutId: Int

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
}

class SimpleDiffCallback<T> : DiffUtil.ItemCallback<T?>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
}

class DiffCallback<T : WithId?> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem?.id == newItem?.id

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
}

interface WithId {
    val id: Long
    override operator fun equals(other: Any?): Boolean
}
