package com.nbrb.tmob.domain.entity

data class Currency(
    val id: Int,
    val parentId: Int,
    val code: String,
    val abbreviation: String,
    val name: String,
    val quotName: String,
    val scale: Int,
    val periodicity: Int,
    val dateStart: String,
    val dateEnd: String
)
