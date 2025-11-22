package com.vdx.flagship.internal

import com.vdx.flagship.model.FeatureFlag
import retrofit2.http.GET
import retrofit2.http.Path

internal interface FlagshipApiService {
    @GET("sdk/{applicationKey}/flags")
    suspend fun getFlags(
        @Path("applicationKey") applicationKey: String
    ): List<FeatureFlag>
}
