package com.example.schedule.model

import java.io.Serializable
import java.util.*

data class Schedule(val courses: List<Course>)

data class Course(val name: String, val groups: List<Group>)

data class Group(val name: String, val week: Week): Serializable

data class Week(val days: List<Day>): Serializable {
    fun getDay(date: Calendar): Day {
        val num = date.get(Calendar.DAY_OF_WEEK) - 1
        return days.find {it.number == num}!!
    }
}

data class Day(val number: Int,
               val lessons: List<Lesson>): Serializable {

    val evenLessons: List<Lesson>
        get() = lessons.filter { lesson -> lesson.even == "Нижняя" }
    val unevenLessons: List<Lesson>
        get() = lessons.filter { lesson -> lesson.even == "Верхняя" }
}

data class Lesson(val beginTime: String,
                  val endTime: String,
                  val even: String,
                  val name: String,
                  val location: String,
                  val type: String,
                  val chair: String,
                  val post: String,
                  val teacher: String): Serializable {

    val fields: List<LessonField>
        get() = listOf(LessonField("Время начала", beginTime),
                LessonField("Время окончания", endTime),
                LessonField("Неделя", even),
                LessonField("Предмет", name),
                LessonField("Аудитория", location),
                LessonField("Тип пары", type),
                LessonField("Кафедра", chair),
                LessonField("Должность преподавателя", post),
                LessonField("ФИО преподавателя", teacher))

    companion object Properties {
        const val fieldsCount: Int = 9
    }
}

data class LessonField(val name: String,
                       val value: String): Serializable