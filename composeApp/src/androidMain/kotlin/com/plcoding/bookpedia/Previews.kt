package com.plcoding.bookpedia

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plcoding.bookpedia.book.presentation.book_list.BookListScreen
import com.plcoding.bookpedia.book.presentation.book_list.BookListState
import com.plcoding.bookpedia.book.presentation.book_list.components.BookSearchBar
import com.plcoding.bookpedia.book.presentation.book_list.sampleBooks

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
private fun BookSearchBarPreview() {
    MaterialTheme {
        BookSearchBar(
            modifier = Modifier
                .fillMaxWidth(),
            searchQuery = "Kotlin",
            onSearchQueryChange = {},
            onSearch = {}
        )
    }
}


@Preview
@Composable
private fun BookListPreview() {
    BookListScreen(
        state = BookListState(
            bookSearchResults = sampleBooks
        ),
        onAction = {}
    )
}