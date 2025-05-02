package com.example.cf_roulette.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cf_roulette.R
import com.example.cf_roulette.data.Problem
import com.example.cf_roulette.repository.ProblemRepository
import com.example.cf_roulette.repository.UserStatusRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class DailyTasksPageFragment : Fragment() {

    private var countdownTimer: CountDownTimer? = null
    private lateinit var timerTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var problemRepository: ProblemRepository
    private lateinit var userStatusRepository: UserStatusRepository
    private lateinit var sharedPref: SharedPreferences
    private lateinit var validProblems:List<Problem>
    private lateinit var btnUpdateStatus: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.daily_tasks_page_fragment, container, false)
        timerTextView = view.findViewById(R.id.timer_text)
        recyclerView = view.findViewById(R.id.item_list)
        btnUpdateStatus = view.findViewById(R.id.btn_update_status)
        sharedPref=requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        problemRepository = ProblemRepository.getInstance(requireContext())
        userStatusRepository = UserStatusRepository.getInstance(requireContext())

        setupRecyclerView(emptyList())

        startUtcCountdown()

        loadDailyTasks()

        startDailyTaskUpdate()

        startStatusUpdate()
        btnUpdateStatus.setOnClickListener {
            checkTasksStatus()
        }
        return view
    }

    private fun setupRecyclerView(problems: List<Problem>) {
        taskAdapter = TaskAdapter(problems) { problem ->
            val url = "https://codeforces.com/problemset/problem/${problem.contestId}/${problem.index}"
            copyToClipboard(url)
            Toast.makeText(requireContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = taskAdapter
    }

    private fun loadDailyTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val problems = problemRepository.getDailyProblems()
                validProblems = problems.filterNotNull()
                taskAdapter.updateProblems(validProblems)
                checkTasksStatus()
                if (validProblems.isEmpty()) {
                    Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading tasks: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startDailyTaskUpdate() {
        val dailyUpdateTimer = object : CountDownTimer(24 * 60 * 60 * 1000, 24 * 60 * 60 * 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                updateTasks()
                startDailyTaskUpdate()
            }
        }.start()
    }

    private fun updateTasks() {
        if (isAdded) {
        loadDailyTasks()
        Toast.makeText(requireContext(), "Tasks have been updated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startStatusUpdate() {
        val statusUpdateTimer = object : CountDownTimer(60 * 1000, 60 * 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                checkTasksStatus()
                startStatusUpdate()
            }
        }.start()
    }

    private fun checkTasksStatus() {
        if (!isAdded) return

        var username = sharedPref.getString("cf_nickname", "").orEmpty()
        if (username.isBlank()) username = " "

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val statuses = userStatusRepository.checkStatus(username, validProblems)
                Log.d("StatusInfo", statuses.toString())
                taskAdapter.updateStatuses(statuses)
                Toast.makeText(requireContext(), "Task statuses have been updated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.d("StatusInfo", "Failed to get statuses: ${e.message}")
                Toast.makeText(requireContext(),
                    "Failed to get statuses: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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
    private var problems: List<Problem> = emptyList(),
    private val onItemClick: (Problem) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var statuses: List<Int> = emptyList()

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskId: TextView    = view.findViewById(R.id.task_id)
        val taskTitle: TextView = view.findViewById(R.id.task_name)
        val taskStatus: TextView= view.findViewById(R.id.task_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount() = problems.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val problem = problems[position]

        holder.taskId.text    = "ID: ${problem.contestId ?: "N/A"}${problem.index ?: ""}"
        holder.taskTitle.text = problem.name ?: "No title"

        val code = statuses.getOrNull(position) ?: 3
        val (label, color) = when (code) {
            0 -> "Unattempted" to Color.GRAY
            1 -> "Solved"      to Color.GREEN
            2 -> "Failed"      to Color.RED
            else -> "Unknown"  to Color.DKGRAY
        }

        holder.taskStatus.text = label

        holder.taskStatus.setTextColor(color)
        holder.itemView.setOnClickListener {
            onItemClick(problem)
        }

        holder.itemView.setOnLongClickListener {
            showPopupMenu(it.context, it, problem)
            true
        }

        holder.itemView.setOnClickListener { onItemClick(problem) }
    }

    fun updateStatuses(newStatuses: List<Int>) {
        statuses = newStatuses
        notifyDataSetChanged()
    }

    fun updateProblems(newProblems: List<Problem>) {
        problems = newProblems
        statuses = List(newProblems.size) { 3 }
        notifyDataSetChanged()
    }
    private fun showPopupMenu(context: Context, anchor: View, problem: Problem) {
        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.problem_popup_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            val url = "https://codeforces.com/problemset/problem/${problem.contestId}/${problem.index}"
            when (item.itemId) {
                R.id.menu_copy -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Task Link", url)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
                R.id.menu_open -> {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse(url)
                    }
                    context.startActivity(intent)
                }
            }
            true
        }

        popup.show()
    }

}
