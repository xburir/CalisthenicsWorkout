package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.databinding.FragmentRegisterBinding
import com.example.calisthenicsworkout.viewmodels.AuthViewModel


class LoginFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentRegisterBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_login, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        binding.authViewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

}