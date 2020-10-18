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
    private lateinit var locale: Locale
    private lateinit var lessonTimeFormat: SimpleDateFormat

    fun getSchedule(inputStream: InputStream?): Schedule? {
        if (inputStream == null) return null
        val first = System.nanoTime()
        locale = Locale.getDefault()
        lessonTimeFormat = SimpleDateFormat("HH:mm", locale)
        val workbook: Workbook
        try { workbook = readWorkbook(inputStream) ?: return null }
        catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        val courses: MutableList<Course> = ArrayList()
        for (courseSheet in workbook)
            courses.add(Course(courseSheet.sheetName, getGroups(courseSheet)))
        val second = System.nanoTime()
        println("time: ${(second-first)*1e-9}")
        return Schedule(courses)
    }

    private fun getGroups(courseSheet: Sheet): List<Group> {
        var column = 0
        val groups: MutableList<Group> = ArrayList()
        while (true) {
            val monday = courseSheet.getRow(leftRows)?.getCell(column)?.toString()
                    ?: return emptyList()
            if (monday == "Понедельник") break else column++
        }
        val sheetGroup = SheetGroup()
        sheetGroup.setColumns(courseSheet, leftRows-1, column)
        while (courseSheet.getRow(0)?.getCell(column) != null) {
            val group = getGroup(sheetGroup, courseSheet, column) ?: break
            if (group.week.days.any { it.lessons.isNotEmpty() }) groups.add(group)
            column++
            while (true) {
                val cell = courseSheet.getRow(leftRows).getCell(column)
                if (cell != null && cell.toString() != "Понедельник") column++ else break
            }
        }
        return groups
    }

    private fun getGroup(sheetGroup: SheetGroup, courseSheet: Sheet, column: Int): Group? {

        val groupCell = courseSheet.getRow(groupRow)?.getCell(column + groupColumn)
        if (groupCell == null || groupCell.toString().trim { it <= ' ' }.isBlank()) return null
        val groupName = if (groupCell.cellType == CellType.NUMERIC)
            groupCell.numericCellValue.toInt().toString() else groupCell.toString()

        val days: ArrayList<Day> = ArrayList(7)

        var row = leftRows
        while (courseSheet.getRow(row)?.getCell(column) != null) {
            val dayOfWeek = courseSheet.getRow(row + dayRow)
                    ?.getCell(column + dayColumn)
                    ?.toString()
                    ?.trim { it <= ' ' }
            val dayNum = ScheduleApplication.daysOfWeek.indexOf(dayOfWeek)
            if (dayNum != -1) {
                val day = Day(dayNum, getDayLessons(sheetGroup, courseSheet, row, column))
                days.add(day)
            }
            row++
        }

        for (weekDayNum in 0..6)
            if (days.find { it.number == weekDayNum } == null)
                days.add(Day(weekDayNum, emptyList()))
        days.removeAll { it.number !in 0..6 } // Ну а вдруг!?

        return Group(groupName, Week(days.sortedBy { it.number }))
    }

    private fun getDayLessons(sheetGroup: SheetGroup, courseSheet: Sheet, rowNum: Int, column: Int): List<Lesson> {
        val lessons = ArrayList<Lesson>()
        var row = rowNum
        while (courseSheet.getRow(row)?.getCell(column) != null) {
            val lessonRow: Row = courseSheet.getRow(row)
            val lesson = getLesson(sheetGroup, lessonRow, column)
            if (lesson != null) lessons.add(lesson)
            row++
            // Если натыкаемся на следующий день или на конец таблицы, тикаем с городу
            if (courseSheet.getRow(row)?.getCell(column)?.toString()?.isBlank() != true) break
        }
        return lessons
    }

    private fun getLesson(sheetGroup: SheetGroup, row: Row, column: Int): Lesson? {
        if (row.getCell(column + sheetGroup.name)
                        ?.toString()?.trim { it <= ' ' }.isNullOrBlank()) return null
        val (beginTime, endTime) = getTime(row.getCell(column + timeColumn))
        val even = when (val evenStr = row.getCell(column + evenColumn).toString()) {
            "в", "В" -> "Верхняя"
            "н", "Н" -> "Нижняя"
            else -> evenStr
        }

        fun getLessonField(fieldColumn: Int): Cell? {
            if (fieldColumn == FIELD_NONE) return null
            return row.getCell(column + fieldColumn)
        }

        val name = getLessonField(sheetGroup.name)?.toString() ?: return null
        val building = getLessonField(sheetGroup.building)?.toString() ?: ""
        val locationTwoCell = getLessonField(sheetGroup.classroom)
        val classroom = if (locationTwoCell?.cellType == CellType.NUMERIC)
            locationTwoCell.numericCellValue.toInt().toString() else locationTwoCell?.toString() ?: ""
        val location = building + if (classroom.trim { it <= ' ' }.isEmpty()) "" else " $classroom"
        val type = when (val typeStr = getLessonField(sheetGroup.type)?.toString() ?: "") {
            "лек" -> "Лекция"
            "пр" -> "Практика"
            "лаб" -> "Лаба"
            else -> if (typeStr.length < 2) typeStr else (typeStr.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + typeStr.substring(1))
        }
        val chair = getLessonField(sheetGroup.chair)?.toString() ?: ""
        val post = getLessonField(sheetGroup.post)?.toString() ?: ""
        val teacher = getLessonField(sheetGroup.teacher)?.toString() ?: ""
        return Lesson(beginTime, endTime, even, name, location, type, chair, post, teacher)
    }

    private fun getTime(timeCell: Cell): Pair<String, String> {
        val beginTime: String
        val endTime: String
        if (timeCell.cellType == CellType.NUMERIC) {
            val beginDate = timeCell.dateCellValue
            val endDate = Date(beginDate.time + 1000 * 60 * lessonMinutesCount)
            beginTime = lessonTimeFormat.format(beginDate)
            endTime = lessonTimeFormat.format(endDate)
        } else {
            var timeCellStr = timeCell.toString()
            val symbols = charArrayOf(';', '.', ',')
            for (c in symbols) if (timeCellStr.contains(c.toString())) {
                timeCellStr = timeCellStr.replace(c, ':')
                break
            }
            val time = timeCellStr.split(":").toTypedArray()
            val date: Calendar = GregorianCalendar()
            date[Calendar.AM_PM] = Calendar.AM
            date[Calendar.HOUR] = time[0].toInt()
            date[Calendar.MINUTE] = time[1].toInt()
            beginTime = lessonTimeFormat.format(date.time)
            date.add(Calendar.MINUTE, lessonMinutesCount)
            endTime = lessonTimeFormat.format(date.time)
        }
        return Pair(beginTime, endTime)
    }

    private fun readWorkbook(inputStream: InputStream): Workbook? {
        return WorkbookFactory.create(inputStream)
    }

    private class SheetGroup {

        private val fieldsMap: MutableMap<String, Pair<String, Int>> = mutableMapOf(
                "name" to Pair("дис", FIELD_NONE),
                "building" to Pair("здан", FIELD_NONE),
                "classroom" to Pair("ауд", FIELD_NONE),
                "type" to Pair("вид", FIELD_NONE),
                "chair" to Pair("каф", FIELD_NONE),
                "post" to Pair("долж", FIELD_NONE),
                "teacher" to Pair("преп", FIELD_NONE)
        )

        val name: Int get() = fieldsMap["name"]!!.second
        val building: Int get() = fieldsMap["building"]!!.second
        val classroom: Int get() = fieldsMap["classroom"]!!.second
        val type: Int get() = fieldsMap["type"]!!.second
        val chair: Int get() = fieldsMap["chair"]!!.second
        val post: Int get() = fieldsMap["post"]!!.second
        val teacher: Int get() = fieldsMap["teacher"]!!.second

        fun setColumns(courseSheet: Sheet, rowNum: Int, columnNum: Int) {
            val row = courseSheet.getRow(rowNum)
            for (column in 3..9) {
                val fieldStr = row?.getCell(columnNum + column)
                        ?.toString()
                        ?.toLowerCase(locale) ?: continue
                val field = fieldsMap.entries.find {
                    fieldStr.contains(it.value.first)
                } ?: continue
                fieldsMap[field.key] = Pair(field.value.first, column)
            }
        }
    }

    private const val FIELD_NONE = -1
}