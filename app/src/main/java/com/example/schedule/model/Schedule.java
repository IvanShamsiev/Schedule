package com.example.schedule.model;

import androidx.annotation.NonNull;

import com.example.schedule.logic.ScheduleHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Schedule {

    private HashMap<Integer, List<Lesson>> week;

    public Schedule(HashMap<Integer, List<Lesson>> week) {
        this.week = week;
    }

    public List<Lesson> getLessons(Calendar date) {
        /*if (ScheduleHelper.isEven(date)) {
            HashMap<Integer, List<Lesson>> evenWeek = new HashMap<>();
            for (Map.Entry<Integer, List<Lesson>> entry: week.entrySet()) {
                List<Lesson> lessons = new ArrayList<>();
                for (Lesson l: entry.getValue()) if (l.getEven().equals("н")) lessons.add(l);
                evenWeek.put(entry.getKey(), lessons);
            }
            return evenWeek.get(date.get(Calendar.DAY_OF_WEEK));
        } else {
            HashMap<Integer, List<Lesson>> unevenWeek = new HashMap<>();
            for (Map.Entry<Integer, List<Lesson>> entry: week.entrySet()) {
                List<Lesson> lessons = new ArrayList<>();
                for (Lesson l: entry.getValue()) if (l.getEven().equals("в")) lessons.add(l);
                unevenWeek.put(entry.getKey(), lessons);
            }
            return unevenWeek.get(date.get(Calendar.DAY_OF_WEEK));
        }*/

        System.out.println(week);

        List<Lesson> lessons = week.get(date.get(Calendar.DAY_OF_WEEK));
        System.out.println(date.get(Calendar.DAY_OF_WEEK));

        System.out.println(lessons);
        List<Lesson> newList = new ArrayList<>();
        for (Lesson l: lessons) if (l.getEven().equals("н") == ScheduleHelper.isEven(date)) newList.add(l);

        return newList;

        /*return ScheduleHelper.isEven(date) ?
            evenWeek.get(date.get(Calendar.DAY_OF_WEEK)):
            unevenWeek.get(date.get(Calendar.DAY_OF_WEEK));*/
    }

    public HashMap<Integer, List<Lesson>> getUnevenWeek() {
        HashMap<Integer, List<Lesson>> unevenWeek = new HashMap<>();
        for (Map.Entry<Integer, List<Lesson>> entry: week.entrySet()) {
            List<Lesson> lessons = new ArrayList<>();
            for (Lesson l: entry.getValue()) if (l.getEven().equals("в")) lessons.add(l);
            unevenWeek.put(entry.getKey(), lessons);
        }
        return unevenWeek;

        //return unevenWeek;
    }

    public HashMap<Integer, List<Lesson>> getEvenWeek() {
        HashMap<Integer, List<Lesson>> evenWeek = new HashMap<>();
        for (Map.Entry<Integer, List<Lesson>> entry: week.entrySet()) {
            List<Lesson> lessons = new ArrayList<>();
            for (Lesson l: entry.getValue()) if (l.getEven().equals("н")) lessons.add(l);
            evenWeek.put(entry.getKey(), lessons);
        }
        return evenWeek;

        //return evenWeek;
    }

    /*@Override
    @NonNull
    public String toString() {
        return "Schedule {" +
                ", evenWeek=" + evenWeek +
                ", unevenWeek=" + unevenWeek +
                " }";
    }*/

    @Override
    public String toString() {
        return "Schedule{" +
                "week=" + week +
                '}';
    }
}
