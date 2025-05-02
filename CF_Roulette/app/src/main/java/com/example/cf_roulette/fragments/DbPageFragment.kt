package com.example.cf_roulette.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cf_roulette.R
import com.example.cf_roulette.repository.ProblemRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DbPageFragment : Fragment() {
    private lateinit var dbHash1: TextView
    private lateinit var dbHash2: TextView
    private lateinit var lastUpdText: TextView
    private lateinit var problemRepository: ProblemRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.db_page_fragment, container, false)

        dbHash1 = view.findViewById(R.id.db_hash_1)
        dbHash2 = view.findViewById(R.id.db_hash_2)
        lastUpdText=view.findViewById(R.id.text_last_update)
        problemRepository = ProblemRepository.getInstance(requireContext())

        val updateButton: Button = view.findViewById(R.id.btn_last_update)
        updateButton.setOnClickListener {
            updateHash()
        }

        showSavedTime()
        displayHash()

        return view
    }

    private fun displayHash(){

        viewLifecycleOwner.lifecycleScope.launch {

            var problemset = problemRepository.getProblemset(800, 3500, 0, listOf(), specialTagBanned = false, tagOring = false)
            if (problemset == null){
                dbHash1.text = "!!!"
                dbHash2.text = "!!!"
                return@launch
            }
            dbHash1.text = problemRepository.hashProblemList(problemset) + " (" + problemset.size.toString() + ")"

            problemset = problemRepository.getProblemset(800, 3500, 128, listOf(), specialTagBanned = false, tagOring = false)
            if (problemset == null){
                dbHash2.text = "!!!"
                return@launch
            }
            dbHash2.text = problemRepository.hashProblemList(problemset) + " (" + problemset.size.toString() + ")"

        }

    }

    private fun updateHash() {

        viewLifecycleOwner.lifecycleScope.launch {

            if(!problemRepository.updateCache()){
                return@launch
            }

            val currentTime = System.currentTimeMillis()
            val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putLong("last_click_time", currentTime).apply()
            showSavedTime()

            displayHash()
        }

    }

    private fun showSavedTime() {
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedTime = sharedPrefs.getLong("last_click_time", 0L)

        if (savedTime != 0L) {
            val formatted = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                .format(Date(savedTime))
            lastUpdText.text = formatted
        }
    }
}