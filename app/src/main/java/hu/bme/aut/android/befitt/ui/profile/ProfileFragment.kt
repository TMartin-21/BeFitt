package hu.bme.aut.android.befitt.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import hu.bme.aut.android.befitt.R
import hu.bme.aut.android.befitt.databinding.FragmentProfileBinding
import hu.bme.aut.android.befitt.model.Profile
import hu.bme.aut.android.befitt.repository.PrefRepository
import io.getstream.avatarview.coil.loadImage

class ProfileFragment : Fragment(), EditDialogFragment.ProfileEditListener {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var prefRepository: PrefRepository

    companion object {
        const val REQUEST_CAMERA_IMAGE = 101

        lateinit var profile: Profile
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefRepository = PrefRepository(requireContext())
    }

    @SuppressLint("SetTextI18n")
    private fun setProfile(){
        profile = prefRepository.getProfile()
        binding.tvName.text = profile.name
        binding.tvAge.text = profile.age.toString()
        binding.tvHeight.text = "${profile.height} cm"
        binding.tvWeight.text = "${profile.weight} kg"
        binding.tvMinDist.text = "${profile.minDistance} m"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        setProfile()

        val image = prefRepository.getImage()
        if (image == null){
            binding.avatarView.loadImage(
                data = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_run) }
            )
        } else {
            binding.avatarView.loadImage(
                data = image
            )
        }

        binding.profileSpeedDial.addActionItem(
            SpeedDialActionItem.Builder(
                R.id.fab_edit_label, R.drawable.ic_edit
            ).create()
        )

        binding.profileSpeedDial.addActionItem(
            SpeedDialActionItem.Builder(
                R.id.fab_delete_label, R.drawable.ic_delete
            ).create()
        )

        binding.profileSpeedDial.addActionItem(
            SpeedDialActionItem.Builder(
                R.id.fab_camera_label, R.drawable.ic_camera
            ).create()
        )

        binding.profileSpeedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when(actionItem.id){
                R.id.fab_edit_label -> {
                    val editDialog = EditDialogFragment()
                    editDialog.setTargetFragment(this, 0)
                    editDialog.show(
                        parentFragmentManager,
                        EditDialogFragment.TAG
                    )
                    return@OnActionSelectedListener true
                }
                R.id.fab_delete_label -> {
                    prefRepository.clearData()
                    binding.avatarView.loadImage(
                        data = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_run) }
                    )
                    setProfile()
                    return@OnActionSelectedListener true
                }
                R.id.fab_camera_label -> {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, REQUEST_CAMERA_IMAGE)
                    return@OnActionSelectedListener true
                }
                else -> { return@OnActionSelectedListener true }
            }
        })

        return binding.root
    }

    override fun saveProfileInSharedPreferences(profile: Profile) {
        prefRepository.saveProfile(profile)
        setProfile()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA_IMAGE){
            if (resultCode == Activity.RESULT_OK){
                try {
                    prefRepository.saveImage(data!!.extras!!.get("data") as Bitmap)
                    binding.avatarView.loadImage(
                        data = prefRepository.getImage()
                    )
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }
}