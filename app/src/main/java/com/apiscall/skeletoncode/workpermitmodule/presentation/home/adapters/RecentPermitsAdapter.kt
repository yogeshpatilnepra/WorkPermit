package com.apiscall.skeletoncode.workpermitmodule.presentation.home.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.databinding.ItemPermitCardBinding
import com.apiscall.skeletoncode.workpermitmodule.presentation.home.HomeViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusColor
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusText
import java.text.SimpleDateFormat
import java.util.Locale

class RecentPermitsAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<HomeViewModel.DashboardPermit, RecentPermitsAdapter.PermitViewHolder>(
    PermitDiffCallback()
) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermitViewHolder {
        val binding =
            ItemPermitCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PermitViewHolder(binding, onItemClick, dateFormat)
    }

    override fun onBindViewHolder(holder: PermitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PermitViewHolder(
        private val binding: ItemPermitCardBinding,
        private val onItemClick: (String) -> Unit,
        private val dateFormat: SimpleDateFormat
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(permit: HomeViewModel.DashboardPermit) {
            binding.tvPermitNumber.text = permit.permitNumber
            binding.tvPermitType.text = permit.type.name.replace("_", " ").lowercase()
                .replaceFirstChar { char -> char.uppercase() }
            binding.tvPermitTitle.text = permit.title
            binding.tvLocation.text = permit.location

            // Set status
            binding.tvStatus.text = permit.status.getStatusText()
            binding.tvStatus.setBackgroundResource(permit.status.getStatusColor())

            // Set date
            binding.tvDate.text = dateFormat.format(System.currentTimeMillis()) // Placeholder

            binding.root.setOnClickListener { onItemClick(permit.id) }
        }
    }

    class PermitDiffCallback : DiffUtil.ItemCallback<HomeViewModel.DashboardPermit>() {
        override fun areItemsTheSame(
            oldItem: HomeViewModel.DashboardPermit,
            newItem: HomeViewModel.DashboardPermit
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: HomeViewModel.DashboardPermit,
            newItem: HomeViewModel.DashboardPermit
        ) =
            oldItem == newItem
    }
}