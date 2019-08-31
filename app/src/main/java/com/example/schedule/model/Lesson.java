package com.example.schedule.model;

import java.util.List;

public class Lesson {
    String  beginTime, endTime, name, teacher, location, type;

    public static String[] names = {"beginTime", "endTime", "name", "teacher", "location", "type"};
    public static byte length = 6;

    Lesson(String bt, String et, String n, String tch, String l, String t) {
        beginTime = bt;
        endTime = et;
        name = n;
        teacher = tch;
        location = l;
        type = t;
    }

    Lesson(List<String> list) {
        beginTime = list.get(0);
        endTime = list.get(1);
        name = list.get(2);
        teacher = list.get(3);
        location = list.get(4);
        type = list.get(5);
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "beginTime='" + beginTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", name='" + name + '\'' +
                ", teacher='" + teacher + '\'' +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public String[] getArray() {
        return new String[] {beginTime, endTime, name, teacher, location, type};
    }
}
