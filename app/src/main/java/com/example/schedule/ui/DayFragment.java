package com.example.schedule.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.schedule.R;
import com.example.schedule.model.Lesson;

import java.util.Calendar;
import java.util.List;

import static com.example.schedule.ui.MainActivity.schedule;

public class DayFragment extends Fragment {

    public static int pagesCount = 14 + 2;

    public static DayFragment newInstance(int pos) {
        DayFragment fragment = new DayFragment();
        fragment.pos = pos;
        return fragment;
    }

    private int pos;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true); // For saving state

        View rootView = inflater.inflate(R.layout.fragment_item_page, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));

        if (schedule == null) return rootView;

        Calendar currentDate = (Calendar) MainActivity.currentDate.clone();
        currentDate.add(Calendar.DATE, pos - 1);
        List<Lesson> lessons = schedule.getLessons(currentDate);

        if (lessons.size() != 0) recyclerView.setAdapter(new LessonAdapter(lessons));
        else rootView.findViewById(R.id.textNoLessons).setVisibility(View.VISIBLE);

        return rootView;
    }

    public static class DaysPagerAdapter extends FragmentPagerAdapter {

        public DaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return DayFragment.newInstance(position);
        }

        /*@Override
        public CharSequence getPageTitle(int position) {
            Calendar currentDate = (Calendar) MainActivity.currentDate.clone();
            currentDate.add(Calendar.DATE, position - middlePos);

            return MainActivity.dayOfWeek[currentDate.get(Calendar.DAY_OF_WEEK)-1];
        }*/

        @Override
        public int getCount() {
            return pagesCount;
        }
    }
}