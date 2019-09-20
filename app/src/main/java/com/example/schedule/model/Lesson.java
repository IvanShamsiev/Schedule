package com.example.schedule.model;

import java.util.List;

public class Lesson {

    private String beginTime, endTime, name, teacher, location, type;

    public Lesson(String beginTime, String endTime, String name, String teacher, String location, String type) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.name = name;
        this.teacher = teacher;
        this.location = location;
        this.type = type;
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
}
