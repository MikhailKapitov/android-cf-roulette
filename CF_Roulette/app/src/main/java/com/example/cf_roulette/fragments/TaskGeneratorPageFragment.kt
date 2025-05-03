package com.example.cf_roulette.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cf_roulette.R
import com.example.cf_roulette.repository.ProblemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskGeneratorPageFragment : Fragment() {
    private lateinit var problemRepository: ProblemRepository
    private val codeforcesTags = listOf(
        "*special", "2-sat", "binary search", "bitmasks", "brute force",
        "chinese remainder theorem", "combinatorics", "constructive algorithms",
        "data structures", "dfs and similar", "divide and conquer", "dp", "dsu",
        "expression parsing", "fft", "flows", "games", "geometry", "graph matchings",
        "graphs", "greedy", "hashing", "implementation", "interactive", "math",
        "matrices", "meet-in-the-middle", "number theory", "probabilities",
        "schedules", "shortest paths", "sortings", "string suffix structures",
        "strings", "ternary search", "trees", "two pointers"
    )
    private var specialTagBanned = true
    private var tagOring = true


    private val selectedTags = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.task_generator_page_fragment, container, false)
        val clearTagsButton = view.findViewById<Button>(R.id.clearTagsButton)
        val placeholder = view.findViewById<FrameLayout>(R.id.task_placeholder)
        val minEditText = view.findViewById<EditText>(R.id.minDifficulty)
        val maxEditText = view.findViewById<EditText>(R.id.maxDifficulty)
        val tagSelector = view.findViewById<Spinner>(R.id.tagSelector)
        val tagsTitle = view.findViewById<TextView>(R.id.tagsTitle)
        val generateButton = view.findViewById<Button>(R.id.rouletteButton)
        val specialProblemButton = view.findViewById<Button>(R.id.specialProblemButton)
        val andOrButton = view.findViewById<Button>(R.id.andOrButton)

        problemRepository = ProblemRepository.getInstance(requireContext())

        clearTagsButton.setOnClickListener {
            selectedTags.clear()
            updateTagsTitle(tagsTitle)
            Toast.makeText(requireContext(), "Tags cleared", Toast.LENGTH_SHORT).show()
        }

        specialProblemButton.setOnClickListener {
            specialTagBanned = !specialTagBanned
            if (specialTagBanned) {
                specialProblemButton.text = "⛔ Special OFF"
                specialProblemButton.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
            } else {
                specialProblemButton.text = "✅ Special ON"
                specialProblemButton.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
            }
        }

        andOrButton.setOnClickListener {
            tagOring = !tagOring
            andOrButton.text = if (tagOring) "OR" else "AND"
            andOrButton.setBackgroundColor(
                resources.getColor(
                    if (tagOring) android.R.color.holo_blue_light
                    else android.R.color.holo_orange_light
                )
            )
        }

        val tagAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Select a tag") + codeforcesTags
        )
        tagSelector.adapter = tagAdapter

        tagSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, viewItem: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                if (selected != "Select a tag" && selectedTags.add(selected)) {
                    updateTagsTitle(tagsTitle)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        generateButton.setOnClickListener {
            val min = minEditText.text.toString().toIntOrNull() ?: 800
            val max = maxEditText.text.toString().toIntOrNull() ?: 3500
            val goodTags = selectedTags.toList()


            lifecycleScope.launch {
                val problem = problemRepository.getProblem(
                    lowerBound = min,
                    upperBound = max,
                    dayCount = 128,
                    goodTags = goodTags,
                    tagOring = tagOring,
                    specialTagBanned = specialTagBanned
                )


                withContext(Dispatchers.Main) {
                    placeholder.removeAllViews()
                    if (problem != null) {
                        val taskCard = layoutInflater.inflate(R.layout.item_task, placeholder, false)
                        taskCard.findViewById<TextView>(R.id.task_id).text = "ID: ${problem.contestId}${problem.index}"
                        taskCard.findViewById<TextView>(R.id.task_name).text = problem.name
                        taskCard.findViewById<TextView>(R.id.task_status).visibility = View.GONE

                        taskCard.setOnClickListener { view ->
                            val popup = PopupMenu(requireContext(), view)
                            popup.menuInflater.inflate(R.menu.problem_popup_menu, popup.menu)

                            val link = "https://codeforces.com/problemset/problem/${problem.contestId}/${problem.index}"

                            popup.setOnMenuItemClickListener { item ->
                                when (item.itemId) {
                                    R.id.menu_copy -> {
                                        copyToClipboard(link)
                                        Toast.makeText(requireContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        true
                                    }
                                    R.id.menu_open -> {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                        intent.data = android.net.Uri.parse(link)
                                        startActivity(intent)
                                        true
                                    }
                                    else -> false
                                }
                            }

                            popup.show()
                        }


                        placeholder.addView(taskCard)
                    } else {
                        Toast.makeText(requireContext(), "Could not find task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return view
    }

    private fun updateTagsTitle(tagsTitle: TextView) {
        tagsTitle.text = if (selectedTags.isEmpty()) {
            "TAGS"
        } else {
            "TAGS: ${selectedTags.joinToString(", ")}"
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Task Link", text)
        clipboard.setPrimaryClip(clip)
    }
}
