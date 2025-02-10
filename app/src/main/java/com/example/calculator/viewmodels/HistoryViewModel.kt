import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase

data class HistoryEntry(
    val expression: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class HistoryViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val historyRef = database.reference.child("calculator_history")

    fun saveHistoryToFirebase(expression: String) {
        val historyEntry = HistoryEntry(
            expression = expression,
        )

        val newHistoryRef = historyRef.push()
        newHistoryRef.setValue(historyEntry)
            .addOnSuccessListener {
                Log.d("HistoryViewModel", "History saved successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("HistoryViewModel", "Error saving history: ${exception.message}")
            }
    }

    fun loadHistoryFromFirebase(onHistoryLoaded: (List<HistoryEntry>) -> Unit) {
        historyRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val historyList = snapshot.children.mapNotNull { it.getValue(HistoryEntry::class.java) }
                    onHistoryLoaded(historyList)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HistoryViewModel", "Error loading history: ${exception.message}")
            }
    }
}
