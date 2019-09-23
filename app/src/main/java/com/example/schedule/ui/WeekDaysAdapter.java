package com.example.schedule.ui;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.model.Lesson;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class WeekDaysAdapter extends RecyclerView.Adapter<WeekDaysAdapter.WeekDayHolder> {

    private HashMap<Integer, List<Lesson>> week;
    private boolean isEven;

    public WeekDaysAdapter(HashMap<Integer, List<Lesson>> week, boolean isEven) {
        this.week = week;
        this.isEven = isEven;
    }

    @NonNull
    @Override
    public WeekDayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.week_day_item, parent, false);
        return new WeekDayHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekDayHolder holder, int position) {
        WeekDay weekDay = new WeekDay(MainActivity.dayOfWeek[position + 1], week.get(position + 2),
                ScheduleHelper.isEven(MainActivity.currentDate) == isEven &&
                        MainActivity.currentDate.get(Calendar.DAY_OF_WEEK) == (position + 2));

        holder.onBind(weekDay);
    }

    @Override
    public int getItemCount() {
        return week.size() - 1;
    }

    public void updateWeek(HashMap<Integer, List<Lesson>> week, boolean isEven) {
        this.week = week;
        this.isEven = isEven;

        notifyDataSetChanged();
    }




    class WeekDayHolder extends RecyclerView.ViewHolder {

        TextView twDayOfWeek;
        TextView twDayLessons;

        WeekDayHolder(@NonNull View itemView) {
            super(itemView);

            twDayOfWeek = itemView.findViewById(R.id.twDayOfWeek);
            twDayLessons = itemView.findViewById(R.id.twDayLessons);
        }

        void onBind(WeekDay weekDay) {

            twDayOfWeek.setText(weekDay.getDayOfWeek());

            if (weekDay.isToday())
                twDayOfWeek.setTextColor(itemView.getResources().getColor(R.color.colorAccent));
            else
                twDayOfWeek.setTextColor(itemView.getResources().getColor(R.color.colorPrimary));

            if (weekDay.getLessons().isEmpty()) {
                twDayLessons.setText("Нет пар");
                twDayLessons.setGravity(Gravity.CENTER);
            } else {
                twDayLessons.setText(weekDay.getStringLessons());
                twDayLessons.setGravity(Gravity.START);
            }
        }
    }




    private class WeekDay {

        private String dayOfWeek;
        private List<Lesson> lessons;
        private boolean isToday;

        WeekDay(String dayOfWeek, List<Lesson> lessons, boolean isToday) {
            this.dayOfWeek = dayOfWeek;
            this.lessons = lessons;
            this.isToday = isToday;
        }

        String getDayOfWeek() {
            return dayOfWeek;
        }

        List<Lesson> getLessons() {
            return lessons;
        }

        boolean isToday() {
            return isToday;
        }


        String getStringLessons() {
            StringBuilder stringBuilder = new StringBuilder();

            for (Lesson l: lessons) stringBuilder.append(l.getBeginTime())
                    .append(" - ").append(l.getShortName()).append("\n");

            return stringBuilder.toString();
        }
    }
}
