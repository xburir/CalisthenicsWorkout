package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.databinding.FragmentAboutBinding
import android.content.Intent
import android.net.Uri
import android.widget.Toast


class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_about,container, false)


        binding.facebookIcon.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/risko003"))
            startActivity(browserIntent)
        }

        binding.emailIcon.setOnClickListener{
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.type = "text/plain"

            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "CalisthenicsWorkout")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("richardburi35@gmail.com"))
            try {
                startActivity(Intent.createChooser(emailIntent, "Choose Email Client..."))
            }
            catch (e: Exception){
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }

        }

        return binding.root

    }


}