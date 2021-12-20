package com.example.calisthenicsworkout.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.AuthActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_home, container, false)


        getUser(binding)

        binding.logoutButton.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(context, AuthActivity::class.java))
            requireActivity().finish()
        }

        return binding.root
    }

    private fun getUser(binding: FragmentHomeBinding) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").get().addOnCompleteListener{
            if(it.isSuccessful){
                val loggedUser = FirebaseAuth.getInstance().currentUser!!.uid
                for(user in it.result!!){
                    if(loggedUser == user.id){
                        binding.userName.text = user.data.getValue("userFullName").toString()
                    }
                }
            }
        }
    }


}