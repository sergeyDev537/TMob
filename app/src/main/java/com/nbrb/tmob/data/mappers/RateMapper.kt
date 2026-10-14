package com.nbrb.tmob.data.mappers

import com.nbrb.tmob.data.db.dbmodels.RateDbModel
import com.nbrb.tmob.data.network.dto.RateDto
import com.nbrb.tmob.domain.entity.Rate

fun RateDto.toDbModel(formattedDate: String): RateDbModel {
    return RateDbModel(
        id = curId,
        date = formattedDate,
        code = curCode,
        abbreviation = curAbbreviation,
        scale = curScale,
        name = curName,
        officialRate = curOfficialRate ?: 0.0
    )
}

fun RateDbModel.toDomain(previousDayRate: Double? = null): Rate {
    val currentPerUnit = if (scale > 0) officialRate / scale else officialRate
    val dynamics = if (previousDayRate != null) {
        currentPerUnit - previousDayRate
    } else null

    return Rate(
        id = id,
        code = code,
        abbreviation = abbreviation,
        name = name,
        scale = scale,
        officialRate = officialRate,
        date = date,
        dynamicsVsPreviousDay = dynamics
    )
}
