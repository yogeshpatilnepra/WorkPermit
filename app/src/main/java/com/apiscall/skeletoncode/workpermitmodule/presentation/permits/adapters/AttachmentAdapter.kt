package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters


import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.ItemAttachmentBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

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

        private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

        fun bind(attachment: Attachment) {
            binding.tvFileName.text = attachment.fileName
            binding.tvFileSize.text = formatFileSize(attachment.fileSize)
            binding.tvUploadedBy.text = "Uploaded by: ${attachment.uploadedBy.fullName}"
            binding.tvUploadedAt.text = dateFormat.format(attachment.uploadedAt)

            // Set icon based on file type
            val iconRes = when {
                attachment.fileType.contains("image") -> R.drawable.ic_image
                attachment.fileType.contains("pdf") -> R.drawable.ic_pdf
                attachment.fileType.contains("doc") || attachment.fileType.contains("document") -> R.drawable.ic_document
                else -> R.drawable.ic_file
            }
            binding.ivFileIcon.setImageResource(iconRes)

            // If it's an image, try to load thumbnail from local file
            if (attachment.fileType.contains("image")) {
                val file = File(attachment.filePath)
                if (file.exists()) {
                    Glide.with(binding.root.context)
                        .load(file)
                        .placeholder(iconRes)
                        .error(iconRes)
                        .centerCrop()
                        .into(binding.ivFileIcon)
                }
            }

            binding.root.setOnClickListener {
                // Open file with system viewer
                val context = binding.root.context
                val file = File(attachment.filePath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, attachment.fileType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open File"))
                } else {
                    onItemClick(attachment)
                }
            }
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
}