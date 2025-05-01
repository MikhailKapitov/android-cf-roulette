package com.example.cf_roulette.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cf_roulette.R
import com.example.cf_roulette.data.Task
import java.util.*
import java.util.concurrent.TimeUnit

class DailyTasksPageFragment : Fragment() {

    private var countdownTimer: CountDownTimer? = null
    private lateinit var timerTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.daily_tasks_page_fragment, container, false)
        timerTextView = view.findViewById(R.id.timer_text)
        recyclerView = view.findViewById(R.id.item_list)

        setupRecyclerView()
        startUtcCountdown()

        return view
    }

    private fun setupRecyclerView() {
        val defaultTasks = listOf(
            Task(1, "Two Sum", 0, "https://codeforces.com/problemset/problem/1/A"),
            Task(2, "Binary Search", 1, "https://codeforces.com/problemset/problem/2/B"),
            Task(3, "Graph Paths", 2, "https://codeforces.com/problemset/problem/3/C"),
            Task(4, "Suffix Array", 3, "https://codeforces.com/problemset/problem/4/D")
        )

        taskAdapter = TaskAdapter(defaultTasks) { task ->
            task.link?.let {
                copyToClipboard(it)
                Toast.makeText(requireContext(), "Ссылка скопирована!", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = taskAdapter
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Task Link", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun startUtcCountdown() {
        val nowUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val nextMidnightUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = nowUtc.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val millisUntilMidnight = nextMidnightUtc.timeInMillis - nowUtc.timeInMillis

        countdownTimer = object : CountDownTimer(millisUntilMidnight, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "00:00:00"
                startUtcCountdown()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownTimer?.cancel()
    }
}



class TaskAdapter(
    private val tasks: List<Task>,
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitle: TextView = view.findViewById(R.id.task_name)
        val taskStatus: TextView = view.findViewById(R.id.task_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTitle.text = task.name
        holder.taskStatus.text = when (task.status) {
            0 -> "Didn't try"
            1 -> "Decided"
            2 -> "Didn't decide"
            3 -> "Don't know yet"
            else -> "Unknown"
        }

        holder.taskStatus.setTextColor(
            when (task.status) {
                1 -> Color.parseColor("#4CAF50") // Зеленый — решил
                2 -> Color.parseColor("#F44336") // Красный — не решил
                0 -> Color.parseColor("#9E9E9E") // Серый — не пытался
                3 -> Color.parseColor("#FF9800") // Оранжевый — пока не знаем
                else -> Color.BLACK
            }
        )

        holder.itemView.setOnClickListener { onItemClick(task) }
    }

    override fun getItemCount() = tasks.size
}