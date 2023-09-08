package hu.bme.aut.android.befitt.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import hu.bme.aut.android.befitt.databinding.FragmentStatBinding
import hu.bme.aut.android.befitt.model.Statistics

class StatItemRecyclerViewAdapter : ListAdapter<Statistics, StatItemRecyclerViewAdapter.ViewHolder>(itemCallback) {
    companion object {
        object itemCallback : DiffUtil.ItemCallback<Statistics>() {
            override fun areItemsTheSame(oldItem: Statistics, newItem: Statistics) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Statistics, newItem: Statistics) = oldItem == newItem
        }
    }

    interface StatItemClickListener {
        fun onItemClick(statistics: Statistics)
        fun onItemLongClick(position: Int, view: View, statistics: Statistics): Boolean
    }

    var itemClickListener: StatItemClickListener? = null

    inner class ViewHolder(val binding: FragmentStatBinding) : RecyclerView.ViewHolder(binding.root) {
        var stat: Statistics? = null

        init {
            itemView.setOnClickListener{
                stat?.let { stat -> itemClickListener?.onItemClick(stat) }
            }
            itemView.setOnLongClickListener{ view ->
                stat?.let { stat -> itemClickListener?.onItemLongClick(adapterPosition, view, stat) }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentStatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stat = this.getItem(position)

        holder.stat = stat
        holder.binding.tvDate.text = stat.startDate
        holder.binding.tvDistance.text = "${stat.distance} m"
    }
}