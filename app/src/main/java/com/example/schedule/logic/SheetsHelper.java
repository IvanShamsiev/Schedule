package com.example.schedule.logic;

import com.example.schedule.ScheduleApplication;
import com.example.schedule.model.Lesson;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SheetsHelper {

    private static final int lessonMinutesCount = 90;

    private static final int leftRows = 2;

    private static final int groupRow = 0;
    private static final int groupColumn = 3;

    private static final int dayRow = 0;
    private static final int dayColumn = 0;

    private static final int timeColumn = 1;
    private static final int evenColumn = 2;
    private static final int nameColumn = 3;
    private static final int locationOneColumn = 4;
    private static final int locationTwoColumn = 5;
    private static final int typeColumn = 6;
    private static final int chairColumn = 7;
    private static final int postColumn = 8;
    private static final int teacherColumn = 9;

    private static SimpleDateFormat lessonTimeFormat;

    public static HashMap<String, HashMap<String, HashMap<Integer, List<Lesson>>>> getCoursesMap(InputStream inputStream) {

        if (inputStream == null) return null;

        lessonTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        HashMap<String, HashMap<String, HashMap<Integer, List<Lesson>>>> coursesMap = new HashMap<>();

        Workbook workbook = readWorkbook(inputStream);
        if (workbook == null) return null;

        for (Sheet courseSheet : workbook)
            coursesMap.put(courseSheet.getSheetName(), getGroupsMap(courseSheet));

        return coursesMap;
    }

    private static HashMap<String, HashMap<Integer, List<Lesson>>> getGroupsMap(Sheet courseSheet) {
        int column = 0;

        HashMap<String, HashMap<Integer, List<Lesson>>> groupsMap = new HashMap<>();

        while (true) {
            if (!courseSheet.getRow(leftRows).getCell(column).toString().equals("Понедельник")) column++;
            else break;
        }
        while (courseSheet.getRow(0).getCell(column) != null) {
            HashMap<Integer, List<Lesson>> week = new HashMap<>(6);

            Cell groupCell = courseSheet.getRow(groupRow).getCell(column + groupColumn);

            if (groupCell == null || groupCell.toString().trim().isEmpty()) break;

            String groupName;
            if (groupCell.getCellType() == CellType.NUMERIC) groupName = "" + (int) groupCell.getNumericCellValue();
            else groupName = groupCell.toString();

            int row = leftRows;
            String dayOfWeek = null;
            while (true) {
                if (courseSheet.getRow(row) == null || courseSheet.getRow(row).getCell(column) == null) break;

                String currentDayOfWeek = courseSheet.getRow(row + dayRow)
                        .getCell(column + dayColumn).toString();
                if (!currentDayOfWeek.trim().isEmpty()) {
                    if (dayOfWeek == null) dayOfWeek = currentDayOfWeek;
                    else dayOfWeek = currentDayOfWeek + "";
                }

                Lesson lesson = getLesson(courseSheet, row, column);
                if (lesson != null) {
                    int dayOfWeekIndex = ScheduleApplication.dayOfWeek.indexOf(dayOfWeek);
                    if (week.get(dayOfWeekIndex) == null)
                        week.put(dayOfWeekIndex, new ArrayList<>());
                    week.get(dayOfWeekIndex).add(lesson);
                }

                row++;
            }

            boolean isEmpty = true;

            if (!week.isEmpty()) for (List<Lesson> list: week.values()) if (!list.isEmpty()) isEmpty = false;

            if (!isEmpty) groupsMap.put(groupName, week);

            column++;

            Cell cell;
            while (true) {
                cell = courseSheet.getRow(leftRows).getCell(column);
                if (cell != null && !cell.toString().equals("Понедельник")) column++;
                else break;
            }
        }

        return groupsMap;
    }

    private static Lesson getLesson(Sheet sheet, int row, int column) {

        Row lessonRow = sheet.getRow(row);

        if (lessonRow.getCell(column + nameColumn) == null ||
                lessonRow.getCell(column + nameColumn).toString().trim().isEmpty()) return null;

        String beginTime, endTime;
        if (lessonRow.getCell(column + timeColumn).getCellType() == CellType.NUMERIC) {
            Date beginDate = lessonRow.getCell(column + timeColumn).getDateCellValue();
            Date endDate = new Date(beginDate.getTime() + 1000 * 60 * lessonMinutesCount);

            beginTime = lessonTimeFormat.format(beginDate);
            endTime = lessonTimeFormat.format(endDate);
        } else {
            String timeCell = lessonRow.getCell(column + timeColumn).toString();
            char[] symbols = new char[] {';', '.', ','};
            for (char c: symbols) if (timeCell.contains(String.valueOf(c))) {
                timeCell = timeCell.replace(c, ':');
                break;
            }
            String[] time = timeCell.split(":");
            Calendar date = new GregorianCalendar();
            date.set(Calendar.AM_PM, Calendar.AM);
            date.set(Calendar.HOUR, Integer.parseInt(time[0]));
            date.set(Calendar.MINUTE, Integer.parseInt(time[1]));

            beginTime = lessonTimeFormat.format(date.getTime());
            date.add(Calendar.MINUTE, lessonMinutesCount);
            endTime = lessonTimeFormat.format(date.getTime());
        }

        String even = lessonRow.getCell(column + evenColumn).toString();
        switch (even) {
            case "в": even = "Верхняя"; break;
            case "н": even = "Нижняя"; break;
        }

        String name = lessonRow.getCell(column + nameColumn).toString();

        String locationOne = lessonRow.getCell(column + locationOneColumn).toString();
        String locationTwo;
        if (lessonRow.getCell(column + locationTwoColumn).getCellType() == CellType.NUMERIC)
            locationTwo = "" + (int) lessonRow.getCell(column + locationTwoColumn).getNumericCellValue();
        else locationTwo = lessonRow.getCell(column + locationTwoColumn).toString();
        String location = locationOne + (locationTwo.trim().isEmpty() ? "" : (" " + locationTwo));

        String type = lessonRow.getCell(column + typeColumn).toString();
        switch (type) {
            case "лек": type = "Лекция"; break;
            case "пр": type = "Практика"; break;
            case "лаб": type = "Лаба"; break;
            default: if (type.length() >= 2) type = type.substring(0, 1).toUpperCase() + type.substring(1); break;
        }

        String chair = lessonRow.getCell(column + chairColumn).toString();

        String post = lessonRow.getCell(column + postColumn).toString();

        String teacher = lessonRow.getCell(column + teacherColumn).toString();

        return new Lesson(beginTime, endTime, even, name, location, type, chair, post, teacher);
    }

    private static Workbook readWorkbook(InputStream inputStream) {

        try { return WorkbookFactory.create(inputStream); }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
