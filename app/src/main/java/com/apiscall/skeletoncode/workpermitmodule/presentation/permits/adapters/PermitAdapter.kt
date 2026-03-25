package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.databinding.ItemPermitCardBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusColor
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusText
import java.text.SimpleDateFormat
import java.util.*

class PermitAdapter(
    private var currentUserRole: Role? = null,
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
        // Pass the CURRENT role at bind time, not at ViewHolder creation time
        holder.bind(getItem(position), currentUserRole)
    }

    class PermitViewHolder(
        private val binding: ItemPermitCardBinding,
        private val onItemClick: (String) -> Unit,
        private val onApproveClick: ((String) -> Unit)?,
        private val onRejectClick: ((String) -> Unit)?,
        private val onSendBackClick: ((String) -> Unit)?,
        private val dateFormat: SimpleDateFormat
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(permit: Permit, currentUserRole: Role?) {
            binding.tvPermitNumber.text = permit.permitNumber
            binding.tvPermitType.text = permit.permitType.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
            binding.tvPermitTitle.text = permit.title
            binding.tvLocation.text = permit.location
            binding.tvStatus.text = getStatusText(permit.status.name)
            binding.tvStatus.setBackgroundResource(getStatusColor(permit.status.name))
            binding.tvDate.text = dateFormat.format(permit.createdAt)

            val stage = permit.approvalStage.lowercase().trim()

            // Show action buttons ONLY when the permit's stage matches the user's role
            // This prevents one role from approving multiple times
            val isReviewStage = stage == "issuer_review" || stage == "ehs_review" || stage == "area_owner_review"
            val canApprove = when (currentUserRole) {
                Role.ISSUER -> stage == "issuer_review"
                Role.EHS_OFFICER -> stage == "ehs_review"
                Role.AREA_OWNER -> stage == "area_owner_review"
                Role.ADMIN, Role.SUPERVISOR -> isReviewStage
                else -> false
            }

            if (canApprove && onApproveClick != null && onRejectClick != null && onSendBackClick != null) {
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

    fun setCurrentUserRole(role: Role?) {
        this.currentUserRole = role
        notifyDataSetChanged()
    }

    class PermitDiffCallback : DiffUtil.ItemCallback<Permit>() {
        override fun areItemsTheSame(oldItem: Permit, newItem: Permit) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Permit, newItem: Permit) = oldItem == newItem
    }
}