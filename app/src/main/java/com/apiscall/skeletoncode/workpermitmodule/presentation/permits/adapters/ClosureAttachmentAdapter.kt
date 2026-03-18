package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.ItemClosureAttachmentBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import java.text.SimpleDateFormat
import java.util.Locale

class ClosureAttachmentAdapter(
    private val onItemClick: (Attachment) -> Unit,
    private val onDeleteClick: (Attachment) -> Unit
) : ListAdapter<Attachment, ClosureAttachmentAdapter.AttachmentViewHolder>(AttachmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val binding =
            ItemClosureAttachmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttachmentViewHolder(binding, onItemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AttachmentViewHolder(
        private val binding: ItemClosureAttachmentBinding,
        private val onItemClick: (Attachment) -> Unit,
        private val onDeleteClick: (Attachment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())

        fun bind(attachment: Attachment) {
            binding.tvFileName.text = attachment.fileName
            binding.tvFileSize.text = formatFileSize(attachment.fileSize)
            binding.tvUploadedAt.text = dateFormat.format(attachment.uploadedAt)

            // Set icon based on file type
            val iconRes = when {
                attachment.fileType.contains("image") -> R.drawable.ic_image
                attachment.fileType.contains("pdf") -> R.drawable.ic_pdf
                attachment.fileType.contains("doc") || attachment.fileType.contains("document") -> R.drawable.ic_document
                else -> R.drawable.ic_file
            }
            binding.ivFileIcon.setImageResource(iconRes)

            binding.root.setOnClickListener { onItemClick(attachment) }
            binding.btnDelete.setOnClickListener { onDeleteClick(attachment) }
        }

        private fun formatFileSize(size: Long): String {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
                else -> "${size / (1024 * 1024 * 1024)} GB"
            }
        }
    }

    class AttachmentDiffCallback : DiffUtil.ItemCallback<Attachment>() {
        override fun areItemsTheSame(oldItem: Attachment, newItem: Attachment) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Attachment, newItem: Attachment) =
            oldItem == newItem
    }
}