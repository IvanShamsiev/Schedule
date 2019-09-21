package com.example.schedule.model;

import androidx.annotation.NonNull;

public class Lesson {

    private String beginTime, endTime, shortName, fullName, teacher, location, type;

    public String getBeginTime() {
        return beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    @NonNull
    @Override
    public String toString() {
        return "Lesson { " +
                "beginTime='" + beginTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", shortName='" + shortName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", teacher='" + teacher + '\'' +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                " }";
    }
}
