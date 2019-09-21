package com.example.schedule.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.model.Lesson;
import com.example.schedule.model.Schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class FullScheduleActivity extends AppCompatActivity {

    Schedule schedule;

    TextView twWeekType;
    ImageButton switchBtn;


    FrameLayout layoutDay1, layoutDay2,
            layoutDay3, layoutDay4,
            layoutDay5, layoutDay6;

    FrameLayout[] layoutDays;


    boolean isEven;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_schedule);

        setTitle("Расписание по неделям");

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());


        schedule = ScheduleHelper.getInstance();

        twWeekType = findViewById(R.id.twWeekType);
        switchBtn = findViewById(R.id.switchBtn);

        layoutDay1 = findViewById(R.id.layoutDay1);
        layoutDay2 = findViewById(R.id.layoutDay2);
        layoutDay3 = findViewById(R.id.layoutDay3);
        layoutDay4 = findViewById(R.id.layoutDay4);
        layoutDay5 = findViewById(R.id.layoutDay5);
        layoutDay6 = findViewById(R.id.layoutDay6);

        layoutDays = new FrameLayout[]
                {layoutDay1, layoutDay2, layoutDay3, layoutDay4, layoutDay5, layoutDay6};

        switchBtn.setOnClickListener(btn -> {if (isEven) setUnevenWeek(); else setEvenWeek();});

        setUnevenWeek();
    }

    void setUnevenWeek() {
        isEven = false;
        twWeekType.setText("Верхняя неделя");

        setLayouts();
    }

    void setEvenWeek() {
        isEven = true;
        twWeekType.setText("Нижняя неделя");

        setLayouts();
    }

    void setLayouts() {

        HashMap<Integer, List<Lesson>> week = isEven ? schedule.getEvenWeek() : schedule.getUnevenWeek();

        ArrayList<String> days = new ArrayList<>(6);

        for (int i = 2; i <= week.size(); i++) {
            StringBuilder lessons = new StringBuilder();
            for (Lesson l: week.get(i))
                lessons.append(l.getBeginTime()).append(" - ").append(l.getShortName()).append("\n");
            days.add(lessons.toString());
        }


        for (FrameLayout l: layoutDays) l.removeAllViews();

        for (int i = 0; i < 6; i++) {
            View day = getLayoutInflater()
                    .inflate(R.layout.week_day_item, layoutDays[i], true);
            TextView twDayOfWeek = day.findViewById(R.id.twDayOfWeek);
            TextView twDayLessons = day.findViewById(R.id.twDayLessons);

            twDayOfWeek.setText(MainActivity.dayOfWeek[i+1]);
            if (MainActivity.currentDate.get(Calendar.DAY_OF_WEEK) == (i+2) &&
                    ScheduleHelper.isEven(MainActivity.currentDate) == isEven)
                twDayOfWeek.setTextColor(getResources().getColor(R.color.colorAccent));

            if (days.get(i).isEmpty()) {
                twDayLessons.setText("Нет пар");
                twDayLessons.setGravity(Gravity.CENTER);
            } else twDayLessons.setText(days.get(i));
        }
    }
}
