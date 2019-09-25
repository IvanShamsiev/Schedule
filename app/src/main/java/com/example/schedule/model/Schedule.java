package com.example.schedule.model;

import androidx.annotation.NonNull;

import com.example.schedule.logic.ScheduleHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Schedule {

    private HashMap<Integer, List<Lesson>> week;

    private Schedule() { }

    public HashMap<Integer, List<Lesson>> getWeek() {
        return week;
    }

    public List<Lesson> getLessons(Calendar date) {

        if (date.get(Calendar.DAY_OF_WEEK) - 1 == 0) return new ArrayList<>();

        List<Lesson> lessons = week.get(date.get(Calendar.DAY_OF_WEEK) - 1);
        System.out.println(date.get(Calendar.DAY_OF_WEEK));

        List<Lesson> newList = new ArrayList<>();
        for (Lesson l: lessons)
            if (ScheduleHelper.isEven(date) == l.getEven().equals("Нижняя"))
                newList.add(l);

        return newList;
    }


    @NonNull
    @Override
    public String toString() {
        return "Schedule { " +
                "week=" + week +
                " }";
    }
}
