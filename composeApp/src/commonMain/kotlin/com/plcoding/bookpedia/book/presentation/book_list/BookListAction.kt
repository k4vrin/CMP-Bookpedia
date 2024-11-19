package com.plcoding.bookpedia.book.presentation.book_list

import com.plcoding.bookpedia.book.domain.Book

sealed interface BookListAction {
    data class OnSearchQueryChanged(val query: String) : BookListAction
    data class OnBookClicked(val book: Book) : BookListAction
    data class OnTabSelected(val index: Int) : BookListAction
}