package com.example.cf_roulette.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cf_roulette.R
import com.example.cf_roulette.data.Task

class TaskGeneratorPageFragment :Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.task_generator_page_fragment, container, false)
        val placeholder = view.findViewById<FrameLayout>(R.id.task_placeholder)

        placeholder.removeAllViews()

        val task = Task(1337, "Solve problem A", 0, "https://codeforces.com/problemset/problem/1/A")

        val taskCard = layoutInflater.inflate(R.layout.item_task, placeholder, false)
        taskCard.findViewById<TextView>(R.id.task_id).text = "ID: ${task.id}"
        taskCard.findViewById<TextView>(R.id.task_name).text = task.name

        // Скрыть статус
        taskCard.findViewById<TextView>(R.id.task_status).visibility = View.GONE

        // Обработчик нажатия
        taskCard.setOnClickListener {
            task.link?.let {
                copyToClipboard(it)
                Toast.makeText(requireContext(), "Ссылка скопирована!", Toast.LENGTH_SHORT).show()
            }
        }

        placeholder.addView(taskCard)

        return view
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Task Link", text)
        clipboard.setPrimaryClip(clip)
    }
}