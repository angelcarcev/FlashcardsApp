package mk.ukim.finki.flascardsapp.adapters

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import mk.ukim.finki.flascardsapp.R
import mk.ukim.finki.flascardsapp.data.FlashcardEntity
import java.io.File

class FlashcardAdapter(
    private var flashcards: List<FlashcardEntity>,
    private val onEditClick: (FlashcardEntity) -> Unit,
    private val onDeleteClick: (FlashcardEntity) -> Unit
) : RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val answerText: TextView = itemView.findViewById(R.id.answerText)
        private val tapHintText: TextView = itemView.findViewById(R.id.tapHintText)
        private val flashcardImage: ImageView = itemView.findViewById(R.id.flashcardImage)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        private var isAnswerVisible = false

        fun bind(
            flashcard: FlashcardEntity,
            onEditClick: (FlashcardEntity) -> Unit,
            onDeleteClick: (FlashcardEntity) -> Unit
        ) {
            questionText.text = flashcard.question
            answerText.text = flashcard.answer

            if (!flashcard.imagePath.isNullOrEmpty()) {
                val imageFile = File(flashcard.imagePath)
                if (imageFile.exists()) {
                    flashcardImage.setImageURI(Uri.fromFile(imageFile))
                    flashcardImage.visibility = View.VISIBLE
                } else {
                    flashcardImage.visibility = View.GONE
                }
            } else {
                flashcardImage.visibility = View.GONE
            }

            isAnswerVisible = false
            answerText.visibility = View.GONE
            answerText.alpha = 0f
            tapHintText.visibility = View.VISIBLE
            tapHintText.alpha = 1f
            cardView.cardElevation = 4f

            editButton.setOnClickListener { onEditClick(flashcard) }
            deleteButton.setOnClickListener { onDeleteClick(flashcard) }

            cardView.setOnClickListener {
                toggleAnswerWithAnimation()
            }
        }

        private fun toggleAnswerWithAnimation() {
            if (isAnswerVisible) {
                ObjectAnimator.ofFloat(answerText, "alpha", 1f, 0f).apply {
                    duration = 200
                    start()
                }.also {
                    it.doOnEnd {
                        answerText.visibility = View.GONE
                    }
                }

                ObjectAnimator.ofFloat(tapHintText, "alpha", 0f, 1f).apply {
                    duration = 200
                    start()
                }.also {
                    tapHintText.visibility = View.VISIBLE
                }

                ObjectAnimator.ofFloat(cardView, "cardElevation", 8f, 4f).apply {
                    duration = 200
                    start()
                }

                isAnswerVisible = false
            } else {
                answerText.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(answerText, "alpha", 0f, 1f).apply {
                    duration = 300
                    start()
                }

                ObjectAnimator.ofFloat(tapHintText, "alpha", 1f, 0f).apply {
                    duration = 200
                    start()
                }.also {
                    it.doOnEnd {
                        tapHintText.visibility = View.GONE
                    }
                }

                ObjectAnimator.ofFloat(cardView, "cardElevation", 4f, 8f).apply {
                    duration = 200
                    start()
                }

                isAnswerVisible = true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        holder.bind(flashcards[position], onEditClick, onDeleteClick)
    }

    override fun getItemCount() = flashcards.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<FlashcardEntity>) {
        flashcards = newList
        notifyDataSetChanged()
    }
}

private fun ObjectAnimator.doOnEnd(action: () -> Unit) {
    addListener(object : android.animation.Animator.AnimatorListener {
        override fun onAnimationStart(animation: android.animation.Animator) {}
        override fun onAnimationEnd(animation: android.animation.Animator) {
            action()
        }
        override fun onAnimationCancel(animation: android.animation.Animator) {}
        override fun onAnimationRepeat(animation: android.animation.Animator) {}
    })
}