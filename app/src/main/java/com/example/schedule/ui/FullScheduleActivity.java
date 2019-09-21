package com.example.schedule.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.model.Lesson;
import com.example.schedule.model.Schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FullScheduleActivity extends AppCompatActivity {

    Schedule schedule;

    TextView twWeekType;
    ImageButton switchBtn;


    FrameLayout layoutDay1, layoutDay2, layoutDay3, layoutDay4, layoutDay5, layoutDay6;

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
        HashMap<Integer, List<Lesson>> unevenWeek = schedule.getUnevenWeek();

        ArrayList<String> strings = new ArrayList<>(12);


        for (int i = 2; i <= unevenWeek.size(); i++) {
            String text = "";
            for (Lesson l: unevenWeek.get(i)) text += l.getBeginTime() + " - " + l.getShortName() + "\n";

            strings.add(text);
        }

        for (FrameLayout l: layoutDays) l.removeAllViews();

        for (int i = 0; i < 6; i++) {
            View view = getLayoutInflater().inflate(R.layout.week_day_item, layoutDays[i], false);
            ((TextView) view.findViewById(R.id.textWeekDay)).setText(MainActivity.dayOfWeek[i+1]);

            if (strings.get(i).isEmpty()) {
                ((TextView) view.findViewById(R.id.textDayLessons)).setText("Нет пар");
                ((TextView) view.findViewById(R.id.textDayLessons)).setGravity(Gravity.CENTER);
            } else ((TextView) view.findViewById(R.id.textDayLessons)).setText(strings.get(i));
            layoutDays[i].addView(view);
        }

        twWeekType.setText("Верхняя неделя");
        isEven = false;
    }

    void setEvenWeek() {
        HashMap<Integer, List<Lesson>> evenWeek = schedule.getEvenWeek();

        ArrayList<String> strings = new ArrayList<>(12);

        for (int i = 2; i <= evenWeek.size(); i++) {
            String text = "";
            for (Lesson l: evenWeek.get(i)) text += l.getBeginTime() + " - " + l.getShortName() + "\n";

            strings.add(text);
        }

        for (FrameLayout l: layoutDays) l.removeAllViews();

        for (int i = 0; i < 6; i++) {
            View view = getLayoutInflater().inflate(R.layout.week_day_item, layoutDays[i], false);
            ((TextView) view.findViewById(R.id.textWeekDay)).setText(MainActivity.dayOfWeek[i+1]);

            if (strings.get(i).isEmpty()) {
                ((TextView) view.findViewById(R.id.textDayLessons)).setText("Нет пар");
                ((TextView) view.findViewById(R.id.textDayLessons)).setGravity(Gravity.CENTER);
            } else ((TextView) view.findViewById(R.id.textDayLessons)).setText(strings.get(i));

            layoutDays[i].addView(view);
        }

        twWeekType.setText("Нижняя неделя");
        isEven = true;
    }

    private class WeekDayAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }

    /*void editGridView() {
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setColumnWidth(160);
        gridView.setHorizontalSpacing(5);
        gridView.setVerticalSpacing(5);
        gridView.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
    }*/
}
