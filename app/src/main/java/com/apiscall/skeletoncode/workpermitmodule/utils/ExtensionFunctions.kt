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
fun PermitStatus.getStatusText(): String {
    return when (this) {
        PermitStatus.DRAFT -> "Draft"
        PermitStatus.PENDING_ISSUER_APPROVAL,
        PermitStatus.PENDING_AREA_OWNER_APPROVAL,
        PermitStatus.PENDING_EHS_APPROVAL -> "Pending"

        PermitStatus.APPROVED -> "Approved"
        PermitStatus.REJECTED -> "Rejected"
        PermitStatus.ACTIVE -> "Active"
        PermitStatus.CLOSED -> "Closed"
        PermitStatus.EXPIRED -> "Expired"
    }
}

fun PermitStatus.getStatusColor(): Int {
    return when (this) {
        PermitStatus.DRAFT -> R.drawable.bg_status_draft
        PermitStatus.PENDING_ISSUER_APPROVAL,
        PermitStatus.PENDING_AREA_OWNER_APPROVAL,
        PermitStatus.PENDING_EHS_APPROVAL -> R.drawable.bg_status_pending

        PermitStatus.APPROVED -> R.drawable.bg_status_approved
        PermitStatus.REJECTED -> R.drawable.bg_status_rejected
        PermitStatus.ACTIVE -> R.drawable.bg_status_active
        PermitStatus.CLOSED, PermitStatus.EXPIRED -> R.drawable.bg_status_closed
    }
}