package com.focuswechat

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.focuswechat.databinding.ActivityStatisticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStatisticsBinding
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupChart()
        loadStatistics()
        
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }
    
    private fun loadStatistics() {
        lifecycleScope.launch {
            // Get last 7 days data
            val calendar = Calendar.getInstance()
            val endDate = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            val startDate = dateFormat.format(calendar.time)
            
            val records = FocusWeChatApp.database.usageRecordDao()
                .getRecordsBetween(startDate, endDate)
                .first()
            
            // Calculate statistics
            val totalMinutes = records.sumOf { it.totalSeconds } / 60
            val totalBlocks = records.sumOf { it.blockCount }
            val avgMinutes = if (records.isNotEmpty()) totalMinutes / records.size else 0
            
            // Update summary
            binding.tvTotalTime.text = "${totalMinutes} 分钟"
            binding.tvAvgTime.text = "${avgMinutes} 分钟/天"
            binding.tvBlockCount.text = "${totalBlocks} 次"
            
            // Prepare chart data
            val entries = mutableListOf<BarEntry>()
            val labels = mutableListOf<String>()
            
            for (i in 0..6) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -6 + i)
                val dateStr = dateFormat.format(cal.time)
                val displayStr = displayFormat.format(cal.time)
                
                val record = records.find { it.date == dateStr }
                val minutes = (record?.totalSeconds ?: 0) / 60f
                
                entries.add(BarEntry(i.toFloat(), minutes))
                labels.add(displayStr)
            }
            
            // Create dataset
            val dataSet = BarDataSet(entries, "使用时长（分钟）").apply {
                color = Color.parseColor("#4CAF50")
                valueTextColor = Color.BLACK
                valueTextSize = 10f
            }
            
            // Set data
            val barData = BarData(dataSet)
            binding.barChart.data = barData
            
            // Format X axis
            binding.barChart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return labels.getOrNull(value.toInt()) ?: ""
                }
            }
            
            binding.barChart.invalidate()
        }
    }
}