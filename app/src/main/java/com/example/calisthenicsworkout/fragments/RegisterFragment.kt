package com.example.calisthenicsworkout.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.calisthenicsworkout.MainActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.databinding.FragmentRegisterBinding
import com.example.calisthenicsworkout.viewmodels.AuthViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class RegisterFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentRegisterBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_register, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        binding.authViewModel = viewModel
        binding.lifecycleOwner = this


        binding.alreadyRegisteredText.setOnClickListener{ view: View ->
            view.findNavController().navigate(
                RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            )
        }

        binding.registerButton.setOnClickListener{
            if(checkInputsFilled(binding)){
                if (checkPasswordEqual(binding.inputPassword.text.toString(),binding.inputPasswordAgain.text.toString())){
                    val email: String = binding.inputEmail.text.toString().trim{it <= ' ' }
                    val password: String = binding.inputPassword.text.toString().trim{it <= ' ' }
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (task.isSuccessful){
                                val firebaseUser: FirebaseUser = task.result!!.user!!
                                Toast.makeText(context,"Registered successfully",Toast.LENGTH_SHORT).show()
                                val intent = Intent(context,MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                val userId = FirebaseAuth.getInstance().currentUser!!.uid
                                addUserToFirebase(binding.inputEmail.text.toString(),binding.inputName.text.toString(),userId)
                                pairSkillToUser(userId)
                                startActivity(intent)
                                requireActivity().finish()
                            }else{
                                Toast.makeText(context,task.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                            }
                        })

                }else{
                    Toast.makeText(context,"Passwords are not the same",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context,"Fill out everything please",Toast.LENGTH_SHORT).show()
            }
        }


        return binding.root
    }

    private fun pairSkillToUser(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("skills").get().addOnCompleteListener() {
            if(it.isSuccessful){
                for(skill in it.result!!){
                    val skillId = skill.id
                    val mappedThing: MutableMap<String,Any> = HashMap()
                    mappedThing["skillId"] = skillId
                    mappedThing["userId"] = userId
                    mappedThing["liked"] = false
                    db.collection("userAndSkillCrossRef").add(mappedThing)
                }
            }
        }
    }

    private fun addUserToFirebase(email: String, name: String,id: String) {
            val db = FirebaseFirestore.getInstance()
            val mappedThing: MutableMap<String,Any> = HashMap()
            mappedThing["userFullName"] = name
            mappedThing["userEmail"] = email
            db.collection("users").document(id).set(mappedThing)
    }

    private fun checkPasswordEqual(pass1: String, pass2: String): Boolean {
        return pass1 == pass2
    }

    private fun checkInputsFilled(binding: FragmentRegisterBinding): Boolean {
        if(TextUtils.isEmpty(binding.inputEmail.text.toString().trim{it <= ' ' })){
            return false
        }
        if(TextUtils.isEmpty(binding.inputName.text.toString().trim{it <= ' ' })){
            return false
        }
        if(TextUtils.isEmpty(binding.inputPassword.text.toString().trim{it <= ' ' })){
            return false
        }
        if(TextUtils.isEmpty(binding.inputPasswordAgain.text.toString().trim{it <= ' ' })){
            return false
        }

        return true
    }

}