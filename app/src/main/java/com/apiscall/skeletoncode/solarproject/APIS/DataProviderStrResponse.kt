package com.nepra.solar.APIS

import okhttp3.ResponseBody

interface DataProviderStrResponse {
    fun onDataProviderResult(responseBody: ResponseBody)
}