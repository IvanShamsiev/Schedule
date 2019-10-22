package com.example.schedule.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class Lesson implements Parcelable {

    public static final int fieldsCount = 9;

    private String beginTime, endTime, even, name, location, type, chair, post, teacher;

    public Lesson(String beginTime, String endTime, String even, String name, String location,
                  String type, String chair, String post, String teacher) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.even = even;
        this.name = name;
        this.location = location;
        this.type = type;
        this.chair = chair;
        this.post = post;
        this.teacher = teacher;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getEven() {
        return even;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public String getChair() {
        return chair;
    }

    public String getPost() {
        return post;
    }

    public String getTeacher() {
        return teacher;
    }

    public List<String> getNames() {
        return Arrays.asList("Время начала", "Время окончания", "Неделя", "Название предмета",
                "Аудитория", "Тип пары", "Кафедра", "Должность", "Преподаватель");
    }

    public List<String> getValues() {
        return Arrays.asList(beginTime, endTime, even, name, location, type, chair, post, teacher);
    }

    @NonNull
    @Override
    public String toString() {
        return "Lesson { " +
                "beginTime='" + beginTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", even='" + even + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                ", chair='" + chair + '\'' +
                ", post='" + post + '\'' +
                ", teacher='" + teacher + '\'' +
                " }";
    }

    /*@NonNull
    @Override
    public String toString() {
        return "L";
    }*/

    // For parcelable
    private Lesson(Parcel source) {
        String[] strings = new String[fieldsCount];
        source.readStringArray(strings);

        beginTime = strings[0];
        endTime = strings[1];
        even = strings[2];
        name = strings[3];
        location = strings[4];
        type = strings[5];
        chair = strings[6];
        post = strings[7];
        teacher = strings[8];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {beginTime, endTime, even, name, location, type, chair, post, teacher});
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
