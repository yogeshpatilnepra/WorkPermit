package com.apiscall.skeletoncode.workpermitmodule.presentation.notifications


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.ItemNotificationBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Notification
import com.apiscall.skeletoncode.workpermitmodule.domain.models.NotificationType
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onItemClick: (Notification) -> Unit
) : ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding, onItemClick, dateFormat)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
        private val onItemClick: (Notification) -> Unit,
        private val dateFormat: SimpleDateFormat
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.tvTitle.text = notification.title
            binding.tvMessage.text = notification.message
            binding.tvTime.text = dateFormat.format(notification.createdAt)

            // Set unread indicator
            if (!notification.isRead) {
                binding.viewUnread.visibility = android.view.View.VISIBLE
                binding.root.setBackgroundColor(0x0F1E3A8A) // Light blue tint
            } else {
                binding.viewUnread.visibility = android.view.View.GONE
                binding.root.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }

            // Set icon based on notification type
            val iconRes = when (notification.type) {
                NotificationType.PERMIT_APPROVAL -> R.drawable.ic_check_circle
                NotificationType.PERMIT_REJECTION -> R.drawable.ic_cancel
                NotificationType.PERMIT_EXPIRING -> R.drawable.ic_warning
                NotificationType.NEW_PERMIT_REQUEST -> R.drawable.ic_new_permit
                else -> R.drawable.ic_notifications
            }
            binding.ivIcon.setImageResource(iconRes)

            binding.root.setOnClickListener { onItemClick(notification) }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification) =
            oldItem == newItem
    }
}