package mk.ukim.finki.flascardsapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import mk.ukim.finki.flascardsapp.data.*

class FlashcardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FlashcardRepository

    val allFlashcards: LiveData<List<FlashcardEntity>>

    init {
        val dao = FlashcardDatabase.getDatabase(application).flashcardDao()
        repository = FlashcardRepository(dao)
        allFlashcards = repository.allFlashcards
    }

    fun insert(flashcard: FlashcardEntity) = viewModelScope.launch {
        repository.insert(flashcard)
    }

    fun update(flashcard: FlashcardEntity) = viewModelScope.launch {
        repository.update(flashcard)
    }

    fun delete(flashcard: FlashcardEntity) = viewModelScope.launch {
        repository.delete(flashcard)
    }

    fun getFlashcardById(id: Int): LiveData<FlashcardEntity?> {
        return repository.getFlashcardById(id)
    }
}