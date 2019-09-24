package com.example.schedule.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Lesson implements Parcelable {

    private String beginTime, endTime, name, teacher, location, type;

    public String getBeginTime() {
        return beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getName() {
        return name;
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
                ", name='" + name + '\'' +
                ", teacher='" + teacher + '\'' +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                " }";
    }


    // For parcelable
    private Lesson(Parcel source) {
        String[] strings = new String[6];
        source.readStringArray(strings);

        beginTime = strings[0];
        endTime = strings[1];
        name = strings[2];
        teacher = strings[3];
        location = strings[4];
        type = strings[5];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {beginTime, endTime, name, teacher, location, type});
    }

    public static final Parcelable.Creator<Lesson> CREATOR = new Parcelable.Creator<Lesson>() {

        @Override
        public Lesson createFromParcel(Parcel source) {
            return new Lesson(source);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };
}
