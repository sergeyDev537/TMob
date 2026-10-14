package com.nbrb.tmob.data.mappers

import com.nbrb.tmob.data.db.dbmodels.CurrencyDbModel
import com.nbrb.tmob.data.network.dto.CurrencyDto
import com.nbrb.tmob.domain.entity.Currency

fun CurrencyDto.toDbModel(): CurrencyDbModel {
    return CurrencyDbModel(
        id = curId,
        parentId = curParentId ?: curId,
        code = curCode,
        abbreviation = curAbbreviation,
        name = curName,
        quotName = curQuotName,
        scale = curScale,
        periodicity = curPeriodicity,
        dateStart = curDateStart,
        dateEnd = curDateEnd
    )
}

fun CurrencyDbModel.toDomain(): Currency {
    return Currency(
        id = id,
        parentId = parentId,
        code = code,
        abbreviation = abbreviation,
        name = name,
        quotName = quotName,
        scale = scale,
        periodicity = periodicity,
        dateStart = dateStart,
        dateEnd = dateEnd
    )
}
