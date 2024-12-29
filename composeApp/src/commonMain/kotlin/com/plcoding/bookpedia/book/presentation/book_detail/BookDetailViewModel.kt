package com.plcoding.bookpedia.book.presentation.book_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.plcoding.bookpedia.app.Route
import com.plcoding.bookpedia.book.domain.repositiory.BookRepository
import com.plcoding.bookpedia.core.domain.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val bookRepository: BookRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bookId = savedStateHandle.toRoute<Route.BookDetail>().id
    private val _state = MutableStateFlow(BookDetailState())
    val state = _state
        .onStart {
            fetchBookDescription()
            observeFavoriteStatus()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _state.value
        )


    fun onAction(action: BookDetailAction) {
        when (action) {
            BookDetailAction.OnBackClicked -> Unit
            BookDetailAction.OnFavoriteClicked -> {
                viewModelScope.launch {
                    if (state.value.isFavorite) {
                        bookRepository.deleteFavoriteBook(bookId)
                    } else {
                        state.value.book?.let {
                            bookRepository.markAsFavorite(it)
                        }
                    }
                }
            }

            is BookDetailAction.OnSelectedBookChange -> {
                _state.update { currState ->
                    currState.copy(
                        book = action.book
                    )
                }

            }
        }
    }

    private fun observeFavoriteStatus() {
        bookRepository.isBookFavorite(bookId)
            .onEach {
                _state.update { currState ->
                    currState.copy(
                        isFavorite = it
                    )
                }
            }
            .launchIn(viewModelScope)
    }


    private fun fetchBookDescription() {
        viewModelScope.launch {
            bookRepository
                .getBookDescription(bookId = bookId)
                .onSuccess { desc ->
                    _state.update { currState ->
                        currState.copy(
                            book = currState.book?.copy(description = desc),
                            isLoading = false
                        )
                    }

                }
        }
    }
}