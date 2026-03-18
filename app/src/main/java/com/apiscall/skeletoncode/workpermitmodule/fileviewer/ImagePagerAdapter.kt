package com.apiscall.skeletoncode.workpermitmodule.fileviewer


import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apiscall.skeletoncode.databinding.ItemImagePagerBinding
import com.github.chrisbanes.photoview.PhotoViewAttacher

class ImagePagerAdapter(
    private val context: Context,
    private val imagePaths: List<String>
) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImagePagerBinding.inflate(LayoutInflater.from(context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imagePaths[position])
    }

    override fun getItemCount() = imagePaths.size

    class ImageViewHolder(
        private val binding: ItemImagePagerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var photoViewAttacher: PhotoViewAttacher? = null

        fun bind(imagePath: String) {
            try {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                binding.ivImage.setImageBitmap(bitmap)

                // Setup PhotoView attacher for zooming
                photoViewAttacher = PhotoViewAttacher(binding.ivImage)
                photoViewAttacher?.update()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}