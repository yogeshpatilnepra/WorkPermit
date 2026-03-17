package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.databinding.ItemApprovalHistoryBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalHistory
import com.apiscall.skeletoncode.workpermitmodule.utils.formatFull
import com.apiscall.skeletoncode.workpermitmodule.utils.loadImage

class ApprovalHistoryAdapter :
    ListAdapter<ApprovalHistory, ApprovalHistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding =
            ItemApprovalHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemApprovalHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(history: ApprovalHistory) {
            binding.tvAction.text = history.action.name
            binding.tvUserName.text = history.user.fullName
            binding.tvTimestamp.text = history.timestamp.formatFull()
            binding.tvComments.text = history.comments ?: ""
            binding.ivUser.loadImage(history.user.profileImageUrl)
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<ApprovalHistory>() {
        override fun areItemsTheSame(oldItem: ApprovalHistory, newItem: ApprovalHistory) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ApprovalHistory, newItem: ApprovalHistory) =
            oldItem == newItem
    }
}