package com.example.calisthenicsworkout.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.AuthActivity
import com.example.calisthenicsworkout.PhotoActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentMyProfileBinding
import com.example.calisthenicsworkout.databinding.ProgressDialogBinding
import com.example.calisthenicsworkout.viewmodels.ProfileViewModel
import com.example.calisthenicsworkout.viewmodels.ProfileViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MyProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory
    private lateinit var binding: FragmentMyProfileBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_my_profile,container,false)


        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = ProfileViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(ProfileViewModel::class.java)
        binding.lifecycleOwner = this

        viewModel.currentUser.observe(viewLifecycleOwner,{
            it?.let { user ->
                binding.fullUserName.text = "Full Name: " + user.userFullName
                binding.userEmail.text = "Email: "+ user.userEmail
                binding.userId.text = "UID: " + user.userId
                binding.profileImageView.setImageURI(user.userImage)
                binding.points = user.points
            }
        })



        binding.logoutButton.setOnClickListener {
            if(binding.unregisterSwitch.isChecked){
                AlertDialog.Builder(context)
                    .setTitle("Are you sure you want to unregister?")
                    .setPositiveButton("Yes") { _, _ ->
                        AlertDialog.Builder(context).setTitle("Deleting...").setCancelable(false).show()
                        viewModel.unregister(Intent(context, AuthActivity::class.java),requireActivity())

                    }
                    .setNegativeButton("No",null)
                    .show()
            }else{
                viewModel.logout(Intent(context, AuthActivity::class.java),requireActivity())
            }


        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    viewModel.saveProfilePic(it,requireContext())

                    val dialog = Dialog(requireContext())
                    val dialogBinding = ProgressDialogBinding.inflate(LayoutInflater.from(requireContext()))
                    dialog.setContentView(dialogBinding.root)
                    dialog.setCancelable(false)
                    dialog.show()
                    viewModel.uploadProgress.observe(viewLifecycleOwner,{ progress ->
                        dialogBinding.progressBar5.progress = progress.toInt()
                        dialogBinding.progressDialogPercent.text = "$progress%"
                        dialogBinding.progressDialogTitle.text = "Uploading picture"
                        if(progress == 100L){
                            Toast.makeText(context, "Profile image changed and saved", Toast.LENGTH_SHORT).show()
                            Toast.makeText(context,"Photo will be updated after app restart",Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            viewModel.uploadProgress.value = 0L
                        }
                    })
                }

            }
        }

        binding.profileImageView.setOnClickListener{

            val dialog = AlertDialog.Builder(context)
                .setPositiveButton("Select new image") {_,_->
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    resultLauncher.launch(intent)
                }
                .setNeutralButton("Show image"){_,_->
                    val intent = Intent(requireActivity(), PhotoActivity::class.java)
                    intent.putExtra("folder","userProfileImages")
                    intent.putExtra("id",FirebaseAuth.getInstance().currentUser!!.uid)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel",null)
                .show()

        }








        return binding.root
    }




}