package com.example.calisthenicsworkout.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.AuthActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentProfileBinding
import com.example.calisthenicsworkout.viewmodels.AuthViewModel
import com.example.calisthenicsworkout.viewmodels.AuthViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var viewModelFactory: AuthViewModelFactory
    private lateinit var binding: FragmentProfileBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_profile,container,false)


        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = AuthViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(AuthViewModel::class.java)
        binding.lifecycleOwner = this

        viewModel.database.getUser(FirebaseAuth.getInstance().currentUser!!.uid).observe(viewLifecycleOwner,{
            it?.let { user ->
                binding.fullUserName.text = "Full Name: " + user.userFullName
                binding.userEmail.text = "UID: "+ user.userEmail
                binding.userId.text = "Email: " + user.userId
                binding.profileImageView.setImageBitmap(user.userImage)
            }
        })



        binding.logoutButton.setOnClickListener {
            viewModel.logout(Intent(context, AuthActivity::class.java),requireActivity())
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    viewModel.saveProfilePic(it,requireContext())
                }

            }
        }

        binding.profileImageView.setOnClickListener{

            AlertDialog.Builder(context)
                .setPositiveButton("Select new image") {_,_->
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    resultLauncher.launch(intent)
                }
                .setNeutralButton("Show image"){_,_->
                    Toast.makeText(context,"Showing image",Toast.LENGTH_SHORT).show()
                }

                .setNegativeButton("Cancel",null)

                .show()



        }



        return binding.root
    }




}