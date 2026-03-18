package com.apiscall.skeletoncode.workpermitmodule.fileviewer


import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.apiscall.skeletoncode.databinding.ActivityFileViewerBinding
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.github.chrisbanes.photoview.PhotoView
import java.io.File


class FileViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileViewerBinding
    private var currentPosition = 0
    private val imagePaths = ArrayList<String>()
    private var fileName: String = "File"

    companion object {
        private const val EXTRA_FILE_PATH = "extra_file_path"
        private const val EXTRA_FILE_TYPE = "extra_file_type"
        private const val EXTRA_FILE_NAME = "extra_file_name"
        private const val EXTRA_IMAGE_PATHS = "extra_image_paths"
        private const val EXTRA_CURRENT_POSITION = "extra_current_position"

        fun openFile(context: Context, filePath: String, fileType: String, fileName: String) {
            val intent = Intent(context, FileViewerActivity::class.java).apply {
                putExtra(EXTRA_FILE_PATH, filePath)
                putExtra(EXTRA_FILE_TYPE, fileType)
                putExtra(EXTRA_FILE_NAME, fileName)
            }
            context.startActivity(intent)
        }

        fun openImages(context: Context, imagePaths: ArrayList<String>, currentPosition: Int) {
            val intent = Intent(context, FileViewerActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_PATHS, imagePaths)
                putExtra(EXTRA_CURRENT_POSITION, currentPosition)
                putExtra(EXTRA_FILE_TYPE, "image/multiple")
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        handleIntent()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleIntent() {
        when {
            intent.hasExtra(EXTRA_IMAGE_PATHS) -> {
                // Multiple images
                val paths = intent.getStringArrayListExtra(EXTRA_IMAGE_PATHS) ?: return
                val position = intent.getIntExtra(EXTRA_CURRENT_POSITION, 0)
                supportActionBar?.title = "Images (${position + 1}/${paths.size})"
                showImageSlider(paths, position)
            }

            intent.hasExtra(EXTRA_FILE_PATH) -> {
                val filePath = intent.getStringExtra(EXTRA_FILE_PATH) ?: return
                val fileType = intent.getStringExtra(EXTRA_FILE_TYPE) ?: return
                fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: "File"

                supportActionBar?.title = fileName

                when {
                    fileType.contains("image") -> showSingleImage(filePath)
                    fileType.contains("pdf") -> showPdf(filePath)
                    else -> showTextFile(filePath)
                }
            }
        }
    }

    private fun showImageSlider(paths: ArrayList<String>, startPosition: Int) {
        binding.viewPager.visibility = View.VISIBLE
        binding.pdfView.visibility = View.GONE
        binding.textView.visibility = View.GONE
        binding.pageIndicator.visibility = View.VISIBLE

        val adapter = ImagePagerAdapter(this, paths)
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(startPosition, false)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                supportActionBar?.title = "Images (${position + 1}/${paths.size})"
                currentPosition = position
                binding.pageIndicator.text = "${position + 1}/${paths.size}"
            }
        })

        binding.pageIndicator.text = "${startPosition + 1}/${paths.size}"
    }

    private fun showSingleImage(filePath: String) {
        binding.viewPager.visibility = View.VISIBLE
        binding.pdfView.visibility = View.GONE
        binding.textView.visibility = View.GONE
        binding.pageIndicator.visibility = View.GONE

        binding.viewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val photoView = PhotoView(parent.context)
                photoView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                return object : RecyclerView.ViewHolder(photoView) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val photoView = holder.itemView as PhotoView
                val bitmap = BitmapFactory.decodeFile(filePath)
                photoView.setImageBitmap(bitmap)
            }

            override fun getItemCount() = 1
        }
    }

    private fun showPdf(filePath: String) {
        binding.viewPager.visibility = View.GONE
        binding.pdfView.visibility = View.VISIBLE
        binding.textView.visibility = View.GONE
        binding.pageIndicator.visibility = View.GONE

        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(this, "PDF file not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // Load PDF with proper rendering
            binding.pdfView.fromFile(file)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .enableAnnotationRendering(true)
                .password(null)
                .scrollHandle(DefaultScrollHandle(this))
                .spacing(10) // in dp
                .pageFitPolicy(FitPolicy.WIDTH) // width matches parent, height adaptive
                .fitEachPage(true) // fit each page to view, better for reading
                .nightMode(false) // toggle night mode
                .enableAntialiasing(true) // improve rendering quality
                .onLoad { nbPages ->
                    supportActionBar?.title = "$fileName (1/$nbPages)"
                    binding.pageIndicator.visibility = View.VISIBLE
                    binding.pageIndicator.text = "1/$nbPages"
                }
                .onPageChange { page, pageCount ->
                    supportActionBar?.title = "$fileName (${page + 1}/$pageCount)"
                    binding.pageIndicator.text = "${page + 1}/$pageCount"
                }
                .onError { t ->
                    Toast.makeText(this, "Error loading PDF: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
                .load()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error opening PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showTextFile(filePath: String) {
        binding.viewPager.visibility = View.GONE
        binding.pdfView.visibility = View.GONE
        binding.textView.visibility = View.VISIBLE
        binding.pageIndicator.visibility = View.GONE

        try {
            val file = File(filePath)
            val content = file.readText()
            binding.textView.text = content
        } catch (e: Exception) {
            binding.textView.text = "Error reading file: ${e.message}"
        }
    }
}