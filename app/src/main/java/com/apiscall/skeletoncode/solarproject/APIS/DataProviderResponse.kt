package com.nepra.solar.APIS

import com.apiscall.skeletoncode.solarproject.model.ServiceResponse

interface DataProviderResponse {
    fun onDataProviderResult(dataModel: ServiceResponse)
}