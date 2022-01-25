package com.example.calisthenicsworkout.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
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
import com.example.calisthenicsworkout.WelcomeActivity
import com.example.calisthenicsworkout.databinding.FragmentLogin2Binding
import com.example.calisthenicsworkout.util.PrefUtil
import com.google.firebase.auth.FirebaseAuth





class Login2Fragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentLogin2Binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_login2, container, false)

        binding.lifecycleOwner = this

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val i = Intent(context, WelcomeActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            requireActivity().finish()
        }



        binding.notRegisteredText.setOnClickListener{ view: View ->
            view.findNavController().navigate(
                Login2FragmentDirections.actionLoginFragmentToRegisterFragment()
            )
        }

        binding.forgottenPassword.setOnClickListener{
            it.findNavController().navigate(
                Login2FragmentDirections.actionLoginFragmentToForgottenPasswordFragment()
            )
        }

        binding.loginButton.setOnClickListener{
            if(checkInputsFilled(binding)){
                val email: String = binding.inputLoginEmail.text.toString().trim{it <= ' ' }
                val password: String = binding.inputLoginPassword.text.toString().trim{it <= ' ' }
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            PrefUtil.setLoadSettings(true,requireContext())
                            startActivity(intent)
                            requireActivity().finish()
                        }else{
                            Toast.makeText(context,task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            }else{
                    Toast.makeText(context,"Fill out everything please", Toast.LENGTH_SHORT).show()
            }
        }


        return binding.root
    }

    private fun checkInputsFilled(binding: FragmentLogin2Binding): Boolean {
        if(TextUtils.isEmpty(binding.inputLoginEmail.text.toString().trim{it <= ' ' })){
            return false
        }
        if(TextUtils.isEmpty(binding.inputLoginPassword.text.toString().trim{it <= ' ' })){
            return false
        }

        return true
    }
}