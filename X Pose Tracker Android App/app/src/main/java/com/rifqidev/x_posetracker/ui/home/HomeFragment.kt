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
import com.github.mikephil.charting.formatter.ValueFormatter
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.adapter.ListWorkoutAdapter
import com.rifqidev.x_posetracker.data.WeeklyProgress
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

        val todayDate = DateHelper.getCurrentDate()

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

        binding.categoryOption.setOnClickListener {
            categoryAdapter.filter.filter(null)
            binding.categoryOption.showDropDown()
        }

        binding.categoryOptionLayout.setEndIconOnClickListener {
            categoryAdapter.filter.filter(null)
            binding.categoryOption.showDropDown()
        }

        val chart = binding.chart

        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setScaleXEnabled(false)
        chart.setScaleYEnabled(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)
        chart.isHighlightPerDragEnabled = false
        chart.isHighlightPerTapEnabled = false

        homeViewModel.weeklyProgress.observe(viewLifecycleOwner) { dbList ->
            val (startDate, endDate) = DateHelper.getLast7DaysRange()
            val allDates = DateHelper.getDateRange(startDate, endDate)
            val dataMap = dbList.associateBy { it.date }

            val completeList = allDates.map { date ->
                dataMap[date] ?: WeeklyProgress(date = date, totalValue = 0)
            }

            val entries = completeList.mapIndexed { index, item ->
                Entry(index.toFloat(), item.totalValue.toFloat())
            }

            val dataSet = LineDataSet(entries, "Progress").apply {
                color = Color.rgb(187, 242, 70)
                setCircleColor(Color.rgb(187, 242, 70))
                circleRadius = 5f
                circleHoleRadius = 2.5f
                lineWidth = 3f
                mode = LineDataSet.Mode.LINEAR
                setDrawFilled(true)
                fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradien)
                valueTextColor = Color.WHITE

                valueFormatter = object : ValueFormatter() {
                    override fun getPointLabel(entry: Entry?): String {
                        return entry?.y?.toInt().toString()
                    }
                }
            }

            chart.data = LineData(dataSet)

            val labels = completeList.map { DateHelper.getMonthDay(it.date) }.toTypedArray()

            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                axisMinimum = 0f
                axisMaximum = 6f
                granularity = 1f
                textColor = Color.WHITE
            }

            chart.axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }

            chart.axisLeft.apply {
                textColor = Color.WHITE
                axisMinimum = 0f
                spaceTop = 2f
            }

            chart.axisRight.isEnabled = false
            chart.description.isEnabled = false
            chart.legend.isEnabled = false

            chart.invalidate()
        }

        binding.categoryOption.setOnItemClickListener { _, _, position, _ ->
            val selected = categoryOptions[position]
            homeViewModel.setProgressCategory(selected)
        }

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
            when (timeOptions[position]) {
                "Hari ini"   -> homeViewModel.setTodayRange()
                "Minggu ini"  -> homeViewModel.setThisWeekRange()
                "Bulan ini"  -> homeViewModel.setThisMonthRange()
                "Tahun ini"  -> homeViewModel.setThisYearRange()
                "Semua"      -> homeViewModel.setAllRange()
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