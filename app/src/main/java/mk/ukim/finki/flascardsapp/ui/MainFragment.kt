package mk.ukim.finki.flascardsapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import mk.ukim.finki.flascardsapp.R
import mk.ukim.finki.flascardsapp.adapters.FlashcardAdapter
import mk.ukim.finki.flascardsapp.databinding.FragmentMainBinding
import mk.ukim.finki.flascardsapp.viewmodel.FlashcardViewModel

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FlashcardViewModel
    private lateinit var adapter: FlashcardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[FlashcardViewModel::class.java]

        setupRecyclerView()
        observeFlashcards()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = FlashcardAdapter(
            flashcards = emptyList(),
            onEditClick = { flashcard ->
                val bundle = Bundle().apply {
                    putInt("flashcardId", flashcard.id)
                }
                findNavController().navigate(R.id.action_mainFragment_to_editCardFragment, bundle)
            },
            onDeleteClick = { flashcard ->
                showDeleteConfirmationDialog(flashcard)
            }
        )

        binding.recyclerViewFlashcards.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFlashcards.adapter = adapter
    }

    private fun observeFlashcards() {
        viewModel.allFlashcards.observe(viewLifecycleOwner) { flashcards ->
            adapter.updateData(flashcards)
        }
    }

    private fun setupFab() {
        binding.fabAddFlashcard.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addCardFragment)
        }
    }

    private fun showDeleteConfirmationDialog(flashcard: mk.ukim.finki.flascardsapp.data.FlashcardEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Избриши картичка")
            .setMessage("Дали сте сигурни дека сакате да ја избришете оваа картичка?")
            .setPositiveButton("Избриши") { _, _ ->
                viewModel.delete(flashcard)
            }
            .setNegativeButton("Откажи", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}