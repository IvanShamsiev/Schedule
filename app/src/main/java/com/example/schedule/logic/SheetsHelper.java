package com.example.schedule.logic;

import com.example.schedule.model.Lesson;
import com.example.schedule.ui.MainActivity;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SheetsHelper {

    private static int lessonMinutesCount = 90;

    private static int lessonsCount = 8;
    private static int rowsForLesson = 2;

    private static int cycleColumnsCount = 10;
    private static int leftRows = 2;
    private static int cycleRowsCount = 16;

    private static int groupRow = 0;
    private static int groupColumn = 3;

    private static int dayRow = 0;
    private static int dayColumn = 0;

    private static int timeColumn = 1;
    private static int evenColumn = 2;
    private static int nameColumn = 3;
    private static int locationOneColumn = 4;
    private static int locationTwoColumn = 5;
    private static int typeColumn = 6;
    private static int chairColumn = 7;
    private static int postColumn = 8;
    private static int teacherColumn = 9;

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static HashMap<String, HashMap<Integer, List<Lesson>>> getGroupsMap(HSSFSheet courseSheet) {
        int column = 0;

        HashMap<String, HashMap<Integer, List<Lesson>>> groupsMap = new HashMap<>();

        while (courseSheet.getRow(0).getCell(column) != null) {
            HashMap<Integer, List<Lesson>> week = new HashMap<>(6);

            String groupName = courseSheet.getRow(groupRow).getCell(column + groupColumn).getStringCellValue();

            for (int row = leftRows; row < 6 * lessonsCount * rowsForLesson + leftRows; row += cycleRowsCount) {
                String dayOfWeek = courseSheet.getRow(row + dayRow).getCell(column + dayColumn).toString();

                List<Lesson> lessons = new ArrayList<>();

                for (int lessonNum = 0; lessonNum < lessonsCount; lessonNum++) {
                    if (courseSheet.getRow(row + lessonNum * 2).getCell(column) == null) break;

                    Lesson lessonUneven = getLesson(courseSheet, row + lessonNum * 2, column);
                    Lesson lessonEven = getLesson(courseSheet, row + lessonNum * 2 + 1, column);

                    if (lessonUneven != null) lessons.add(lessonUneven);
                    if (lessonEven != null) lessons.add(lessonEven);
                }

                week.put(MainActivity.dayOfWeek.indexOf(dayOfWeek), lessons);
            }

            groupsMap.put(groupName, week);

            column += cycleColumnsCount;
        }

        return groupsMap;
    }

    private static Lesson getLesson(HSSFSheet sheet, int row, int column) {

        Row lessonRow = sheet.getRow(row);

        if (lessonRow.getCell(column + nameColumn) == null || lessonRow.getCell(column + nameColumn).toString().isEmpty()) return null;

        String beginTime, endTime;
        if (lessonRow.getCell(column + timeColumn).getCellType() == CellType.NUMERIC) {
            Date beginDate = lessonRow.getCell(column + timeColumn).getDateCellValue();
            Date endDate = new Date(beginDate.getTime() + 1000 * 60 * lessonMinutesCount);

            beginTime = dateFormat.format(beginDate);
            endTime = dateFormat.format(endDate);
        } else {
            String[] time = lessonRow.getCell(column + timeColumn).toString().split(":");
            Calendar date = new GregorianCalendar();
            date.set(Calendar.AM_PM, Calendar.AM);
            date.set(Calendar.HOUR, Integer.parseInt(time[0]));
            date.set(Calendar.MINUTE, Integer.parseInt(time[1]));

            beginTime = dateFormat.format(date.getTime());
            date.add(Calendar.MINUTE, lessonMinutesCount);
            endTime = dateFormat.format(date.getTime());
        }

        String even = lessonRow.getCell(column + evenColumn).toString();

        String name = lessonRow.getCell(column + nameColumn).toString();

        String locationOne = lessonRow.getCell(column + locationOneColumn).toString();
        String locationTwo = "";
        if (lessonRow.getCell(column + locationTwoColumn).getCellType() == CellType.NUMERIC)
            locationTwo = "" + (int) lessonRow.getCell(column + locationTwoColumn).getNumericCellValue();
        else locationTwo = lessonRow.getCell(column + locationTwoColumn).toString();
        String location = locationOne + (locationTwo.isEmpty() ? "" : (" " + locationTwo));

        String type = lessonRow.getCell(column + typeColumn).toString();

        String chair = lessonRow.getCell(column + chairColumn).toString();

        String post = lessonRow.getCell(column + postColumn).toString();

        String teacher = lessonRow.getCell(column + teacherColumn).toString();

        return new Lesson(beginTime, endTime, even, name, location, type, chair, post, teacher);
    }

    private static HSSFWorkbook readWorkbook(String filename) {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            return wb;
        }
        catch (Exception e) {
            System.out.println("Неудача");
            e.printStackTrace();
            return null;
        }
    }
}
