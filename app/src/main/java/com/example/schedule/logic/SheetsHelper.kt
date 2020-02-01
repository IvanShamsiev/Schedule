package com.example.schedule.logic

import com.example.schedule.ScheduleApplication
import com.example.schedule.model.*
import org.apache.poi.ss.usermodel.*
import java.io.InputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object SheetsHelper {

    private const val lessonMinutesCount = 90
    private const val leftRows = 2
    private const val groupRow = 0
    private const val groupColumn = 3
    private const val dayRow = 0
    private const val dayColumn = 0
    private const val timeColumn = 1
    private const val evenColumn = 2
    private const val nameColumn = 3
    private const val locationOneColumn = 4
    private const val locationTwoColumn = 5
    private const val typeColumn = 6
    private const val chairColumn = 7
    private const val postColumn = 8
    private const val teacherColumn = 9
    private lateinit var locale: Locale
    private lateinit var lessonTimeFormat: SimpleDateFormat

    fun getSchedule(inputStream: InputStream?): Schedule? {
        if (inputStream == null) return null
        locale = Locale.getDefault()
        lessonTimeFormat = SimpleDateFormat("HH:mm", locale)
        val workbook: Workbook
        try { workbook = readWorkbook(inputStream) ?: return null }
        catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        val courses: MutableList<Course> = ArrayList()
        for (courseSheet in workbook) courses.add(Course(courseSheet.sheetName, getGroups(courseSheet)))
        return Schedule(courses)
    }

    private fun getGroups(courseSheet: Sheet): List<Group> {
        var column = 0
        val groups: MutableList<Group> = ArrayList()
        while (true) {
            if (courseSheet.getRow(leftRows).getCell(column).toString() != "Понедельник") column++
            else break
        }
        while (courseSheet.getRow(0).getCell(column) != null) {
            val days: ArrayList<MutableDay> = ArrayList(7)
            for (i in 0..6) days.add(MutableDay(i, ArrayList()))

            val groupCell = courseSheet.getRow(groupRow).getCell(column + groupColumn)
            if (groupCell == null || groupCell.toString().trim { it <= ' ' }.isEmpty()) break
            val groupName = if (groupCell.cellType == CellType.NUMERIC) groupCell.numericCellValue.toInt().toString() else groupCell.toString()

            var row = leftRows
            var dayOfWeek: String? = null
            while (true) {
                if (courseSheet.getRow(row) == null || courseSheet.getRow(row).getCell(column) == null) break
                val currentDayOfWeek = courseSheet.getRow(row + dayRow)
                        .getCell(column + dayColumn).toString()
                if (currentDayOfWeek.trim { it <= ' ' }.isNotEmpty()) dayOfWeek = currentDayOfWeek
                val lesson = getLesson(courseSheet, row, column)
                if (lesson != null) {
                    val dayOfWeekIndex = ScheduleApplication.dayOfWeek.map { it.toLowerCase(locale) }.indexOf(dayOfWeek?.toLowerCase(locale))
                    days[dayOfWeekIndex].lessons.add(lesson)
                }
                row++
            }
            if (days.any { it.lessons.isNotEmpty() })
                groups.add(Group(groupName, Week(days.map { Day(it.number, it.lessons) })))
            column++
            while (true) {
                val cell = courseSheet.getRow(leftRows).getCell(column)
                if (cell != null && cell.toString() != "Понедельник") column++ else break
            }
        }
        return groups
    }

    private fun getLesson(sheet: Sheet, row: Int, column: Int): Lesson? {
        val lessonRow = sheet.getRow(row)
        if (lessonRow.getCell(column + nameColumn) == null ||
                lessonRow.getCell(column + nameColumn).toString().trim { it <= ' ' }.isEmpty()) return null
        val beginTime: String
        val endTime: String
        if (lessonRow.getCell(column + timeColumn).cellType == CellType.NUMERIC) {
            val beginDate = lessonRow.getCell(column + timeColumn).dateCellValue
            val endDate = Date(beginDate.time + 1000 * 60 * lessonMinutesCount)
            beginTime = lessonTimeFormat.format(beginDate)
            endTime = lessonTimeFormat.format(endDate)
        } else {
            var timeCell = lessonRow.getCell(column + timeColumn).toString()
            val symbols = charArrayOf(';', '.', ',')
            for (c in symbols) if (timeCell.contains(c.toString())) {
                timeCell = timeCell.replace(c, ':')
                break
            }
            val time = timeCell.split(":").toTypedArray()
            val date: Calendar = GregorianCalendar()
            date[Calendar.AM_PM] = Calendar.AM
            date[Calendar.HOUR] = time[0].toInt()
            date[Calendar.MINUTE] = time[1].toInt()
            beginTime = lessonTimeFormat.format(date.time)
            date.add(Calendar.MINUTE, lessonMinutesCount)
            endTime = lessonTimeFormat.format(date.time)
        }
        var even = lessonRow.getCell(column + evenColumn).toString()
        when (even) {
            "в" -> even = "Верхняя"
            "н" -> even = "Нижняя"
        }
        val name = lessonRow.getCell(column + nameColumn).toString()
        val locationOne = lessonRow.getCell(column + locationOneColumn).toString()
        val locationTwo: String
        val locationTwoCell = lessonRow.getCell(column + locationTwoColumn)
        locationTwo = if (locationTwoCell.cellType == CellType.NUMERIC)
            locationTwoCell.numericCellValue.toInt().toString() else locationTwoCell.toString()
        val location = locationOne + if (locationTwo.trim { it <= ' ' }.isEmpty()) "" else " $locationTwo"
        var type = lessonRow.getCell(column + typeColumn).toString()
        when (type) {
            "лек" -> type = "Лекция"
            "пр" -> type = "Практика"
            "лаб" -> type = "Лаба"
            else -> if (type.length >= 2)
                type = type.substring(0, 1).toUpperCase(Locale.getDefault()) + type.substring(1)
        }
        val chair = lessonRow.getCell(column + chairColumn).toString()
        val post = lessonRow.getCell(column + postColumn).toString()
        val teacher = lessonRow.getCell(column + teacherColumn).toString()
        return Lesson(beginTime, endTime, even, name, location, type, chair, post, teacher)
    }

    private fun readWorkbook(inputStream: InputStream): Workbook? {
        return WorkbookFactory.create(inputStream)
    }

    private data class MutableDay(val number: Int, var lessons: MutableList<Lesson>)
}