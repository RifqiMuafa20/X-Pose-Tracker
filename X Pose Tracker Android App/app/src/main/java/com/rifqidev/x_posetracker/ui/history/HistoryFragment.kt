package com.rifqidev.x_posetracker.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.adapter.EventAdapter
import com.rifqidev.x_posetracker.adapter.HistoryAdapter
import com.rifqidev.x_posetracker.adapter.MemberAdapter
import com.rifqidev.x_posetracker.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.noData.visibility = View.VISIBLE

        val factory = ViewModelFactory.getInstance(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), factory)[HistoryViewModel::class.java]

        adapter = HistoryAdapter()
        binding.rvActivity.adapter = adapter
        binding.rvActivity.layoutManager = LinearLayoutManager(this.requireActivity())

        viewModel.getUserProfile().observe(viewLifecycleOwner) { user ->
            if (user != null) {
                viewModel.getAllRecord(user.idUser).observe(viewLifecycleOwner) { activityList ->
                    if (activityList.isNullOrEmpty()) {
                        binding.noData.visibility = View.VISIBLE
                        adapter.setData(emptyList())
                    } else {
                        binding.noData.visibility = View.GONE
                        adapter.setData(activityList)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}