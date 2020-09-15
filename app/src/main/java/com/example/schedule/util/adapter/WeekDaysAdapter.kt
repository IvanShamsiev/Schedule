package com.example.schedule.util.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.ScheduleApplication
import com.example.schedule.logic.ScheduleHelper.group
import com.example.schedule.logic.ScheduleHelper.isEven
import com.example.schedule.model.Lesson
import com.example.schedule.util.adapter.WeekDaysAdapter.WeekDayHolder
import kotlinx.android.synthetic.main.week_day_item.view.*
import java.util.*

class WeekDaysAdapter(private val currentDate: Calendar, private val isEven: Boolean):
        RecyclerView.Adapter<WeekDayHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekDayHolder {
        val view = LayoutInflater.from(
                parent.context).inflate(R.layout.week_day_item, parent, false)
        return WeekDayHolder(view)
    }

    override fun onBindViewHolder(holder: WeekDayHolder, position: Int) {
        val day = group!!.week.days[position + 1]
        val lessons = if (isEven) day.evenLessons else day.unevenLessons
        val weekDay = WeekDay(ScheduleApplication.daysOfWeek[position + 1], lessons,
                isEven(currentDate) == isEven &&
                        currentDate.get(Calendar.DAY_OF_WEEK) - 1 == position + 1)
        holder.onBind(weekDay)
    }

    override fun getItemCount(): Int {
        return 6
    }

    inner class WeekDayHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(weekDay: WeekDay) {
            with (itemView) {
                twDayOfWeek.text = weekDay.dayOfWeek
                if (weekDay.isToday) twDayOfWeek.setTextColor(itemView.resources.getColor(ScheduleApplication.COLOR_ACCENT))
                else twDayOfWeek.setTextColor(itemView.resources.getColor(ScheduleApplication.COLOR_SECONDARY))
                if (weekDay.lessons.isEmpty()) {
                    twDayLessons.text = "Нет пар"
                    twDayLessons.gravity = Gravity.CENTER
                } else {
                    twDayLessons.text = weekDay.lessons.map { "${it.beginTime} - ${it.name}" }.reduce {s1, s2 -> "$s1\n$s2"}
                    twDayLessons.gravity = Gravity.START
                }
            }
        }
    }

    data class WeekDay(val dayOfWeek: String,
                       val lessons: List<Lesson>,
                       val isToday: Boolean)

}