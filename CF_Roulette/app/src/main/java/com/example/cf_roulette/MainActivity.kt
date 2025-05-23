package com.example.cf_roulette

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cf_roulette.data.Contest
import com.example.cf_roulette.data.Problem
import com.example.cf_roulette.fragments.RegistrationPageFragment
import com.example.cf_roulette.repository.ContestRepository
import com.example.cf_roulette.repository.ProblemRepository
import com.example.cf_roulette.repository.UserStatusRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)


        
        // IK that this is terrible code. Just testing things.

        val isDebugOn = false

        if (isDebugOn){

            val problemRepository = ProblemRepository.getInstance(applicationContext)
            val contestRepository = ContestRepository.getInstance(applicationContext)
            val userStatusRepository = UserStatusRepository.getInstance(applicationContext)

            lifecycleScope.launch {

                Log.d("TrashTest", "Clearing cache...")
                Log.d("TrashTest", "Cache cleared: " + problemRepository.deleteCache().toString())

                Log.d("TrashTest", "Generating problem...")
                var problem : Problem? = problemRepository.getProblem(800, 1200, 128, listOf("implementation", "brute force"), tagOring = false, specialTagBanned = true)
                if (problem != null) {
                    val problemName = problem.name
                    if (problemName != null) {
                        Log.d("TrashTest", problemName)
                    } else {
                        Log.d("TrashTest", "Problem found, no name though.")
                    }
                } else {
                    Log.d("TrashTest", "No problem found (That is not a good thing LOL).")
                }

                Log.d("TrashTest", "Updating cache...")
                Log.d("TrashTest", "Cache update: " + problemRepository.updateCache().toString())

                Log.d("TrashTest", "Generating problem...")
                problem = problemRepository.getProblem(800, 1200, 128, listOf("implementation", "brute force"), tagOring = false, specialTagBanned = true, 123)
                if (problem != null) {
                    val problemName = problem.name
                    if (problemName != null) {
                        Log.d("TrashTest", problemName)
                    } else {
                        Log.d("TrashTest", "Problem found, no name though.")
                    }
                } else {
                    Log.d("TrashTest", "No problem found (That is not a good thing LOL).")
                }

                Log.d("TrashTest", "Generating problem...")
                problem = problemRepository.getProblem(800, 1200, 128, listOf("implementation", "brute force"), tagOring = false, specialTagBanned = true, 123)
                if (problem != null) {
                    val problemName = problem.name
                    if (problemName != null) {
                        Log.d("TrashTest", problemName)
                    } else {
                        Log.d("TrashTest", "Problem found, no name though.")
                    }
                } else {
                    Log.d("TrashTest", "No problem found (That is not a good thing LOL).")
                }

                Log.d("TrashTest", "Generating problem...")
                problem = problemRepository.getProblem(800, 1200, 128, listOf("implementation", "brute force"), tagOring = false, specialTagBanned = true)
                if (problem != null) {
                    val problemName = problem.name
                    if (problemName != null) {
                        Log.d("TrashTest", problemName)
                    } else {
                        Log.d("TrashTest", "Problem found, no name though.")
                    }
                } else {
                    Log.d("TrashTest", "No problem found (That is not a good thing LOL).")
                }



                Log.d("TrashTest", "Clearing contest cache...")
                Log.d("TrashTest", "Cache cleared: " + contestRepository.deleteCache().toString())

                Log.d("TrashTest", "Getting contest...")
                var contest: Contest? = contestRepository.getContest(123)
                if (contest != null) {
                    Log.d("TrashTest", "Found contest: " + contest.name)
                } else {
                    Log.d("TrashTest", "No contest found.")
                }

                Log.d("TrashTest", "Updating contest cache...")
                Log.d("TrashTest", "Cache update: " + contestRepository.updateCache().toString())

                Log.d("TrashTest", "Getting contest...")
                contest = contestRepository.getContest(123)
                if (contest != null) {
                    Log.d("TrashTest", "Found contest: " + contest.name)
                } else {
                    Log.d("TrashTest", "No contest found .")
                }



                Log.d("TrashTest", "Hashing!")
                var problemset = problemRepository.getProblemset(800, 3500, 0, listOf(), specialTagBanned = false, tagOring = false)
                if (problemset == null){
                    Log.d("TrashTest", "No problemset...")
                }
                else{
                    Log.d("TrashTest", "Hash unfiltered: " + problemRepository.hashProblemList(problemset))
                    Log.d("TrashTest", "Len: " + problemset.size)
                }
                Log.d("TrashTest", "Hashing!")
                problemset = problemRepository.getProblemset(800, 3500, 128, listOf(), specialTagBanned = false, tagOring = false)
                if (problemset == null){
                    Log.d("TrashTest", "No problemset...")
                }
                else{
                    Log.d("TrashTest", "Hash filtered: " + problemRepository.hashProblemList(problemset))
                    Log.d("TrashTest", "Len: " + problemset.size)
                }



                Log.d("TrashTest", "Daily!")
                val daily = problemRepository.getDailyProblems()

                for (dailyProblem in daily){
                    Log.d("TrashTest", dailyProblem?.name ?: "IDK MAN")
                }

            }

        }

    }
}