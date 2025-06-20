package com.rifqidev.x_posetracker.ui.record

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rifqidev.x_posetracker.databinding.FragmentRecordBinding
import com.rifqidev.x_posetracker.ui.record_private.PrivateRecordActivity
import com.rifqidev.x_posetracker.ui.record_supervisor.SupervisorRecordActivity

class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.privateRecord.setOnClickListener {
            val intent = Intent(
                requireContext(),
                PrivateRecordActivity::class.java
            )
            startActivity(intent)
        }

        binding.supervisorRecord.setOnClickListener {
            val intent = Intent(
                requireContext(),
                SupervisorRecordActivity::class.java
            )
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}