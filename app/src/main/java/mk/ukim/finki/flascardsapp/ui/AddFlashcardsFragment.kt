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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import mk.ukim.finki.flascardsapp.R
import mk.ukim.finki.flascardsapp.data.FlashcardEntity
import mk.ukim.finki.flascardsapp.notification.FlashcardNotificationManager
import mk.ukim.finki.flascardsapp.viewmodel.FlashcardViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class AddFlashcardsFragment : Fragment() {
    private lateinit var questionEditText: TextInputEditText
    private lateinit var answerEditText: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var removeImageButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var viewModel: FlashcardViewModel

    private var selectedImageUri: Uri? = null
    private var savedImagePath: String? = null

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
        val view = inflater.inflate(R.layout.fragment_add_flashcards, container, false)

        questionEditText = view.findViewById(R.id.questionEditText)
        answerEditText = view.findViewById(R.id.answerEditText)
        saveButton = view.findViewById(R.id.saveButton)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        removeImageButton = view.findViewById(R.id.removeImageButton)
        imagePreview = view.findViewById(R.id.imagePreview)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[FlashcardViewModel::class.java]

        setupClickListeners()
    }

    private fun setupClickListeners() {
        selectImageButton.setOnClickListener {
            checkPermissionAndSelectImage()
        }

        removeImageButton.setOnClickListener {
            removeSelectedImage()
        }

        saveButton.setOnClickListener {
            saveFlashcard()
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
                multiPermissionLauncher.launch(arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ))
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
            singlePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
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

    private fun saveFlashcard() {
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

        val newFlashcard = FlashcardEntity(
            question = question,
            answer = answer,
            imagePath = savedImagePath
        )

        viewModel.insert(newFlashcard)

        val notificationManager = FlashcardNotificationManager(requireContext())
        notificationManager.showNewCardNotification(question)

        Toast.makeText(context, "Картичката е зачувана!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}