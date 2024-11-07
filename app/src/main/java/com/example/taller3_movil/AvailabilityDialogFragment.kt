import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AvailabilityDialogFragment : DialogFragment() {

    interface AvailabilityListener {
        fun onStatusSelected(status: Boolean)
    }

    private var listener: AvailabilityListener? = null

    fun setListener(listener: AvailabilityListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val options = arrayOf("Disponible", "Desconectado")
        return AlertDialog.Builder(requireContext())
            .setTitle("Selecciona tu disponibilidad")
            .setItems(options) { _, which ->
                val status = which == 0  // Si es "Disponible", el valor será true; de lo contrario, false
                listener?.onStatusSelected(status)
            }
            .create()
    }
}

