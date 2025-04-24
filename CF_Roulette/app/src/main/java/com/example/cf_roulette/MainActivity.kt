package com.example.cf_roulette

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
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

        lifecycleScope.launch {
            Log.d("CF", "Requesting...")
            val problem = ProblemRepository.getProblem(800, 1200)
            problem?.let {
                if(problem.name != null){
                    Log.d("CF", problem.name)
                }
                else{
                    Log.d("CF", "Problem found, no name though.")
                }
                // Nice!
            } ?: run {
                Log.d("CF", "No problem found (That is not a good thing LOL).")
                // Null...
            }
        }
    }
}