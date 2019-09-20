package com.example.schedule.model;

import android.support.annotation.NonNull;

import com.example.schedule.logic.ScheduleHelper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Schedule {

    private HashMap<Integer, List<Lesson>> unevenWeek, evenWeek;

    private Schedule() {}

    public List<Lesson> getLessons(Calendar date) {
        return ScheduleHelper.isEven(date) ?
            evenWeek.get(date.get(Calendar.DAY_OF_WEEK)):
            unevenWeek.get(date.get(Calendar.DAY_OF_WEEK));
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
