package hu.bme.aut.android.befitt.ui.profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hu.bme.aut.android.befitt.databinding.DialogEditBinding
import hu.bme.aut.android.befitt.model.Profile
import java.lang.ClassCastException
import java.lang.RuntimeException

class EditDialogFragment : DialogFragment() {
    interface ProfileEditListener {
        fun saveProfileInSharedPreferences(profile: Profile)
    }

    private lateinit var binding: DialogEditBinding
    private lateinit var listener: ProfileEditListener

    companion object {
        const val TAG = "EditDialogFragment"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = targetFragment as ProfileEditListener
        } catch (e: ClassCastException) {
            throw RuntimeException(e)
        }
    }

    private fun setPicker(np: NumberPicker, array: Array<String>, maxValue: Int){
        np.minValue = 1
        np.maxValue = maxValue
        np.wrapSelectorWheel = false
        np.displayedValues = array
        np.value = 1
    }

    private fun setErrorMessage(et : TextInputEditText, textInputLayout: TextInputLayout){
        et.doOnTextChanged { text, start, before, count ->
            if (text!!.isEmpty()) {
                textInputLayout.error = "Required data!"
            } else {
                textInputLayout.error = null
            }
        }
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditBinding.inflate(LayoutInflater.from(context))

        var ageNumbers = arrayOf<String>()
        for (i in 1..100)
            ageNumbers += i.toString()

        var minDistNumbers = arrayOf<String>()
        for (j in 1..50)
            minDistNumbers += (j * 100).toString()

        setPicker(binding.agePicker, ageNumbers, 100)
        setPicker(binding.minDistPicker, minDistNumbers, 50)

        setErrorMessage(binding.etName, binding.textInputLayout1)
        setErrorMessage(binding.etHeight, binding.textInputLayout2)
        setErrorMessage(binding.etWeight, binding.textInputLayout3)

        return AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(binding.root)
            .setPositiveButton("Ok") { _, _ ->
                if (isValid())
                    listener.saveProfileInSharedPreferences(getProfile())
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun isValid() = !binding.etName.text.isNullOrEmpty() &&
            !binding.etWeight.text.isNullOrEmpty() && !binding.etHeight.text.isNullOrEmpty()

    private fun getProfile() = Profile(
        name = binding.etName.text.toString(),
        age = binding.agePicker.value,
        height = binding.etHeight.text.toString().toInt(),
        weight = binding.etWeight.text.toString().toFloat(),
        minDistance = binding.minDistPicker.value * 100
    )
}