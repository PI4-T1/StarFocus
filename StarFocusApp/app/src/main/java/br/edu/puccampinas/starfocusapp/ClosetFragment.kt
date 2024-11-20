package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import br.edu.puccampinas.starfocusapp.databinding.FragmentClosetBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ClosetFragment : Fragment() {
    private var _binding: FragmentClosetBinding? = null
    private val binding get() = _binding!!
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val imageUrls: MutableList<String> = mutableListOf()
    private var currentIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClosetBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            loadImages()
            updateImageView()
        }

        with(binding) {
            btnEsquerda.setOnClickListener {
                if (currentIndex > 0) {
                    currentIndex--
                } else {
                    currentIndex = imageUrls.size - 1
                }
                updateImageView()
            }

            btnDireita.setOnClickListener {
                if (currentIndex < imageUrls.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }
                updateImageView()
            }

            btnselecionar.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch { select() }
            }
        }
        return binding.root
    }

    private suspend fun select() {
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@withContext
            val userDoc = firestore.collection("Pessoas").document(userId)

            val listResult = storage.reference.child("Clothes").listAll().await()

            val sortedPrefixes = listResult.prefixes.sortedBy { it.name }
            sortedPrefixes.forEachIndexed { index, folderRef ->
                if (index == currentIndex){
                    userDoc.update("outfit", folderRef.name).await()
                }
            }
        }
        withContext(Dispatchers.Main){
            Toast.makeText(requireContext(), "Nova roupa selecionada!", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun loadImages() {
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@withContext
            val userDoc = firestore.collection("Pessoas").document(userId).get().await()

            val outfit = userDoc.getString("outfit") ?: ""
            val roupas = userDoc.get("roupas") as List<*>
            val listResult = storage.reference.child("Clothes").listAll().await()

            val sortedPrefixes = listResult.prefixes.sortedBy { it.name }
            sortedPrefixes.forEachIndexed { index, folderRef ->
                if (folderRef.name in roupas){
                    val url = folderRef.child("closet.png").downloadUrl.await().toString()
                    imageUrls.add(url)
                    if (folderRef.name == outfit) {
                        currentIndex = index
                    }
                }
            }
        }
    }

    private fun updateImageView() {
        if (imageUrls.isNotEmpty() && currentIndex in imageUrls.indices) {
            Glide.with(this).load(imageUrls[currentIndex]).into(binding.monsterImageView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
