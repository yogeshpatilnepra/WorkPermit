package com.apiscall.skeletoncode.solarproject.activities

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.apiscall.skeletoncode.BuildConfig
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.solarproject.constants.AppConstant
import com.apiscall.skeletoncode.databinding.ActivitySpleshBinding
import com.apiscall.skeletoncode.databinding.DialogVersionNotifyBinding
import com.apiscall.skeletoncode.solarproject.utility.Utility
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import io.paperdb.Paper
import java.util.Locale

class SpleshActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var mBinder: ActivitySpleshBinding
    private var versionList: ArrayList<String> = ArrayList()
    var dialogVersion: Dialog? = null

    var updateType = ""
    lateinit var appUpdateManager: AppUpdateManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this@SpleshActivity)
        initUI()
    }


    private fun initUI() {
        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_splesh)


        changeLanguage()
        getFirebaseToken()
    }

    fun manageAPIS() {

        var login_type = Paper.book().read(AppConstant.LOGIN_TYPE, "")

        if (login_type.equals("customer")) {
            //----- auto login-------
        } else if (login_type.equals("company")) {

        } else {
        }

    }

    override fun onClick(view: View?) {

    }

    private fun changeMyActivity(activity: Class<*>) {

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, activity)
            this.startActivity(intent)
            finish()
        }, 2000)
    }

    fun changeLanguage() {
        try {
            var locale = Locale("en")

            if (Paper.book().read(AppConstant.MY_LANGUAGE, "").equals("hi"))
                locale = Locale("hi")
            else if (Paper.book().read(AppConstant.MY_LANGUAGE, "").equals("gu"))
                locale = Locale("gu")
            else
                locale = Locale("en")

            Locale.setDefault(locale)
            val resources: Resources = resources
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFirebaseToken() {

        val appName = "Nepra Solar"

        val existingApp = try {
            FirebaseApp.getInstance(appName)
        } catch (e: IllegalStateException) {
            null
        }

        if (existingApp == null) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:1006609464859:android:cbc01f6ec7aa8d0d4b42be") // Required for Analytics.
                .setProjectId("nepra-solar") // Required for Firebase Installations.
                .setApiKey("AIzaSyDd97MKQ792rzr5Mtj5DuA6EvdSxATfe4U") // Required for Auth.
                .build()
            FirebaseApp.initializeApp(this, options, "Nepra Solar")
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            Paper.book().write(AppConstant.FCM_TOKEN, task.result)
        })
    }

    fun version_dialog(updateType: String) {

        if (dialogVersion != null)
            dialogVersion!!.dismiss()

        val mBinderDialog: DialogVersionNotifyBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_version_notify,
            null,
            false
        )

        dialogVersion = Dialog(this)
        dialogVersion!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogVersion!!.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogVersion!!.setContentView(mBinderDialog.root)
        dialogVersion!!.setCancelable(false)
        dialogVersion!!.setCanceledOnTouchOutside(false)

        if (updateType.equals("force"))
            mBinderDialog.tvSkip.visibility = View.GONE
        else
            mBinderDialog.tvSkip.visibility = View.VISIBLE

        mBinderDialog.tvCurrentVersion.text = Html.fromHtml(BuildConfig.VERSION_NAME)
        mBinderDialog.tvNewVersion.text = Html.fromHtml(versionList[(versionList.size) - 1])

        mBinderDialog.btnUpdate.setOnClickListener {
            dialogVersion!!.dismiss()
            checkUpdates()
            /*val urlWeb = "http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

            val i = Intent(Intent.ACTION_VIEW, Uri.parse(urlWeb))
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            startActivity(i)
            finish()*/
        }

        mBinderDialog.tvSkip.setOnClickListener {
            dialogVersion!!.dismiss()
            manageAPIS()
        }

        mBinderDialog.btnDismiss.setOnClickListener {
            dialogVersion!!.dismiss()
            finish()
        }

        val window: Window? = dialogVersion!!.getWindow()
        val wlp = window!!.attributes
        wlp!!.width = ActionBar.LayoutParams.MATCH_PARENT
        window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window!!.attributes = wlp

        if (!dialogVersion!!.isShowing()) dialogVersion!!.show()
    }


    private fun checkUpdates() {
        try {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                        AppUpdateType.IMMEDIATE
                    )
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        1001
                    )
                } else {
                    manageAPIS()
                }
            }
            appUpdateManager.registerListener(listener)
        } catch (e: Exception) {
            e.printStackTrace()
            Utility.toast(this, e.toString())
        }
    }

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADING) {

        } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
            appUpdateManager.completeUpdate()
        } else if (state.installStatus() == InstallStatus.INSTALLED) {
            manageAPIS()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            when (resultCode) {
                RESULT_OK -> {

                }

                RESULT_CANCELED -> {
                    if (updateType == "force")
                        finish()
                    else
                        manageAPIS()
                }

                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    if (updateType == "force") {
                        Utility.toast(this, "Update Failed")
                        finish()
                    } else
                        manageAPIS()

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(listener)
    }
}