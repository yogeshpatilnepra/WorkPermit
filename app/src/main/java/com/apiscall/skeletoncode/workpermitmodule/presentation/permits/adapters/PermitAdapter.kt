package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.databinding.ItemPermitCardBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusColor
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusText
import java.text.SimpleDateFormat
import java.util.*

class PermitAdapter(
    private val onItemClick: (String) -> Unit,
    private val onApproveClick: ((String) -> Unit)? = null,
    private val onRejectClick: ((String) -> Unit)? = null,
    private val onSendBackClick: ((String) -> Unit)? = null
) : ListAdapter<Permit, PermitAdapter.PermitViewHolder>(PermitDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermitViewHolder {
        val binding =
            ItemPermitCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PermitViewHolder(
            binding,
            onItemClick,
            onApproveClick,
            onRejectClick,
            onSendBackClick,
            dateFormat
        )
    }

    override fun onBindViewHolder(holder: PermitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PermitViewHolder(
        private val binding: ItemPermitCardBinding,
        private val onItemClick: (String) -> Unit,
        private val onApproveClick: ((String) -> Unit)?,
        private val onRejectClick: ((String) -> Unit)?,
        private val onSendBackClick: ((String) -> Unit)?,
        private val dateFormat: SimpleDateFormat
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(permit: Permit) {
            binding.tvPermitNumber.text = permit.permitNumber
            binding.tvPermitType.text = permit.permitType.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
            binding.tvPermitTitle.text = permit.title
            binding.tvLocation.text = permit.location
            binding.tvStatus.text = getStatusText(permit.status.name)
            binding.tvStatus.setBackgroundResource(getStatusColor(permit.status.name))
            binding.tvDate.text = dateFormat.format(permit.createdAt)

            // Show action buttons only if approval stage is review and callbacks are provided
            val isApprovalStage = permit.approvalStage == "issuer_review" ||
                    permit.approvalStage == "ehs_review" ||
                    permit.approvalStage == "area_owner_review"

            if (isApprovalStage && onApproveClick != null && onRejectClick != null && onSendBackClick != null) {
                binding.actionButtons.visibility = View.VISIBLE
                binding.btnApprove.setOnClickListener {
                    onApproveClick.invoke(permit.id)
                }
                binding.btnReject.setOnClickListener {
                    onRejectClick.invoke(permit.id)
                }
                binding.btnSendBack.setOnClickListener {
                    onSendBackClick.invoke(permit.id)
                }
            } else {
                binding.actionButtons.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(permit.id) }
        }
    }

    class PermitDiffCallback : DiffUtil.ItemCallback<Permit>() {
        override fun areItemsTheSame(oldItem: Permit, newItem: Permit) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Permit, newItem: Permit) = oldItem == newItem
    }
}