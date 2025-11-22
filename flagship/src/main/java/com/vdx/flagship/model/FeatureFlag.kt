package com.vdx.flagship.model

import com.google.gson.annotations.SerializedName

data class FeatureFlag(
    @SerializedName("key") val key: String,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("value") val value: String?,
    @SerializedName("type") val type: FlagType
)

enum class FlagType {
    @SerializedName("BOOLEAN") BOOLEAN,
    @SerializedName("STRING") STRING,
    @SerializedName("NUMBER") NUMBER,
    @SerializedName("JSON") JSON
}
