package com.rifqidev.x_posetracker.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.adapter.ListWorkoutAdapter
import com.rifqidev.x_posetracker.data.WorkoutItem
import com.rifqidev.x_posetracker.databinding.FragmentHomeBinding
import com.rifqidev.x_posetracker.utils.DateHelper
import com.rifqidev.x_posetracker.utils.toBitmap
import java.time.LocalDate

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var categoryOptions: Array<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>

    private lateinit var timeOptions: Array<String>
    private lateinit var timeAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory.getInstance(requireActivity().application)
        homeViewModel = ViewModelProvider(requireActivity(), factory)[HomeViewModel::class.java]

        homeViewModel.getUserProfile().observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.fullname.text = user.userName
                val profileImage = user.userProfile?.toBitmap()
                if (user.userProfile != null) {
                    binding.profileImage.setImageBitmap(profileImage)
                } else {
                    binding.profileImage.setImageResource(R.drawable.account_icon)
                }
            }
        }

        val todayDate = DateHelper.getCurrentDateOnly()

        homeViewModel.getTodayDuration(todayDate).observe(viewLifecycleOwner) { duration ->
            if (duration != null) {
                binding.durationValue.text = DateHelper.formatTime(duration.toLong())
            } else {
                binding.durationValue.text = DateHelper.formatTime(0L)
            }
        }

        homeViewModel.getTodayCalories(todayDate).observe(viewLifecycleOwner) { calories ->
            if (calories != null) {
                binding.calorieValue.text = String.format("%.2f", calories)
            } else {
                binding.calorieValue.text = String.format("%.2f", 0.0)
            }
        }

        categoryOptions = resources.getStringArray(R.array.categories_menu)

        categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            categoryOptions
        )

        binding.categoryOption.setAdapter(categoryAdapter)

        binding.categoryOption.setText(categoryOptions[0], false)

        binding.categoryOption.setOnItemClickListener { _, _, position, _ ->
            val categorySelected = categoryOptions[position]
        }

        binding.categoryOption.setOnClickListener {
            categoryAdapter.filter.filter(null)
            binding.categoryOption.showDropDown()
        }

        binding.categoryOptionLayout.setEndIconOnClickListener {
            categoryAdapter.filter.filter(null)
            binding.categoryOption.showDropDown()
        }

        val chart = binding.chart

        val entries = listOf(
            Entry(0f, 40f),
            Entry(1f, 30f),
            Entry(2f, 35f),
            Entry(3f, 50f),
            Entry(4f, 15f),
            Entry(5f, 25f),
            Entry(6f, 40f),
        )

        val dataSet = LineDataSet(entries, "Progress").apply {
            color = Color.rgb(187, 242, 70)
            setCircleColor(Color.rgb(187, 242, 70))
            circleRadius = 5f
            circleHoleRadius = 2.5f
            lineWidth = 3f
            mode = LineDataSet.Mode.LINEAR
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradien)
        }

        chart.data = LineData(dataSet)

        val days = arrayOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(days)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textColor = Color.WHITE
        }

        chart.axisLeft.apply {
            textColor = Color.WHITE
        }

        dataSet.valueTextColor = Color.WHITE
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.invalidate()

        val names = resources.getStringArray(R.array.categories_menu)

        val iconResIds = listOf(
            R.drawable.push_up_icon,
            R.drawable.sit_up_icon,
            R.drawable.pull_up_icon,
            R.drawable.lunges_icon
        )

        val workoutItems = mutableListOf(
            WorkoutItem(names[0], iconResIds[0], date = null, repetition = "0"),
            WorkoutItem(names[1], iconResIds[1], date = null, repetition = "0"),
            WorkoutItem(names[2], iconResIds[2], date = null, repetition = "0"),
            WorkoutItem(names[3], iconResIds[3], date = null, repetition = "0")
        )

        val adapter = ListWorkoutAdapter(workoutItems)
        binding.rvWorkout.adapter = adapter

        timeOptions = resources.getStringArray(R.array.days_menu)

        timeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            timeOptions
        )

        binding.daysOption.setAdapter(timeAdapter)

        binding.daysOption.setText(timeOptions[0], false)

        binding.daysOption.setOnItemClickListener { _, _, position, _ ->
            val timeSelected = timeOptions[position]

            if(timeSelected == "Hari ini") {
                homeViewModel.setTodayRange()
            } else if(timeSelected == "Minggu ini") {
                homeViewModel.setThisWeekRange()
            } else if(timeSelected == "Bulan ini") {
                homeViewModel.setThisMonthRange()
            } else if(timeSelected == "Tahun ini") {
                homeViewModel.setThisYearRange()
            } else if(timeSelected == "Semua") {
                homeViewModel.setAllRange()
            }
        }

        binding.daysOption.setOnClickListener {
            timeAdapter.filter.filter(null)
            binding.daysOption.showDropDown()
        }

        binding.daysOptionLayout.setEndIconOnClickListener {
            timeAdapter.filter.filter(null)
            binding.daysOption.showDropDown()
        }

        homeViewModel.setTodayRange()

        homeViewModel.bestAchievements.observe(viewLifecycleOwner) { achievements ->
            workoutItems.forEachIndexed { index, item ->
                workoutItems[index] = item.copy(date = null, repetition = "0")
            }

            achievements.forEach { achievement ->
                val index = when (achievement.category) {
                    "Push-Up" -> 0
                    "Sit-Up"  -> 1
                    "Pull-Up" -> 2
                    "Lunges"  -> 3
                    else -> null
                }

                index?.let { i ->
                    if(achievement.bestCount == 0) return@forEach
                    workoutItems[i] = workoutItems[i].copy(
                        date = achievement.bestDate,
                        repetition = achievement.bestCount.toString()
                    )
                }
            }

            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}