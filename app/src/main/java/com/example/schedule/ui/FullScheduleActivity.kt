package com.example.schedule.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schedule.R
import com.example.schedule.ScheduleApplication
import com.example.schedule.ScheduleApplication.CURRENT_DATE_EXTRA
import com.example.schedule.logic.ScheduleHelper
import com.example.schedule.util.adapter.WeekDaysAdapter
import kotlinx.android.synthetic.main.activity_full_schedule.*
import java.util.*

class FullScheduleActivity : AppCompatActivity() {

    private lateinit var evenWeekAdapter: WeekDaysAdapter
    private lateinit var unevenWeekAdapter: WeekDaysAdapter
    private var isEven = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var theme = R.style.AppTheme_NoActionBar
        when (ScheduleApplication.currentTheme) {
            R.style.AppTheme_Dark -> theme = R.style.AppTheme_Dark_NoActionBar
            R.style.AppTheme_Light -> theme = R.style.AppTheme_Light_NoActionBar
        }
        setTheme(theme)
        setContentView(R.layout.activity_full_schedule)
        title = "Расписание по неделям"

        // Set toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setBackgroundColor(resources.getColor(ScheduleApplication.COLOR_PRIMARY))
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val currentDate = intent.extras!!.get(CURRENT_DATE_EXTRA) as Calendar
        isEven = ScheduleHelper.isEven(currentDate)
        recyclerView.layoutManager = GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false)
        evenWeekAdapter = WeekDaysAdapter(currentDate, true)
        unevenWeekAdapter = WeekDaysAdapter(currentDate, false)
        recyclerView.adapter = if (isEven) evenWeekAdapter else unevenWeekAdapter

        switchBtn.setOnClickListener { if (isEven) setUnevenWeek() else setEvenWeek() }

        setUnevenWeek()
    }

    private fun setUnevenWeek() {
        isEven = false
        twWeekType!!.text = "Верхняя неделя"
        recyclerView.adapter = unevenWeekAdapter
    }

    private fun setEvenWeek() {
        isEven = true
        twWeekType!!.text = "Нижняя неделя"
        recyclerView.adapter = evenWeekAdapter
    }
}