package com.example.schedule.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.schedule.R;
import com.example.schedule.model.Lesson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.schedule.ui.MainActivity.months;
import static com.example.schedule.ui.MainActivity.page;
import static com.example.schedule.ui.MainActivity.pageDate;
import static com.example.schedule.ui.MainActivity.schedule;
import static com.example.schedule.ui.MainActivity.weekEvenStyle;

public class WeekFragment extends Fragment {

    int position;

    public static WeekFragment newInstance(int pos) {
        WeekFragment fragment = new WeekFragment();
        fragment.position = pos;
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.page, container, false);

        ListView listView = rootView.findViewById(R.id.week_page);

        Calendar dayDate = (Calendar) pageDate.clone();
        dayDate.add(Calendar.DAY_OF_MONTH, position - page);

        if (schedule == null) return rootView;
        List<Lesson> lessons = schedule.getLessons(dayDate);

        if (lessons.size() == 0) {
            listView.setVisibility(View.GONE);
            rootView.findViewById(R.id.textNoLessons).setVisibility(View.VISIBLE);
        } else {
            List<HashMap<String, String>> data = new ArrayList<>(lessons.size());

            HashMap<String, String> map;
            for (Lesson l : lessons) {
                map = new HashMap<>(Lesson.length);
                String[] values = l.getArray();
                for (int i = 0; i < Lesson.length; i++) map.put(Lesson.names[i], values[i]);
                data.add(map);
            }

            String[] from = Lesson.names;
            int[] to = {R.id.timeLessonBegin, R.id.timeLessonEnd, R.id.lessonName,
                    R.id.lessonTeacher, R.id.lessonLocation, R.id.lessonType};

            SimpleAdapter adapter = new SimpleAdapter(
                    getContext(), data, R.layout.lesson, from, to);

            listView.setAdapter(adapter);
        }


        TextView textDate = rootView.findViewById(R.id.textDate);
        TextView textEvenWeek = rootView.findViewById(R.id.textEvenWeek);

        textDate.setText(String.format(Locale.getDefault(), "%d %s",
                dayDate.get(Calendar.DAY_OF_MONTH), months[dayDate.get(Calendar.MONTH)]));

        String upWeek = weekEvenStyle ? "Верхняя неделя" : "Нечётная неделя";
        String downWeek = weekEvenStyle ? "Нижняя неделя" : "Чётная неделя";
        textEvenWeek.setText((schedule.isEven(dayDate) ? upWeek : downWeek));

        return rootView;
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return WeekFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2000;
        }
    }
}