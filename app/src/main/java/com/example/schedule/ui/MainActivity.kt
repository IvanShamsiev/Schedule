package com.example.schedule.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.schedule.R
import com.example.schedule.ScheduleApplication
import com.example.schedule.ScheduleApplication.CURRENT_DATE_EXTRA
import com.example.schedule.ScheduleApplication.START_ACTIVITY_REQUEST_CODE
import com.example.schedule.logic.ScheduleHelper
import com.example.schedule.util.DayAdapter
import com.example.schedule.util.daysBetween
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dataChoiceDialog: DatePickerDialog

    private lateinit var currentDate: Calendar
    private lateinit var pageDate: Calendar

    private lateinit var preferences: SharedPreferences
    private var weekEvenStyle: Boolean = true // true: В-Н; false: Ч-Н
    private var showNavigationLayout: Boolean = false // false: hide, true: show = false

    private lateinit var adapter: DayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ScheduleApplication.currentTheme)
        setContentView(R.layout.activity_main)
        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        currentDate = GregorianCalendar()
        pageDate = GregorianCalendar()

        loadSchedule()
    }

    private fun loadSchedule() {
        val groupLoaded = ScheduleHelper.loadGroup()
        if (groupLoaded) setUI()
        else toStartActivity()
    }

    private fun toStartActivity() {
        startActivityForResult(StartActivity.newIntent(this), START_ACTIVITY_REQUEST_CODE)
    }

    private fun setUI() {
        adapter = DayAdapter(currentDate, weekEvenStyle)
        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageDate = currentDate.clone() as Calendar
                pageDate.add(Calendar.DATE, position - DayAdapter.middlePos)
                setCurrentTitle()
            }
        })
        viewPager.setCurrentItem(DayAdapter.middlePos, false)

        // Set dialog for peek date for schedule
        val dateSetListener = OnDateSetListener { _, year, month, dayOfMonth ->
            val newPageDate = currentDate.clone() as Calendar
            newPageDate[year, month] = dayOfMonth
            val daysBetween = daysBetween(newPageDate.time, currentDate.time)
            viewPager.currentItem = DayAdapter.middlePos - daysBetween
            pageDate[year, month] = dayOfMonth
        }
        val year = currentDate[Calendar.YEAR]
        val month = currentDate[Calendar.MONTH]
        val day = currentDate[Calendar.DAY_OF_MONTH]
        dataChoiceDialog = DatePickerDialog(this, dateSetListener, year, month, day)
        dataChoiceDialog.setTitle("Выберите дату")

        // Set navigation layout
        btnLeft.setOnClickListener { viewPager.currentItem-- }
        btnRight.setOnClickListener { viewPager.currentItem++ }
    }

    override fun onResume() {
        super.onResume()
        weekEvenStyle = preferences.getString("week_even_style", "0") == "0"
        showNavigationLayout = preferences.getBoolean("show_navigation_layout", false)
        navigationLayout.visibility = if (showNavigationLayout) View.VISIBLE else View.GONE
        setCurrentTitle()
        if (viewPager != null && viewPager.adapter != null)
            (viewPager.adapter as DayAdapter).updateEvenStyle(weekEvenStyle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == START_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) loadSchedule()
            else finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.typeWeeks).intent = Intent(this, FullScheduleActivity::class.java).putExtra(CURRENT_DATE_EXTRA, currentDate)
        menu.findItem(R.id.preferences).intent = Intent(this, PreferencesActivity::class.java)
        menu.findItem(R.id.calendar).setOnMenuItemClickListener {
            dataChoiceDialog.show()
            true
        }
        menu.findItem(R.id.reloadSchedule).setOnMenuItemClickListener {
            startActivityForResult(StartActivity.newIntent(this), START_ACTIVITY_REQUEST_CODE)
            true
        }
        return true
    }

    private fun setCurrentTitle() {
        var day = pageDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: "Wrong day of week"
        if (day.isNotEmpty()) day = day.substring(0, 1).toUpperCase(Locale.getDefault()) + day.substring(1)
        if (showNavigationLayout) {
            setTitle(R.string.app_name)
            twTitle!!.text = day
        } else title = day
    }
}