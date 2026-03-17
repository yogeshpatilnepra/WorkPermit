package com.apiscall.skeletoncode.solarproject.APIS

import android.content.Context
import android.os.Build
import com.apiscall.skeletoncode.BuildConfig
import com.apiscall.skeletoncode.solarproject.constants.AppConstant
import com.apiscall.skeletoncode.solarproject.model.MobileInfo
import com.apiscall.skeletoncode.solarproject.model.ServiceResponse
import com.apiscall.skeletoncode.solarproject.utility.SHA256
import com.apiscall.skeletoncode.solarproject.utility.Utility
import com.google.gson.Gson
import com.nepra.solar.APIS.DataProviderResponse
import com.nepra.solar.APIS.DataProviderStrResponse
import io.paperdb.Paper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import kotlin.collections.iterator

class Repository @Inject constructor(private val apiService: APIService) {

    private var responseListener: DataProviderResponse? = null
    private var responseStrListener: DataProviderStrResponse? = null

    fun setResponseListener(responseListener: DataProviderResponse) {
        this.responseListener = responseListener
    }

    fun setResponseListenerStr(responseListener: DataProviderStrResponse) {
        this.responseStrListener = responseListener
    }

    fun callRxApi(
        isLoaderRequired: Boolean,
        url: String,
        request: HashMap<String, String>,
        mContext: Context
    ) {
        if (Utility.isNetworkAvaliable(mContext)) {

            if (isLoaderRequired)
                Utility.dialog_loading(mContext)

            apiService.APICallPostForm(url, getHeaderParam(request), request)
                .enqueue(object : Callback<ServiceResponse> {
                    override fun onResponse(
                        call: Call<ServiceResponse>,
                        response: Response<ServiceResponse>
                    ) {
                        if (response.isSuccessful) {
                            Utility.dissmis_dialog_loading()
                            response.body()?.let { responseListener!!.onDataProviderResult(it) }
                        } else {
                            Utility.dissmis_dialog_loading()
                            handleError(response.errorBody(), mContext)
                        }
                    }

                    override fun onFailure(call: Call<ServiceResponse>, t: Throwable) {
                        Utility.dissmis_dialog_loading()
                        failureResponse()
                    }
                })
        } else
            noInternetResponse()
    }

    fun callRxApiWithMultipleImage(
        isLoaderRequired: Boolean,
        url: String,
        imageList: HashMap<String, String>,
        request: HashMap<String, String>,
        mContext: Context
    ) {

        if (Utility.isNetworkAvaliable(mContext)) {

            if (isLoaderRequired)
                Utility.dialog_loading(mContext)

            val mapImageList: ArrayList<MultipartBody.Part> = ArrayList()
            for ((key, value) in imageList) {
                val file = File(imageList[key]!!)
                val fileReqBody: RequestBody =
                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData(key, file.name, fileReqBody)
                mapImageList.add(part)
            }

            val mapRequest: LinkedHashMap<String, RequestBody> =
                LinkedHashMap<String, RequestBody>()
            for ((key, value) in request) {
                val requestBody: RequestBody = value.toRequestBody("text/plain".toMediaTypeOrNull())
                mapRequest[key] = requestBody
            }

            apiService.APICallPostMultipleImages(
                url,
                getHeaderParam(request),
                mapImageList,
                mapRequest
            ).enqueue(object : Callback<ServiceResponse> {
                override fun onResponse(
                    call: Call<ServiceResponse>,
                    response: Response<ServiceResponse>
                ) {
                    if (response.isSuccessful) {
                        Utility.dissmis_dialog_loading()
                        response.body()?.let { responseListener!!.onDataProviderResult(it) }
                    } else {
                        Utility.dissmis_dialog_loading()
                        handleError(response.errorBody(), mContext)
                    }
                }

                override fun onFailure(call: Call<ServiceResponse>, t: Throwable) {
                    Utility.dissmis_dialog_loading()
                    failureResponse()
                }
            })
        } else
            noInternetResponse()
    }

