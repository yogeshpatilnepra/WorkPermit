package com.apiscall.skeletoncode.workpermitmodule.presentation.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.databinding.ItemStatCardBinding

data class StatCard(
    val title: String,
    val value: String,
    val iconRes: Int
)

class StatCardAdapter :
    ListAdapter<StatCard, StatCardAdapter.StatCardViewHolder>(StatCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatCardViewHolder {
        val binding =
            ItemStatCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StatCardViewHolder(
        private val binding: ItemStatCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: StatCard) {
            binding.tvStatTitle.text = stat.title
            binding.tvStatValue.text = stat.value
            binding.ivStatIcon.setImageResource(stat.iconRes)
        }
    }

    class StatCardDiffCallback : DiffUtil.ItemCallback<StatCard>() {
        override fun areItemsTheSame(oldItem: StatCard, newItem: StatCard) =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: StatCard, newItem: StatCard) = oldItem == newItem
    }
}