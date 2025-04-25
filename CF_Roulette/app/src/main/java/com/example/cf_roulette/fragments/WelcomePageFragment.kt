package com.example.cf_roulette.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.cf_roulette.R




class WelcomePageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.welcome_page_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val copyButton = view.findViewById<Button>(R.id.copy_link_button)
        copyButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(android.content.ClipboardManager::class.java)
            val clip = android.content.ClipData.newPlainText("Website", "https://mikhailkapitov.github.io/web-project-MAN")
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
        val gifView = view.findViewById<ImageView>(R.id.welcome_gif)
        Glide.with(this)
            .asGif()
            .load("https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMHJqcG1sNWhhOXRoYnN3MXloemo0YnFhcWU1YnRkcWZrdXNscW43MyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/3oKIPnAiaMCws8nOsE/giphy.gif")
            .into(gifView)

    }
}