    fun callRxGetApi(
        isLoaderRequired: Boolean,
        url: String,
        request: HashMap<String, String>,
        mContext: Context
    ) {
        if (Utility.isNetworkAvaliable(mContext)) {

            if (isLoaderRequired)
                Utility.dialog_loading(mContext)

            apiService.APICallGetMethod(url, getHeaderParam(request))
                .enqueue(object : Callback<ServiceResponse> {
                    override fun onResponse(
                        call: Call<ServiceResponse>,
                        response: Response<ServiceResponse>
                    ) {
                        if (response.isSuccessful) {
                            Utility.dissmis_dialog_loading()
                            response.body()?.let { responseListener!!.onDataProviderResult(it) }
                        } else {
                            Utility.dissmis_dialog_loading()
                            handleError(response.errorBody(), mContext)
                        }
                    }

                    override fun onFailure(call: Call<ServiceResponse>, t: Throwable) {
                        Utility.dissmis_dialog_loading()
                        failureResponse()
                    }
                })
        } else
            noInternetResponse()
    }

    fun callRxApiStr(
        isLoaderRequired: Boolean,
        url: String,
        request: HashMap<String, String>,
        mContext: Context
    ) {
        if (Utility.isNetworkAvaliable(mContext)) {

            if (isLoaderRequired)
                Utility.dialog_loading(mContext)

            apiService.APICallPost(url, getHeaderParam(request))
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            Utility.dissmis_dialog_loading()
                            responseStrListener!!.onDataProviderResult(response.body()!!)
                        } else {
                            Utility.dissmis_dialog_loading()
                            handleError(response.errorBody(), mContext)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Utility.dissmis_dialog_loading()
                        failureResponse()
                    }
                })
        } else
            noInternetResponse()
    }

    fun callRxApiGetStr(
        isLoaderRequired: Boolean,
        url: String,
        request: HashMap<String, String>,
        mContext: Context
    ) {
        if (Utility.isNetworkAvaliable(mContext)) {

            if (isLoaderRequired)
                Utility.dialog_loading(mContext)

            apiService.APICallGet(url, getHeaderParam(request))
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            Utility.dissmis_dialog_loading()
                            responseStrListener!!.onDataProviderResult(response.body()!!)
                        } else {
                            Utility.dissmis_dialog_loading()
                            handleError(response.errorBody(), mContext)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Utility.dissmis_dialog_loading()
                        failureResponse()
                    }
                })
        } else
            noInternetResponse()
    }

    private fun noInternetResponse() {
        responseListener!!.onDataProviderResult(
            ServiceResponse("501", "No Internet connection.", "")
        )
    }

    private fun failureResponse() {
        responseListener!!.onDataProviderResult(
            ServiceResponse("204", "There is issue on server side. Please try again", "")
        )
    }

    private fun handleError(error: ResponseBody?, mContext: Context) {
        try {
            val jsonObj1 = JSONObject(error!!.string())
            val code = jsonObj1.getString("code")
            val msg = jsonObj1.getString("msg")

            if (code == "401") {
                Utility.toast(mContext, msg)
                Utility.logout(mContext)
            } else if (code == "403") {
                Utility.alert_dialog(mContext, msg, false)
            } else {
                responseListener!!.onDataProviderResult(ServiceResponse(code, msg, ""))
            }

        } catch (e: Exception) {
            responseListener!!.onDataProviderResult(
                ServiceResponse(
                    "204",
                    "There is issue on server side. Please try again",
                    ""
                )
            )
        }
    }

    private fun getHeaderParam(request: HashMap<String, String>): LinkedHashMap<String, String> {
        var jsonObject = JSONObject()
        for ((key, value) in request) {
            jsonObject.put(key, value.toString().trim())
        }
        val strHash = SHA256().hmacDigest(jsonObject.toString())

        val model = MobileInfo()
        model.apply {
            model.serialNo = Build.SERIAL
            model.model = Build.MODEL
            model.manufacture = Build.MANUFACTURER
            model.brand = Build.BRAND
            model.sdkVersion = Build.VERSION.SDK_INT.toString()
            model.versionCode = Build.VERSION.RELEASE.toString()
        }

        val hashMap = LinkedHashMap<String, String>()
        hashMap["Authorization"] = "Bearer " + Paper.book().read(AppConstant.ACCESS_TOKEN, "")
        hashMap["x-device-token"] = Paper.book().read(AppConstant.FCM_TOKEN, "")!!
        hashMap["x-device-type"] = "ANDROID"
        hashMap["x-app-version"] = BuildConfig.VERSION_NAME
        hashMap["x-device-info"] = Gson().toJson(model)
        hashMap["X-localization"] = "en"
        hashMap["x-secret-hash-key"] = strHash
        hashMap["x-merchants-key"] = "nepra_solar"

        return hashMap
    }

}