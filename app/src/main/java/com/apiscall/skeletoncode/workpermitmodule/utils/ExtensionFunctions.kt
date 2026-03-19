package com.apiscall.skeletoncode.workpermitmodule.utils

import android.view.View
import android.widget.ImageView
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// View Extensions
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

// ImageView Extensions
fun ImageView.loadImage(url: String?) {
    if (!url.isNullOrEmpty()) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .circleCrop()
            .into(this)
    } else {
        setImageResource(R.drawable.ic_profile_placeholder)
    }
}

// Date Extensions
fun Date.formatDate(pattern: String = Constants.DATE_FORMAT_DISPLAY): String {
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(this)
}

fun Date.formatTime(): String {
    return formatDate(Constants.TIME_FORMAT)
}

fun Date.formatFull(): String {
    return formatDate(Constants.DATE_FORMAT_FULL)
}

// PermitStatus Extensions
fun getStatusText(status: String): String {
    return when (status.uppercase()) {
        "DRAFT" -> "Draft"
        "PENDING_ISSUER_APPROVAL" -> "Issuer Review"
        "PENDING_AREA_OWNER_APPROVAL" -> "Area Owner Review"
        "PENDING_EHS_APPROVAL" -> "EHS Review"
        "APPROVED" -> "Issued"
        "ACTIVE" -> "In Progress"
        "CLOSED" -> "Closed"
        "REJECTED" -> "Rejected"
        "EXPIRED" -> "Expired"
        else -> status.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}

fun getStatusColor(status: String): Int {
    return when (status.uppercase()) {
        "DRAFT" -> R.drawable.bg_status_draft
        "PENDING_ISSUER_APPROVAL", "PENDING_AREA_OWNER_APPROVAL", "PENDING_EHS_APPROVAL" ->
            R.drawable.bg_status_pending

        "APPROVED" -> R.drawable.bg_status_approved
        "ACTIVE" -> R.drawable.bg_status_active
        "CLOSED" -> R.drawable.bg_status_closed
        "REJECTED" -> R.drawable.bg_status_rejected
        "EXPIRED" -> R.drawable.bg_status_expired
        else -> R.drawable.bg_status_draft
    }
}

fun PermitStatus.getStatusText(): String {
    return getStatusText(this.name)
}

fun PermitStatus.getStatusColor(): Int {
    return getStatusColor(this.name)
}