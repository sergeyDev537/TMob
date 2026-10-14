package com.nbrb.tmob.data.network.dto

import com.google.gson.annotations.SerializedName

data class RateDto(
    @SerializedName("Cur_ID") val curId: Int,
    @SerializedName("Date") val date: String,
    @SerializedName("Cur_Code") val curCode: String,
    @SerializedName("Cur_Abbreviation") val curAbbreviation: String,
    @SerializedName("Cur_Scale") val curScale: Int,
    @SerializedName("Cur_Name") val curName: String,
    @SerializedName("Cur_OfficialRate") val curOfficialRate: Double?
)
