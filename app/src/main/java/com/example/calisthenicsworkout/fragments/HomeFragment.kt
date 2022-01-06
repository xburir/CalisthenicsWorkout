package com.example.calisthenicsworkout.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.AuthActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.VideoActivity
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.example.calisthenicsworkout.databinding.FragmentHomeBinding
import com.example.calisthenicsworkout.fragments.skill.SkillFragmentDirections
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModel
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModelFactory
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var viewModel: FetchDataViewModel
    private lateinit var viewModelFactory: FetchDataViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_home, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = FetchDataViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(this,viewModelFactory).get(FetchDataViewModel::class.java)

        setHasOptionsMenu(true)


        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_bar_home,menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.toString() == "Read Online Data"){

            val status = TextView(context)
            val dialog = AlertDialog.Builder(context)
                .setTitle("Progress")
                .setView(status)
                .setCancelable(false)
                .show()

            viewModel.finished.observe(viewLifecycleOwner,{
                it?.let { string ->
                    status.text = string
                    if (string == "All done"){
                        dialog.dismiss()
                        Toast.makeText(context,"Data downloaded",Toast.LENGTH_SHORT).show()
                    }else if( string == "Starting"){
                        dialog.show()
                    }
                }
            })


            viewModel.readFireStoreData(requireActivity())


        }
        return super.onOptionsItemSelected(item)
    }






}