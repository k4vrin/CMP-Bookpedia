package com.plcoding.bookpedia.book.domain

data class Book(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val authors: List<String>,
    val description: String?,
    val language: List<String>,
    val publishedDate: String?,
    val averageRating: Double?,
    val ratingsCount: Int?,
    val numPages: Int?,
    val numEditions: Int
)
