package com.plcoding.bookpedia.book.presentation.book_list

import com.plcoding.bookpedia.book.domain.Book
import com.plcoding.bookpedia.core.presentation.UiText

data class BookListState(
    val searchQuery: String = "Kotlin",
    val bookSearchResults: List<Book> = sampleBooks,
    val favoriteBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTabIndex: Int = 0,
    val errorMessage: UiText? = null,
)

val sampleBooks = (1..100).map {
    Book(
        id = it.toString(),
        title = "Book $it",
        thumbnailUrl = "https://picsum.photos/200/300",
        authors = listOf("Author $it"),
        description = "Description $it",
        language = emptyList(),
        publishedDate = null,
        averageRating = 4.67854,
        ratingsCount = 5,
        numPages = 100,
        numEditions = 3
    )
}