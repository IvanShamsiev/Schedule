package com.example.schedule.model;

import android.support.annotation.NonNull;

import com.example.schedule.logic.ScheduleHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Schedule {

    private HashMap<Integer, List<List<String>>> unevenWeek, evenWeek;

    private Schedule() {}

    public List<Lesson> getLessons(Calendar date) {
        List<Lesson> lessons = new ArrayList<>();

        List<List<String>> stringLessons = ScheduleHelper.isEven(date) ?
            evenWeek.get(date.get(Calendar.DAY_OF_WEEK)):
            unevenWeek.get(date.get(Calendar.DAY_OF_WEEK));

        if (stringLessons != null && stringLessons.size() != 0)
        for (List<String> lessonInfo: stringLessons) lessons.add(new Lesson(lessonInfo));

        return lessons;
    }


    @Override
    @NonNull
    public String toString() {
        return "Schedule {" +
                ", evenWeek=" + evenWeek +
                ", unevenWeek=" + unevenWeek +
                " }";
    }
}
