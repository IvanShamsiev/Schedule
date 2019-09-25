package com.example.schedule.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.model.Lesson;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.example.schedule.ui.MainActivity.months;
import static com.example.schedule.ui.MainActivity.schedule;
import static com.example.schedule.ui.MainActivity.weekEvenStyle;

public class DayFragment extends Fragment {

    private static int pagesCount = 2000;
    public static int middlePos = pagesCount / 2;

    private Calendar currentDate;

    public static DayFragment newInstance(int pos) {
        DayFragment fragment = new DayFragment();
        fragment.currentDate = (Calendar) MainActivity.currentDate.clone();
        fragment.currentDate.add(Calendar.DATE, pos);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true); // For saving state

        View rootView = inflater.inflate(R.layout.fragment_item_page, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        if (schedule == null) return rootView;
        List<Lesson> lessons = schedule.getLessons(currentDate);

        if (lessons.size() != 0) recyclerView.setAdapter(new LessonAdapter(lessons));
        else {
            recyclerView.setVisibility(View.GONE);
            rootView.findViewById(R.id.textNoLessons).setVisibility(View.VISIBLE);
        }


        TextView textDate = rootView.findViewById(R.id.textDate);
        TextView textEvenWeek = rootView.findViewById(R.id.textEvenWeek);

        textDate.setText(String.format(Locale.getDefault(), "%d %s",
                currentDate.get(Calendar.DAY_OF_MONTH), months[currentDate.get(Calendar.MONTH)]));

        String upWeek = weekEvenStyle ? "Верхняя неделя" : "Нечётная неделя";
        String downWeek = weekEvenStyle ? "Нижняя неделя" : "Чётная неделя";
        textEvenWeek.setText((ScheduleHelper.isEven(currentDate) ? downWeek : upWeek));

        return rootView;
    }

    public static class DaysPagerAdapter extends FragmentPagerAdapter {

        public DaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return DayFragment.newInstance(position - middlePos);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar currentDate = (Calendar) MainActivity.currentDate.clone();
            currentDate.add(Calendar.DATE, position - middlePos);

            return MainActivity.dayOfWeek.get(currentDate.get(Calendar.DAY_OF_WEEK)-1);
        }

        @Override
        public int getCount() {
            return pagesCount;
        }
    }
}