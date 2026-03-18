package com.apiscall.skeletoncode.solarproject.utility

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatDelegate
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.solarproject.APIS.APIComponent
import com.apiscall.skeletoncode.solarproject.APIS.DaggerAPIComponent
import com.apiscall.skeletoncode.solarproject.APIS.Repository
import com.apiscall.skeletoncode.solarproject.constants.AppConstant
import com.apiscall.skeletoncode.solarproject.videocropping.OnVideoTrimListener
import com.devs.readmoreoption.ReadMoreOption
import com.googlecode.mp4parser.FileDataSourceViaHeapImpl
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack
import io.paperdb.Paper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale

class Utility : Application(), ActivityLifecycleCallbacks {

    lateinit var apiComponent: APIComponent

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        apiComponent = DaggerAPIComponent.builder().build()
        Paper.init(applicationContext)

        if (Paper.book().read(AppConstant.MY_THEME, "")
                .equals("dark")
        ) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        readMoreOption = ReadMoreOption.Builder(this)
            .textLength(25) // OR
            //.textLength(300, ReadMoreOption.TYPE_CHARACTER)
            .moreLabel(" Read More")
            .lessLabel(" Read Less")
            .moreLabelColor(resources.getColor(R.color.primaryColor))
            .lessLabelColor(resources.getColor(R.color.primaryColor))
            .labelUnderLine(false)
            .expandAnimation(true)
            .build()
    }

    fun getResponseFromNEWApi(): Repository {
        return apiComponent.getRepository()
    }

    companion object {

        const val VIDEO_FORMAT = ".mp4"
        var dialogAlert: Dialog? = null
        var dialogInternet: Dialog? = null
        var dialog_loader: Dialog? = null
        val myDecimal = DecimalFormat("0.#")
        var readMoreOption: ReadMoreOption? = null

        fun isNetworkAvaliable(ctx: Context): Boolean {
            var activeNetwork: NetworkInfo? = null
            try {
                val connectivityManager =
                    ctx.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                activeNetwork = connectivityManager.activeNetworkInfo
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return !(activeNetwork == null || !activeNetwork.isConnectedOrConnecting)
        }

        fun dialog_loading(context: Context?) {
            try {
                dialog_loader = Dialog(context!!, R.style.TransparentBackground)
                dialog_loader!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog_loader!!.setContentView(R.layout.single_dialog_loading)
                dialog_loader!!.setCancelable(false)
                dialog_loader!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun dissmis_dialog_loading() {
            try {
                if (dialog_loader != null) dialog_loader!!.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun toast(context: Context?, toastMessage: String) {
            var toastMessage = toastMessage

            if (toastMessage.equals("", ignoreCase = true)) toastMessage =
                "There is issue on server side. Please try again"

            val toast = Toast.makeText(context, toastMessage, Toast.LENGTH_LONG)
            toast.show()
        }

        fun alert_dialog(mContext: Context?, message: String, isSuccess: Boolean) {

            if (dialogAlert != null) dialogAlert!!.dismiss()

            dialogAlert = Dialog(mContext!!)
            dialogAlert!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogAlert!!.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogAlert!!.setContentView(R.layout.dialog_alert)
            dialogAlert!!.setCancelable(false)
            dialogAlert!!.setCanceledOnTouchOutside(false)

            val tvTitle = dialogAlert!!.findViewById<View>(R.id.tvTitle) as TextView
            val tv_msg = dialogAlert!!.findViewById<View>(R.id.tv_msg) as TextView
            val btn_ok = dialogAlert!!.findViewById<View>(R.id.btn_ok) as Button
            val ivAlert = dialogAlert!!.findViewById<View>(R.id.ivAlert) as ImageView
            val ivSuccess = dialogAlert!!.findViewById<View>(R.id.ivSuccess) as ImageView

            if (isSuccess) {
                tvTitle.text = mContext.resources.getString(R.string.success)
                ivAlert.visibility = View.GONE
                ivSuccess.visibility = View.VISIBLE
            } else {
                ivAlert.visibility = View.VISIBLE
                ivSuccess.visibility = View.GONE
            }
            if (message.equals("", ignoreCase = true)) tv_msg.text =
                mContext.resources.getString(R.string.something_went)
            else tv_msg.text = message

            btn_ok.setOnClickListener {
                dialogAlert!!.dismiss()
            }

            val window: Window? = dialogAlert!!.getWindow()
            val wlp = window!!.attributes
            wlp!!.width = ActionBar.LayoutParams.MATCH_PARENT
            window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window!!.attributes = wlp

            if (!dialogAlert!!.isShowing()) dialogAlert!!.show()
        }

        fun internet_dialog(mContext: Context?) {

            if (dialogInternet != null) dialogInternet!!.dismiss()

            dialogInternet = Dialog(mContext!!)
            dialogInternet!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogInternet!!.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogInternet!!.setContentView(R.layout.dialog_internet)
            dialogInternet!!.setCancelable(false)
            dialogInternet!!.setCanceledOnTouchOutside(false)

            val btnRetry = dialogInternet!!.findViewById<View>(R.id.btnRetry) as Button

            btnRetry.setOnClickListener {
                dialogInternet!!.dismiss()
            }

            val window: Window? = dialogInternet!!.getWindow()
            val wlp = window!!.attributes
            wlp!!.width = ActionBar.LayoutParams.MATCH_PARENT
            window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window!!.attributes = wlp

            if (!dialogInternet!!.isShowing()) dialogInternet!!.show()
        }


        fun setTypeOfDate(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
            var strMonth: String = "" + (monthOfYear + 1)
            var strDay: String = "" + (dayOfMonth)
            if (strMonth.length == 1) {
                strMonth = "0$strMonth"
            }
            if (strDay.length == 1) {
                strDay = "0$strDay"
            }
            return "$strDay-$strMonth-$year"
        }

        fun getCurrentDate(): String {
            val today = Date()
            var format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return format.format(today)
        }

        fun convertDateToMili(inputDate: String, inputPattern: String): Long {
            val sdf = SimpleDateFormat(inputPattern)
            var timeInMilliseconds: Long = 0
            try {
                val mDate = sdf.parse(inputDate)
                timeInMilliseconds = mDate.time
            } catch (e: Exception) {
                Log.e("exception", e.localizedMessage)
            }
            return timeInMilliseconds
        }

        fun getTimeStamp(): String? {
            val dateFormat = SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault())
            val date = Date()
            return dateFormat.format(date)
        }

        fun isEmailValid(str: String): Boolean {
            return !Patterns.EMAIL_ADDRESS.matcher(str).matches()
        }

        fun openDatePicker(
            context: Context, editText: EditText, minDate: Long, maxDate: Long, outputFormat: String
        ) {
            val calendar: Calendar = Calendar.getInstance()
            val selectedDate: String = editText.text.toString()

            if (!selectedDate.isNullOrEmpty()) {
                var selectedMilli = convertDateToMili(selectedDate, outputFormat)
                calendar.timeInMillis = selectedMilli
            }

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            val datePickerDialog = DatePickerDialog(context, { _, year, monthOfYear, dayOfMonth ->
                val finalDate = setTypeOfDate(year, monthOfYear, dayOfMonth)
                editText.setText(changeDateFormat(finalDate, "dd-MM-yyyy", outputFormat))
            }, year, month, day)

            if (!minDate.toString().equals("0")) datePickerDialog.datePicker.minDate = minDate

            if (!maxDate.toString().equals("0") && !maxDate.toString()
                    .equals("")
            ) datePickerDialog.datePicker.maxDate = maxDate

            datePickerDialog.show()
        }

        fun changeDateFormat(
            inputDate: String, inputPattern: String, outputPattern: String
        ): String {
            val inputFormat = SimpleDateFormat(inputPattern)
            val outputFormat = SimpleDateFormat(outputPattern)

            var date: Date? = null
            var str: String? = null
            try {
                date = inputFormat.parse(inputDate)
                str = outputFormat.format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return str!!
        }

        fun getStringValue(value: Any?): String {
            var newStr: String = ""
            if (value == null) newStr = ""
            else if (value == "") newStr = ""
            else if (value == "null") newStr = ""
            else if (value == "0") newStr = ""
            else newStr = value.toString()

            return newStr
        }

        fun getStringDashValue(value: Any?): String {
            var newStr: String = ""
            if (value == null) newStr = ""
            else if (value == "") newStr = ""
            else if (value == "null") newStr = ""
            else if (value == "0") newStr = ""
            else newStr = value.toString()

            if (newStr.equals("")) newStr = "-"

            return newStr
        }

        fun getIntValue(value: Any?): Int {
            var newInt: Int = 0
            if (value == null) newInt = 0
            else if (value.toString().equals("")) newInt = 0
            else if (value == 0.0) newInt = 0
            else {
                if (value.toString().contains(".")) {
                    val split = value.toString().split(".")
                    newInt = split[0].toInt()
                } else {
                    newInt = value.toString().toInt()
                }
            }
            return newInt
        }

        fun getDoubleValue(value: Any?): Double {
            var newInt: Double = 0.0
            if (value == null) newInt = 0.0
            else if (value.toString().equals("")) newInt = 0.0
            else newInt = value.toString().toDouble()

            return newInt
        }

        fun getBoolean(value: Any?): Boolean {
            var newStr: Boolean = false
            if (value == null) newStr = false
            else if (value == "") newStr = false
            else if (value == "null") newStr = false
            else if (value == "0") newStr = false
            else if (value == "0.0") newStr = false
            else if (value == 0) newStr = false
            else if (value == 0.0) newStr = false
            else if (value == "false") newStr = false
            else if (value == false) newStr = false
            else if (value == "true") newStr = true
            else if (value == "1") newStr = true
            else if (value == "1.0") newStr = true
            else if (value == 1) newStr = true
            else if (value == 1.0) newStr = true
            else newStr = true

            return newStr
        }

        fun isNightMode(context: Context): Boolean {
            val nightModeFlags =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }

        //---------Video Croppr

        @Throws(IOException::class)
        fun startTrim(
            activity: Activity,
            src: File,
            dst: String,
            startMs: Long,
            endMs: Long,
            callback: OnVideoTrimListener
        ) {
            val file1 = create(activity, dst)
            file1?.let { generateVideo(src, it, startMs, endMs, callback) }
        }

        @Throws(IOException::class)
        private fun generateVideo(
            src: File, dst: File, startMs: Long, endMs: Long, callback: OnVideoTrimListener
        ) {
            val movie = MovieCreator.build(FileDataSourceViaHeapImpl(src.absolutePath))
            val tracks = movie.tracks
            movie.tracks = LinkedList()
            var startTime1 = (startMs / 1000).toDouble()
            var endTime1 = (endMs / 1000).toDouble()
            var timeCorrected = false
            for (track in tracks) {
                if (track.syncSamples != null && track.syncSamples.size > 0) {
                    if (timeCorrected) {
                        throw RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.")
                    }
                    startTime1 = correctTimeToSyncSample(track, startTime1, false)
                    endTime1 = correctTimeToSyncSample(track, endTime1, true)
                    timeCorrected = true
                }
            }
            for (track in tracks) {
                var currentSample: Long = 0
                var currentTime = 0.0
                var lastTime = -1.0
                var startSample1: Long = -1
                var endSample1: Long = -1
                for (i in track.sampleDurations.indices) {
                    val delta = track.sampleDurations[i]
                    if (currentTime > lastTime && currentTime <= startTime1) startSample1 =
                        currentSample

                    if (currentTime > lastTime && currentTime <= endTime1) endSample1 =
                        currentSample

                    lastTime = currentTime
                    currentTime += delta.toDouble() / track.trackMetaData.timescale.toDouble()
                    currentSample++
                }
                movie.addTrack(AppendTrack(CroppedTrack(track, startSample1, endSample1)))
            }
            val out = DefaultMp4Builder().build(movie)
            try {
                val fos = FileOutputStream(dst)
                val fc = fos.channel
                out.writeContainer(fc)
                fc.close()
                fos.close()
                if (callback != null) callback.getResult(Uri.parse(dst.toString()))
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }

        private fun correctTimeToSyncSample(track: Track, cutHere: Double, next: Boolean): Double {
            val timeOfSyncSamples = DoubleArray(track.syncSamples.size)
            var currentSample: Long = 0
            var currentTime = 0.0
            for (i in track.sampleDurations.indices) {
                val delta = track.sampleDurations[i]
                if (Arrays.binarySearch(track.syncSamples, currentSample + 1) >= 0) {
                    timeOfSyncSamples[Arrays.binarySearch(track.syncSamples, currentSample + 1)] =
                        currentTime
                }
                currentTime += delta.toDouble() / track.trackMetaData.timescale.toDouble()
                currentSample++
            }
            var previous = 0.0
            for (timeOfSyncSample in timeOfSyncSamples) {
                if (timeOfSyncSample > cutHere) {
                    return if (next) {
                        timeOfSyncSample
                    } else {
                        previous
                    }
                }
                previous = timeOfSyncSample
            }
            return timeOfSyncSamples[timeOfSyncSamples.size - 1]
        }


        private fun create(activity: Activity, dst: String): File? {
            val file = File(dst)
            file.parentFile.mkdirs()
            val storageDir = activity.getExternalFilesDir(file.parentFile.absolutePath)
            try {
                return File.createTempFile(
                    activity.resources.getString(R.string.app_name) + Date().time,  /* prefix */
                    ".mp4",  /* suffix */
                    storageDir /* directory */
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        fun maskMobile(phoneNumber: String): String {
            if (phoneNumber.length < 4) return phoneNumber

            val maskedDigits = "*".repeat(phoneNumber.length - 4)
            val lastFourDigits = phoneNumber.substring(phoneNumber.length - 4)
            return "$maskedDigits$lastFourDigits"
        }

        fun logout(mContext: Context?) {
            val lastLoginType = Paper.book().read(AppConstant.LOGIN_TYPE, "customer")

            Paper.book().delete(AppConstant.ACCESS_TOKEN)
            Paper.book().delete(AppConstant.CUSTOMER_LOGIN_RESPONSE)
            Paper.book().delete(AppConstant.COMPANY_LOGIN_RESPONSE)
            Paper.book().delete(AppConstant.LOGIN_TYPE)

            if (lastLoginType.equals("customer")) {
                Log.d("LOGINNNNTYPPPE", "customer ")
//                val intent = Intent(mContext!!, CustomerLoginActivity::class.java)
//                val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                intent.flags = flags
//                mContext!!.startActivity(intent)
            } else {
                Log.d("LOGINNNNTYPPPE", "company ")
//                val intent = Intent(mContext!!, CompanyLoginActivity::class.java)
//                val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                intent.flags = flags
//                mContext!!.startActivity(intent)
            }

        }

        //        CharSequence text, DialogInterface.OnClickListener listener
//        fun AlertDialogWithButtons(mContext: Context?, title: String, message: CharSequence, text: Any, listener: DialogInterface.OnClickListener, negativeButton: (name: String) -> Unit) {
//            val builder = android.app.AlertDialog.Builder(mContext)
//            builder.setTitle(title)
//            builder.setMessage(message)
//            builder.setPositiveButton(text) {
//                listener
//            }
//        }
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {

    }

    override fun onActivityStarted(p0: Activity) {

    }

    override fun onActivityResumed(p0: Activity) {


    }

    override fun onActivityPaused(p0: Activity) {

    }

    override fun onActivityStopped(p0: Activity) {

    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

    }

    override fun onActivityDestroyed(p0: Activity) {

    }


}