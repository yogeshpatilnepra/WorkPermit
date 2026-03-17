package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.ItemAttachmentBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment

class AttachmentAdapter(
    private val onItemClick: (Attachment) -> Unit
) : RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    private var attachments = listOf<Attachment>()

    fun submitList(list: List<Attachment>) {
        attachments = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val binding =
            ItemAttachmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttachmentViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount() = attachments.size

    class AttachmentViewHolder(
        private val binding: ItemAttachmentBinding,
        private val onItemClick: (Attachment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(attachment: Attachment) {
            binding.tvFileName.text = attachment.fileName
            binding.tvFileSize.text = formatFileSize(attachment.fileSize)

            // Set icon based on file type
            val iconRes = when {
                attachment.fileType.contains("image") -> R.drawable.ic_image
                attachment.fileType.contains("pdf") -> R.drawable.ic_pdf
                else -> R.drawable.ic_file
            }
            binding.ivFileIcon.setImageResource(iconRes)

            binding.root.setOnClickListener { onItemClick(attachment) }
        }

        private fun formatFileSize(size: Long): String {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                else -> "${size / (1024 * 1024)} MB"
            }
        }
    }
}