package com.example.schedule.model;

import java.util.List;

public class Lesson {
    private String  beginTime, endTime, name, teacher, location, type;

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

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
