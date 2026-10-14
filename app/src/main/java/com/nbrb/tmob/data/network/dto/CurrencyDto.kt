package com.nbrb.tmob.data.network.dto

import com.google.gson.annotations.SerializedName

data class CurrencyDto(
    @SerializedName("Cur_ID") val curId: Int,
    @SerializedName("Cur_ParentID") val curParentId: Int?,
    @SerializedName("Cur_Code") val curCode: String,
    @SerializedName("Cur_Abbreviation") val curAbbreviation: String,
    @SerializedName("Cur_Name") val curName: String,
    @SerializedName("Cur_QuotName") val curQuotName: String,
    @SerializedName("Cur_Scale") val curScale: Int,
    @SerializedName("Cur_Periodicity") val curPeriodicity: Int,
    @SerializedName("Cur_DateStart") val curDateStart: String,
    @SerializedName("Cur_DateEnd") val curDateEnd: String
)
