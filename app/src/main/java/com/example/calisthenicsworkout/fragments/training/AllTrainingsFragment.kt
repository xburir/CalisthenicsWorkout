package com.example.calisthenicsworkout.fragments.training

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.TrainingListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.FragmentAllTrainingsBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


class AllTrainingsFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAllTrainingsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_all_trainings, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this

        val adapter = TrainingListAdapter(TrainingListAdapter.TrainingListener {
                trainingId -> viewModel.onTrainingClicked(trainingId)
        })
        val manager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = manager
        binding.recyclerView.adapter = adapter

        viewModel.allTrainings.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        viewModel.chosenTrainingId.observe(viewLifecycleOwner, { training ->
            training?.let {
                this.findNavController().navigate(
                    AllTrainingsFragmentDirections.actionAllTrainingsFragmentToTrainingFragment(
                        training
                    )
                )
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_bar_all_trainings,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.toString() == "Add Training"){
            val input = EditText(context)
            input.hint = "Enter Text"
            input.inputType = InputType.TYPE_CLASS_TEXT
            AlertDialog.Builder(context)
                .setTitle("Add Training")
                .setView(input)
                .setPositiveButton("OK") {_,_->
                    val id = input.text.toString()
                    addTraining(id)
                }
                .setNegativeButton("Cancel",null)
                .show()
        }

        return super.onOptionsItemSelected(item)
    }

    fun addTraining(trainingId: String){
        val db = FirebaseFirestore.getInstance()
        val fbStorage = FirebaseStorage.getInstance()
        db.collection("trainings").get().addOnCompleteListener{
            if(it.isSuccessful){
                var found = false
                for(entry in it.result!!){
                    if(entry.id == trainingId){
                        found = true
                        val id = entry.id
                        val name = entry.data.getValue("name").toString()
                        val owner = entry.data.getValue("owner").toString()
                        val target = entry.data.getValue("target").toString()
                        val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                        val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.nothing)
                        val training = Training(name,target,id,owner,bitmap,numberOfExercises)
                        val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                        pictureRef.downloadUrl
                            .addOnSuccessListener {
                                viewModel.viewModelScope.launch {
                                    training.image = getBitmap(it)
                                    viewModel.addTrainingToDatabase(training)
                                }
                            }
                            .addOnFailureListener {
                                viewModel.viewModelScope.launch {
                                    viewModel.addTrainingToDatabase(training)
                                }
                            }
                    }
                }
                if(!found){
                    Toast.makeText(context,"Training not found",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getBitmap(source: Uri): Bitmap {
        val loading = ImageLoader(requireContext())
        val request = ImageRequest.Builder(requireContext()).data(source).build()
        val result = (loading.execute(request) as SuccessResult).drawable
        return (result as BitmapDrawable).bitmap
    }
}