package com.example.myot.ticket.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import com.example.myot.R
import com.example.myot.databinding.FragmentMusicalSearchBinding
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.ticket.model.TicketViewModel

class MusicalSearchFragment : Fragment() {

    private var _binding: FragmentMusicalSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TicketViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMusicalSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivSearchMusical.setOnClickListener {
            val query = binding.etSearchMusical.text.toString()
            if (query.isNotBlank()) {
                viewModel.searchMusical(query)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.RecordFragmentContainerView, RecordFragment())
                    .addToBackStack(null)
                    .commit()

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}