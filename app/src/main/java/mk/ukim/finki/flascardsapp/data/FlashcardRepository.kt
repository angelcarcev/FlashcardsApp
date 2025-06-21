package mk.ukim.finki.flascardsapp.data

import androidx.lifecycle.LiveData

class FlashcardRepository(private val dao: FlashcardDao) {

    val allFlashcards = dao.getAll()

    suspend fun insert(flashcard: FlashcardEntity) {
        dao.insert(flashcard)
    }

    suspend fun update(flashcard: FlashcardEntity) {
        dao.update(flashcard)
    }

    suspend fun delete(flashcard: FlashcardEntity) {
        dao.delete(flashcard)
    }

    fun getFlashcardById(id: Int): LiveData<FlashcardEntity?> {
        return dao.getById(id)
    }
}