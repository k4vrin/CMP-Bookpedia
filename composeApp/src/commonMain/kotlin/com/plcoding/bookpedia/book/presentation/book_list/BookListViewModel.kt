@file:OptIn(FlowPreview::class)

package com.plcoding.bookpedia.book.presentation.book_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.plcoding.bookpedia.book.domain.model.Book
import com.plcoding.bookpedia.book.domain.repositiory.BookRepository
import com.plcoding.bookpedia.core.domain.onError
import com.plcoding.bookpedia.core.domain.onSuccess
import com.plcoding.bookpedia.core.presentation.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookListViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {

    private var cachedBooks = emptyList<Book>()
    private var searchJob: Job? = null
    private var favoriteBookJob: Job? = null

    private val _state = MutableStateFlow(BookListState())
    val state = _state
        .onStart {
            if (cachedBooks.isEmpty()) {
                observeSearchQuery()
            }
            observerFavoriteBooks()
        }
        .stateIn(
            scope = viewModelScope,
            // 5 seconds longer than the last subscriber disconnects
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _state.value
        )

    fun onAction(action: BookListAction) {
        when (action) {
            is BookListAction.OnBookClicked -> {

            }

            is BookListAction.OnSearchQueryChanged -> {
                _state.update { currState ->
                    currState.copy(
                        searchQuery = action.query
                    )
                }

            }

            is BookListAction.OnTabSelected -> {
                _state.update { currState ->
                    currState.copy(
                        selectedTabIndex = action.index
                    )
                }

            }
        }
    }

    private fun observerFavoriteBooks() {
        favoriteBookJob?.cancel()
        favoriteBookJob = bookRepository
            .getFavoriteBooks()
            .onEach { favoriteBooks ->
                _state.update { currState ->
                    currState.copy(
                        favoriteBooks = favoriteBooks
                    )
                }

            }
            .launchIn(viewModelScope)
    }

    private fun observeSearchQuery() {
        state
            .map { it.searchQuery }
            .distinctUntilChanged()
            .debounce(500)
            .onEach { query ->
                when {
                    query.isBlank() -> {
                        _state.update { currState ->
                            currState.copy(
                                errorMessage = null,
                                bookSearchResults = cachedBooks
                            )
                        }
                    }

                    query.length >= 2 -> {
                        searchJob?.cancel()
                        searchJob = searchBooks(query)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun searchBooks(query: String) =

        viewModelScope.launch {
            _state.update { currState ->
                currState.copy(isLoading = true)
            }
            bookRepository.searchBooks(query)
                .onSuccess { searchedBooks ->
//                    cachedBooks = searchedBooks
                    _state.update { currState ->
                        currState.copy(
                            isLoading = false,
                            bookSearchResults = searchedBooks
                        )
                    }
                }
                .onError { error ->
                    Logger.d(TAG) { "searchBooks: error: $error" }
                    _state.update { currState ->
                        currState.copy(
                            bookSearchResults = emptyList(),
                            isLoading = false,
                            errorMessage = error.toUiText()
                        )
                    }
                }
        }

    companion object {
        private const val TAG = "BookListViewModel"
    }


}

