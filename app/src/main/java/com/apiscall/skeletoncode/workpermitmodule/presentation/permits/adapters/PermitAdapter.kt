package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.databinding.ItemPermitCardBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.utils.formatDate
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusColor
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusText

class PermitAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<Permit, PermitAdapter.PermitViewHolder>(PermitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermitViewHolder {
        val binding =
            ItemPermitCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PermitViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: PermitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PermitViewHolder(
        private val binding: ItemPermitCardBinding,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(permit: Permit) {
            binding.tvPermitNumber.text = permit.permitNumber
            binding.tvPermitType.text = permit.permitType.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
            binding.tvPermitTitle.text = permit.title
            binding.tvLocation.text = permit.location
            binding.tvStatus.text = permit.status.getStatusText()
            binding.tvStatus.setBackgroundResource(permit.status.getStatusColor())
            binding.tvDate.text = permit.createdAt.formatDate()

            binding.root.setOnClickListener { onItemClick(permit.id) }
        }
    }

    class PermitDiffCallback : DiffUtil.ItemCallback<Permit>() {
        override fun areItemsTheSame(oldItem: Permit, newItem: Permit) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Permit, newItem: Permit) = oldItem == newItem
    }
}