package com.apiscall.skeletoncode.workpermitmodule.presentation.approvals


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.ItemPermitActionBinding
import com.apiscall.skeletoncode.workpermitmodule.presentation.approvals.ApprovalQueueViewModel.PermitAction
import java.text.SimpleDateFormat
import java.util.Locale

class PermitActionAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<PermitAction, PermitActionAdapter.ActionViewHolder>(ActionDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val binding =
            ItemPermitActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionViewHolder(binding, onItemClick, dateFormat)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ActionViewHolder(
        private val binding: ItemPermitActionBinding,
        private val onItemClick: (String) -> Unit,
        private val dateFormat: SimpleDateFormat
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(action: PermitAction) {
            binding.tvPermitNumber.text = action.permitNumber
            binding.tvTitle.text = action.title
            binding.tvTimestamp.text = dateFormat.format(action.timestamp)

            // Set action text and background
            binding.tvAction.text = action.action

            // Set appropriate background based on action type
            val backgroundRes = when (action.action) {
                "Approved" -> R.drawable.bg_status_approved
                "Rejected" -> R.drawable.bg_status_rejected
                "Sent Back" -> R.drawable.bg_status_sentback
                else -> R.drawable.bg_status_pending
            }
            binding.tvAction.setBackgroundResource(backgroundRes)
            binding.tvAction.setTextColor(
                binding.root.context.getColor(
                    android.R.color.white
                )
            )

            binding.root.setOnClickListener { onItemClick(action.permitId) }
        }
    }

    class ActionDiffCallback : DiffUtil.ItemCallback<PermitAction>() {
        override fun areItemsTheSame(oldItem: PermitAction, newItem: PermitAction) =
            oldItem.permitId == newItem.permitId

        override fun areContentsTheSame(oldItem: PermitAction, newItem: PermitAction) =
            oldItem == newItem
    }
}