package com.apiscall.skeletoncode.workpermitmodule.presentation.home.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.ItemQuickActionBinding

enum class QuickAction(val title: String, val iconRes: Int) {
    NEW_PERMIT("New Permit", R.drawable.ic_new_permit),
    SCAN_QR("Scan QR", R.drawable.ic_qr_code),
    SEARCH("Search", R.drawable.ic_search),
    SYNC("Sync", R.drawable.ic_sync)
}

class QuickActionAdapter(
    private val onItemClick: (QuickAction) -> Unit
) : RecyclerView.Adapter<QuickActionAdapter.QuickActionViewHolder>() {

    private val actions = QuickAction.values()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickActionViewHolder {
        val binding = ItemQuickActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuickActionViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: QuickActionViewHolder, position: Int) {
        holder.bind(actions[position])
    }

    override fun getItemCount() = actions.size

    class QuickActionViewHolder(
        private val binding: ItemQuickActionBinding,
        private val onItemClick: (QuickAction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(action: QuickAction) {
            binding.tvActionTitle.text = action.title
            binding.ivActionIcon.setImageResource(action.iconRes)
            binding.root.setOnClickListener { onItemClick(action) }
        }
    }
}