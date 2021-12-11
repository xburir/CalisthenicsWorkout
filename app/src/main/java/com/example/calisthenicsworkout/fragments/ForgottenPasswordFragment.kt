package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.databinding.FragmentForgottenPasswordBinding
import com.example.calisthenicsworkout.databinding.FragmentLogin2Binding
import com.google.firebase.auth.FirebaseAuth


class ForgottenPasswordFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentForgottenPasswordBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_forgotten_password, container, false)


        binding.resetButton.setOnClickListener{
            val email = binding.inputResetEmail.text.toString().trim{it <= ' '}
            if (email.isEmpty()){
                Toast.makeText(context,"Please enter your email",Toast.LENGTH_SHORT).show()
            }else{
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener{
                    if(it.isSuccessful){
                        Toast.makeText(context,"An email was sent to you with a link to reset your password",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(context,it.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return binding.root
    }



}