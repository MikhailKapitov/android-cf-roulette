package com.example.cf_roulette

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cf_roulette.data.Problem
import com.example.cf_roulette.repository.ProblemRepository
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

        val problemRepository = ProblemRepository.getInstance(applicationContext)


        // IK that this is terrible code. Just testing things.
        lifecycleScope.launch {

            Log.d("TrashTest", "Clearing cache...")
            Log.d("TrashTest", "Cache cleared: " + problemRepository.deleteCache().toString())

            Log.d("TrashTest", "Generating [800; 1200]...")
            var problem : Problem? = problemRepository.getProblem(800, 1200)
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
            Log.d("TrashTest", "Cache update:" + problemRepository.updateCache().toString())

            problem = problemRepository.getProblem(800, 1200, 123)
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

            problem = problemRepository.getProblem(800, 1200, 123)
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

            problem = problemRepository.getProblem(800, 1200)
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
        }

    }
}