package mk.ukim.finki.flascardsapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FlashcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: FlashcardEntity)

    @Query("SELECT * FROM flashcards")
    fun getAll(): LiveData<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE id = :id")
    fun getById(id: Int): LiveData<FlashcardEntity?>

    @Delete
    suspend fun delete(flashcard: FlashcardEntity)

    @Update
    suspend fun update(flashcard: FlashcardEntity)
}