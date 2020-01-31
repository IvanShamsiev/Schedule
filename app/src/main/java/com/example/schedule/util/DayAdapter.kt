package com.example.schedule.util

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.ScheduleApplication.LESSON_EXTRA
import com.example.schedule.logic.ScheduleHelper
import com.example.schedule.ui.AboutLessonActivity
import com.example.schedule.util.DayAdapter.DayViewHolder
import kotlinx.android.synthetic.main.item_fragment_page.view.*
import kotlinx.android.synthetic.main.item_lesson.view.*
import java.util.*

class DayAdapter(private val currentDate: Calendar, private var weekEvenStyle: Boolean):
        RecyclerView.Adapter<DayViewHolder>() {

    private val locale = Locale.getDefault()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val dayView = LayoutInflater.from(parent.context).inflate(R.layout.item_fragment_page, parent, false)
        return DayViewHolder(dayView)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = currentDate.clone() as Calendar
        date.add(Calendar.DATE, position - middlePos)
        holder.bind(date)
    }

    override fun getItemCount(): Int {
        return pagesCount
    }

    fun updateEvenStyle(weekEvenStyle: Boolean) {
        this.weekEvenStyle = weekEvenStyle
        notifyDataSetChanged()
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(date: Calendar) {
            if (ScheduleHelper.group == null) return
            val group = ScheduleHelper.group!!
            with (itemView) {
                var month = date.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) ?: "Wrong month name"
                month = month.substring(0, 1).toUpperCase(locale) + month.substring(1)
                textDate.text = String.format("%d %s", date[Calendar.DAY_OF_MONTH], month)

                val upWeek = if (weekEvenStyle) "Верхняя неделя" else "Нечётная неделя"
                val downWeek = if (weekEvenStyle) "Нижняя неделя" else "Чётная неделя"
                textEvenWeek.text = if (ScheduleHelper.isEven(date)) downWeek else upWeek

                val day = group.week.getDay(date)
                val lessons = if (ScheduleHelper.isEven(date)) day.evenLessons else day.unevenLessons
                if (lessons.isNotEmpty()) {
                    lessonsRecyclerView.visibility = View.VISIBLE
                    textNoLessons.visibility = View.GONE
                    lessonsRecyclerView.adapter = MySimpleAdapter(R.layout.item_lesson, lessons, { itemView, lesson ->
                        with (itemView) {
                            tvBeginTime.text = lesson.beginTime
                            tvEndTime.text = lesson.endTime
                            tvName.text = lesson.name
                            tvTeacher.text = lesson.teacher
                            tvLocation.text = lesson.location
                            tvType.text = lesson.type
                            setOnClickListener {
                                val intent = Intent(context, AboutLessonActivity::class.java)
                                intent.putExtra(LESSON_EXTRA, lesson)
                                context.startActivity(intent)
                            }
                        }
                    })
                } else {
                    lessonsRecyclerView.visibility = View.GONE
                    textNoLessons.visibility = View.VISIBLE
                    lessonsRecyclerView.adapter = null
                }
            }
        }

        init {
            with (itemView) {
                lessonsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                lessonsRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }
    }

    companion object {
        private const val pagesCount = Int.MAX_VALUE
        var middlePos = pagesCount / 2
    }
}