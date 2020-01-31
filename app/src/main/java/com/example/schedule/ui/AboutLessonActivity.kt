package com.example.schedule.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schedule.R
import com.example.schedule.ScheduleApplication
import com.example.schedule.model.Lesson
import com.example.schedule.util.MySimpleAdapter
import kotlinx.android.synthetic.main.activity_about_lesson.*
import kotlinx.android.synthetic.main.item_about_lesson.view.*

class AboutLessonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var theme = R.style.AppTheme_NoActionBar
        when (ScheduleApplication.currentTheme) {
            R.style.AppTheme_Dark -> theme = R.style.AppTheme_Dark_NoActionBar
            R.style.AppTheme_Light -> theme = R.style.AppTheme_Light_NoActionBar
        }
        setTheme(theme)
        setContentView(R.layout.activity_about_lesson)
        title = "О предмете"

        // Set toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setBackgroundColor(resources.getColor(ScheduleApplication.COLOR_PRIMARY))
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val lesson = intent.extras!!.get(ScheduleApplication.LESSON_EXTRA) as Lesson
        rvLessonInfo.layoutManager = LinearLayoutManager(this)
        rvLessonInfo.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvLessonInfo.adapter = MySimpleAdapter(R.layout.item_about_lesson, lesson.fields, { itemView, lessonField ->
            with(itemView) {
                twName.text = lessonField.name
                twValue.text = lessonField.value
            }
        }, {
            with (it) {
                twName.setTextColor(context.resources.getColor(ScheduleApplication.COLOR_SECONDARY))
                twValue.setTextColor(context.resources.getColor(ScheduleApplication.COLOR_ACCENT))
            }
        })
    }
}