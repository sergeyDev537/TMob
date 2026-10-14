package com.nbrb.tmob.data.network.dto

import com.google.gson.annotations.SerializedName

data class RateShortDto(
    @SerializedName("Cur_ID") val curId: Int,
    @SerializedName("Date") val date: String,
    @SerializedName("Cur_OfficialRate") val curOfficialRate: Double?
)
