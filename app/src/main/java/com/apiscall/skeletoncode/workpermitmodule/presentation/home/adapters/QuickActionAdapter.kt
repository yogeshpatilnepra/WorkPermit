package com.apiscall.skeletoncode.workpermitmodule.presentation.home.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.ItemQuickActionBinding

enum class QuickAction(val title: String, val iconRes: Int) {
    NEW_PERMIT("New Permit", R.drawable.ic_add),
    SEARCH("Search", R.drawable.ic_search),
    SYNC("Sync", R.drawable.ic_sync)
}

class QuickActionAdapter(
    private val onItemClick: (QuickAction) -> Unit
) : RecyclerView.Adapter<QuickActionAdapter.QuickActionViewHolder>() {

    private var showNewPermit = true
    private val allActions = listOf(QuickAction.NEW_PERMIT, QuickAction.SEARCH, QuickAction.SYNC)

    fun updateNewPermitVisibility(show: Boolean) {
        showNewPermit = show
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickActionViewHolder {
        val binding =
            ItemQuickActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuickActionViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: QuickActionViewHolder, position: Int) {
        val action = allActions[position]
        // Skip NEW_PERMIT if not allowed - make it gone and reduce space
        if (action == QuickAction.NEW_PERMIT && !showNewPermit) {
            holder.itemView.visibility = View.GONE
            // Set layout params to 0 width to remove space
            val params = holder.itemView.layoutParams
            params.width = 0
            holder.itemView.layoutParams = params
        } else {
            holder.itemView.visibility = View.VISIBLE
            // Reset width to wrap_content
            val params = holder.itemView.layoutParams
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.itemView.layoutParams = params
            holder.bind(action)
        }
    }

    override fun getItemCount() = allActions.size

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