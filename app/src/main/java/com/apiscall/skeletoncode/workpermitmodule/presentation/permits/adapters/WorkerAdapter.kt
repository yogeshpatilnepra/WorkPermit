package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.databinding.ItemWorkerBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.WorkerSignIn
import com.apiscall.skeletoncode.workpermitmodule.utils.formatFull

class WorkerAdapter(
    private val onSignOutClick: (String) -> Unit
) : ListAdapter<WorkerSignIn, WorkerAdapter.WorkerViewHolder>(WorkerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val binding = ItemWorkerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkerViewHolder(private val binding: ItemWorkerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(worker: WorkerSignIn) {
            binding.tvWorkerName.text = worker.name
            binding.tvSignInTime.text = "Signed in: ${worker.signInAt.formatFull()}"
            
            if (worker.signOutAt != null) {
                binding.btnSignOut.visibility = View.GONE
                binding.tvSignOutTime.visibility = View.VISIBLE
                binding.tvSignOutTime.text = "Signed out: ${worker.signOutAt.formatFull()}"
            } else {
                binding.btnSignOut.visibility = View.VISIBLE
                binding.tvSignOutTime.visibility = View.GONE
                binding.btnSignOut.setOnClickListener {
                    onSignOutClick(worker.id)
                }
            }
        }
    }

    class WorkerDiffCallback : DiffUtil.ItemCallback<WorkerSignIn>() {
        override fun areItemsTheSame(oldItem: WorkerSignIn, newItem: WorkerSignIn): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkerSignIn, newItem: WorkerSignIn): Boolean {
            return oldItem == newItem
        }
    }
}
