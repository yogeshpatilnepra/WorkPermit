package com.apiscall.skeletoncode.solarproject.APIS

import com.apiscall.skeletoncode.solarproject.model.ServiceResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Url

interface APIService {

    @FormUrlEncoded
    @POST
    fun APICallPostForm(
        @Url endPoint: String,
        @HeaderMap hashMapToken: HashMap<String, String>,
        @FieldMap params: HashMap<String, String>
    ): Call<ServiceResponse>

    @Multipart
    @POST
    fun APICallPostMultipleImages(
        @Url endPoint: String,
        @HeaderMap hashMap: HashMap<String, String>,
        @Part imageMap: List<MultipartBody.Part>,
        @PartMap map: HashMap<String, RequestBody>
    ): Call<ServiceResponse>

    @GET
    fun APICallGetMethod(
        @Url endPoint: String,
        @HeaderMap hashMap: HashMap<String, String>
    ): Call<ServiceResponse>

    @POST
    fun APICallPost(
        @Url endPoint: String,
        @HeaderMap hashMap: HashMap<String, String>
    ): Call<ResponseBody>

    @POST
    fun APICallGet(
        @Url endPoint: String,
        @HeaderMap hashMap: HashMap<String, String>
    ): Call<ResponseBody>
}