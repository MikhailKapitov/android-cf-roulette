package com.example.cf_roulette.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cf_roulette.R
import android.content.SharedPreferences

class RegistrationPageFragment : Fragment() {
    private lateinit var sharedPref: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.registration_page_fragment, container, false)

        val nicknameInput = view.findViewById<EditText>(R.id.nickname_input)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val logoutButton = view.findViewById<Button>(R.id.logout_button)


        nicknameInput.setText(sharedPref.getString("cf_nickname", ""))

        saveButton.setOnClickListener {
            val nickname = nicknameInput.text.toString().trim()
            if (nickname.isNotEmpty()) {
                sharedPref.edit().putString("cf_nickname", nickname).apply()
                Toast.makeText(requireContext(), "Nickname saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a nickname.", Toast.LENGTH_SHORT).show()
            }
        }

        logoutButton.setOnClickListener {
            sharedPref.edit().remove("cf_nickname").apply()
            nicknameInput.setText("")
            Toast.makeText(requireContext(), "Logged out!", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}