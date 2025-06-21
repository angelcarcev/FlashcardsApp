package mk.ukim.finki.flascardsapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import mk.ukim.finki.flascardsapp.R
import mk.ukim.finki.flascardsapp.data.FlashcardEntity
import mk.ukim.finki.flascardsapp.viewmodel.FlashcardViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class EditFlashcardFragment : Fragment() {
    private lateinit var questionEditText: TextInputEditText
    private lateinit var answerEditText: TextInputEditText
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var removeImageButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var viewModel: FlashcardViewModel

    private var currentFlashcard: FlashcardEntity? = null
    private var selectedImageUri: Uri? = null
    private var savedImagePath: String? = null
    private var flashcardId: Int = -1

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
            }
        }
    }

    private val multiPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasImageAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true ||
                    permissions[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }

        if (hasImageAccess) {
            openImagePicker()
        } else {
            Toast.makeText(context, "Потребна е дозвола за пристап до слики", Toast.LENGTH_SHORT).show()
        }
    }

    private val singlePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(context, "Потребна е дозвола за пристап до слики", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_flashcards, container, false)

        questionEditText = view.findViewById(R.id.questionEditText)
        answerEditText = view.findViewById(R.id.answerEditText)
        updateButton = view.findViewById(R.id.updateButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        removeImageButton = view.findViewById(R.id.removeImageButton)
        imagePreview = view.findViewById(R.id.imagePreview)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[FlashcardViewModel::class.java]

        flashcardId = arguments?.getInt("flashcardId", -1) ?: -1

        if (flashcardId != -1) {
            loadFlashcard()
        } else {
            Toast.makeText(context, "Грешка при вчитување на картичката", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        setupClickListeners()
    }

    private fun loadFlashcard() {
        viewModel.getFlashcardById(flashcardId).observe(viewLifecycleOwner) { flashcard ->
            flashcard?.let {
                currentFlashcard = it
                populateFields(it)
            }
        }
    }

    private fun populateFields(flashcard: FlashcardEntity) {
        questionEditText.setText(flashcard.question)
        answerEditText.setText(flashcard.answer)
        savedImagePath = flashcard.imagePath

        if (!flashcard.imagePath.isNullOrEmpty()) {
            val imageFile = File(flashcard.imagePath)
            if (imageFile.exists()) {
                imagePreview.setImageURI(Uri.fromFile(imageFile))
                imagePreview.visibility = View.VISIBLE
                removeImageButton.visibility = View.VISIBLE
                selectImageButton.text = "Промени слика"
            }
        }
    }

    private fun setupClickListeners() {
        selectImageButton.setOnClickListener {
            checkPermissionAndSelectImage()
        }

        removeImageButton.setOnClickListener {
            removeSelectedImage()
        }

        updateButton.setOnClickListener {
            updateFlashcard()
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        cancelButton.setOnClickListener {
            showCancelConfirmationDialog()
        }
    }

    private fun checkPermissionAndSelectImage() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                handleAndroid14PlusPermissions()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                handleAndroid13Permissions()
            }
            else -> {
                handleLegacyPermissions()
            }
        }
    }

    private fun handleAndroid14PlusPermissions() {
        val hasFullAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            TODO("VERSION.SDK_INT < TIRAMISU")
        }

        val hasPartialAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            TODO("VERSION.SDK_INT < UPSIDE_DOWN_CAKE")
        }

        when {
            hasFullAccess || hasPartialAccess -> {
                openImagePicker()
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    multiPermissionLauncher.launch(arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ))
                }
            }
        }
    }

    private fun handleAndroid13Permissions() {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            TODO("VERSION.SDK_INT < TIRAMISU")
        }

        if (hasPermission) {
            openImagePicker()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                singlePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }

    private fun handleLegacyPermissions() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            openImagePicker()
        } else {
            singlePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    @SuppressLint("IntentReset")
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun displaySelectedImage(uri: Uri) {
        imagePreview.setImageURI(uri)
        imagePreview.visibility = View.VISIBLE
        removeImageButton.visibility = View.VISIBLE
        selectImageButton.text = "Промени слика"
    }

    private fun removeSelectedImage() {
        selectedImageUri = null
        savedImagePath = null
        imagePreview.visibility = View.GONE
        removeImageButton.visibility = View.GONE
        selectImageButton.text = "Избери слика"
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "flashcard_${UUID.randomUUID()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun updateFlashcard() {
        val question = questionEditText.text.toString().trim()
        val answer = answerEditText.text.toString().trim()

        if (question.isBlank()) {
            questionEditText.error = "Внесете прашање"
            return
        }

        if (answer.isBlank()) {
            answerEditText.error = "Внесете одговор"
            return
        }

        selectedImageUri?.let { uri ->
            savedImagePath = saveImageToInternalStorage(uri)
        }

        currentFlashcard?.let { flashcard ->
            val updatedFlashcard = flashcard.copy(
                question = question,
                answer = answer,
                imagePath = savedImagePath
            )

            viewModel.update(updatedFlashcard)
            Toast.makeText(context, "Картичката е ажурирана!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Избриши картичка")
            .setMessage("Дали сте сигурни дека сакате да ја избришете оваа картичка? Оваа акција не може да се врати.")
            .setPositiveButton("Избриши") { _, _ ->
                deleteFlashcard()
            }
            .setNegativeButton("Откажи", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteFlashcard() {
        currentFlashcard?.let { flashcard ->
            flashcard.imagePath?.let { imagePath ->
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    imageFile.delete()
                }
            }
            viewModel.delete(flashcard)
            Toast.makeText(context, "Картичката е избришана!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun showCancelConfirmationDialog() {
        val hasChanges = hasUnsavedChanges()

        if (hasChanges) {
            AlertDialog.Builder(requireContext())
                .setTitle("Незачувани промени")
                .setMessage("Имате незачувани промени. Дали сте сигурни дека сакате да излезете без да ги зачувате?")
                .setPositiveButton("Излези") { _, _ ->
                    findNavController().popBackStack()
                }
                .setNegativeButton("Продолжи со уредување", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        val currentQuestion = questionEditText.text.toString().trim()
        val currentAnswer = answerEditText.text.toString().trim()

        currentFlashcard?.let { flashcard ->
            val questionChanged = currentQuestion != flashcard.question
            val answerChanged = currentAnswer != flashcard.answer
            val imageChanged = selectedImageUri != null ||
                    (savedImagePath != flashcard.imagePath)

            return questionChanged || answerChanged || imageChanged
        }

        return false
    }
}