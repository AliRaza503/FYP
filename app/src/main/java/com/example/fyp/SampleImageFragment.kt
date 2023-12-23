package com.example.fyp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fyp.databinding.FragmentSampleImageBinding

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SampleImageFragment : Fragment() {
    private var _binding: FragmentSampleImageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSampleImageBinding.inflate(inflater, container, false)
        // Get the argument passed from the previous fragment
        val drawableIndex = arguments?.getInt("image_index")
        // Set the drawable resource to the ImageView
        binding.imageView.setImageResource(R.drawable.ic_1_front)
        return binding.root

    }


    /*

     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}