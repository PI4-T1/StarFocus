import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.example.app.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflando o layout com ViewBinding
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        // Inicializa o Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Chama o método para carregar os dados do Firestore
        loadStoryStatusFromFirestore()

        return binding.root
    }

    private fun loadStoryStatusFromFirestore() {
        val userId = "user_id_example" // Defina o ID do usuário aqui (pode ser do FirebaseAuth ou outra fonte)
        db.collection("pessoas").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val history1 = document.getBoolean("history1") ?: false
                    val history2 = document.getBoolean("history2") ?: false
                    val history3 = document.getBoolean("history3") ?: false
                    val history4 = document.getBoolean("history4") ?: false

                    // Atualiza a visibilidade dos botões com base no status das histórias
                    updateButtonVisibility(history1, history2, history3, history4)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("MapFragment", "Error getting documents.", exception)
            }
    }

    private fun updateButtonVisibility(history1: Boolean, history2: Boolean, history3: Boolean, history4: Boolean) {
        // História 1 está sempre desbloqueada
        binding.history1unlock.visibility = View.VISIBLE
        binding.history2unlock.visibility = if (history2) View.VISIBLE else View.GONE
        binding.history2locked.visibility = if (!history2) View.VISIBLE else View.GONE
        binding.history3unlock.visibility = if (history3) View.VISIBLE else View.GONE
        binding.history3locked.visibility = if (!history3) View.VISIBLE else View.GONE
        binding.history4unlock.visibility = if (history4) View.VISIBLE else View.GONE
        binding.history4locked.visibility = if (!history4) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Libera a referência para evitar vazamento de memória
    }
}
