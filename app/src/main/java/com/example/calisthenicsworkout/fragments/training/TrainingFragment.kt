package com.example.calisthenicsworkout.fragments.training

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.PhotoActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.TimerActivity
import com.example.calisthenicsworkout.adapters.ExerciseListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.FragmentTrainingBinding
import com.example.calisthenicsworkout.util.PrefUtil
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext





class TrainingFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory
    private lateinit var trainingOnScreenId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentTrainingBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_training,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this


        setHasOptionsMenu(true)



        val adapter = ExerciseListAdapter(ExerciseListAdapter.ExerciseListener {
                skillId -> viewModel.onSkillClicked(skillId)
        })
        val manager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = manager
        binding.recyclerView.adapter = adapter



        changeTrainingOnFragment(binding,viewModel.lastViewedTrainingId,adapter)


        viewModel.chosenSkillId.observe(viewLifecycleOwner, { skill->
            skill?.let {
                this.findNavController().navigate(
                    TrainingFragmentDirections.actionTrainingFragmentToSkillFragment(
                        skill
                    )
                )
            }
        })

        binding.startTimerButton.setOnClickListener{
            val intent = Intent(context, TimerActivity::class.java)
            PrefUtil.setTrainingId(trainingOnScreenId,requireContext())
            startActivity(intent)
        }

        binding.trainingImageInFragment.setOnClickListener{
            val intent = Intent(requireActivity(), PhotoActivity::class.java)
            intent.putExtra("folder","trainingImages")
            intent.putExtra("id",trainingOnScreenId)
            startActivity(intent)
        }


        return binding.root
    }

    private fun changeTrainingOnFragment(binding: FragmentTrainingBinding, training: String, adapter: ExerciseListAdapter) {
        viewModel.viewModelScope.launch {
            withContext(Dispatchers.IO){
                trainingOnScreenId = viewModel.database.getTraining(training).id
                binding.training = viewModel.database.getTraining(training)
            }
        }

        viewModel.database.getExercisesOfTraining(viewModel.lastViewedTrainingId).observe(viewLifecycleOwner,{  exercises ->
            val exerciseList = arrayListOf<Exercise>()
            exercises.forEach { exercise ->
                if(exercise.trainingId == viewModel.lastViewedTrainingId) {
                    exerciseList.add(exercise)
                }
            }
            exerciseList.sortBy { it.order }
            adapter.submitList(exerciseList)
            viewModel.onTrainingNavigated()
        })

    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_bar_training,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.toString() == "Delete Training"){
            viewModel.allTrainings.observe(viewLifecycleOwner,{
                it.forEach { training ->
                    if (training.id == trainingOnScreenId){
                        when (training.owner) {
                            FirebaseAuth.getInstance().currentUser!!.uid -> {
                                deleteOwnTraining()
                            }
                            "admin" -> {
                                Toast.makeText(context,"You can't delete this training",Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                deleteFollowedTraining()
                            }
                        }
                    }
                }
            })
        }else if(item.toString() == "Share Training"){
            copyTraining()
        }else if(item.toString() == "Upload Training"){
            uploadTraining()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun uploadTraining() {
        viewModel.viewModelScope.launch {
            withContext(Dispatchers.IO){
                val training = viewModel.database.getTraining(trainingOnScreenId)
                if(training.owner == FirebaseAuth.getInstance().currentUser!!.uid){
                    val database = FirebaseFirestore.getInstance()
                    val mappedTraining: MutableMap<String,Any> = HashMap()
                    mappedTraining["name"] = training.name
                    mappedTraining["owner"] = training.owner
                    mappedTraining["numberOfExercises"] = training.numberOfExercises
                    mappedTraining["target"] = training.target
                    database.collection("trainings").document(training.id).set(mappedTraining).addOnFailureListener {
                        requireActivity().runOnUiThread {
                            Toast.makeText(context,"Saving training to online database failed, upload it later with good internet connection",Toast.LENGTH_SHORT).show()
                        }
                    }
                    val exerciseList = viewModel.database.getExercisesOfTrainingDirect(trainingOnScreenId)
                    exerciseList.forEach{
                        val mappedExercise: MutableMap<String,Any> = HashMap()
                        mappedExercise["reps"] = it.repetitions
                        mappedExercise["sets"] = it.sets
                        mappedExercise["order"] = it.order
                        mappedExercise["skillId"] = it.skillId
                        mappedExercise["trainingId"] = it.trainingId
                        database.collection("exercises").add(mappedExercise).addOnFailureListener{
                            requireActivity().runOnUiThread {
                                Toast.makeText(context,"Saving exercise to online database failed, upload it later with good internet connection",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }else{
                    requireActivity().runOnUiThread {
                        Toast.makeText(context,"This is not your training to upload",Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }

    }

    private fun copyTraining() {
        val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", trainingOnScreenId)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context,"Training ID copied to clipboard",Toast.LENGTH_SHORT).show()
    }

    private fun deleteFollowedTraining() {
        AlertDialog.Builder(context)
            .setMessage("Are you sure you want to delete this training?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                viewModel.viewModelScope.launch {
                    withContext(Dispatchers.IO){
                        viewModel.database.deleteTrainingExercises(trainingOnScreenId)
                        viewModel.database.deleteTraining(trainingOnScreenId)
                    }
                }
                findNavController().navigate(TrainingFragmentDirections.actionTrainingFragmentToHomeFragment())
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteOwnTraining() {
        val db = FirebaseFirestore.getInstance()
        AlertDialog.Builder(context)
            .setMessage("Are you sure you want to delete this training?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                viewModel.viewModelScope.launch {
                    withContext(Dispatchers.IO){
                        viewModel.database.deleteTrainingExercises(trainingOnScreenId)
                        viewModel.database.deleteTraining(trainingOnScreenId)
                    }
                }
                db.collection("exercises").whereEqualTo("trainingId",trainingOnScreenId).get().addOnCompleteListener{
                    if(it.isSuccessful){
                        for(entry in it.result!!){
                            db.collection("exercises").document(entry.id).delete()
                        }
                    }
                }
                db.collection("trainings").document(trainingOnScreenId).delete()
                FirebaseStorage.getInstance().reference.child("trainingImages").child("${trainingOnScreenId}.png").delete()

                findNavController().navigate(TrainingFragmentDirections.actionTrainingFragmentToHomeFragment())
            }
            .setNegativeButton("No", null)
            .show()
    }

}