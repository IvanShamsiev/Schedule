package com.example.schedule.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.model.Schedule;

public class FullScheduleActivity extends AppCompatActivity {

    Schedule schedule;

    TextView twWeekType;
    ImageButton switchBtn;

    RecyclerView recyclerView;
    WeekDaysAdapter weekAdapter;

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

        switchBtn.setOnClickListener(btn -> {if (isEven) setUnevenWeek(); else setEvenWeek();});


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(
                this, 2, LinearLayoutManager.VERTICAL, false));


        weekAdapter = new WeekDaysAdapter(schedule.getEvenWeek(), true);

        recyclerView.setAdapter(weekAdapter);

        setUnevenWeek();
    }



    void setUnevenWeek() {
        isEven = false;
        twWeekType.setText("Верхняя неделя");

        weekAdapter.updateWeek(schedule.getUnevenWeek(), false);
    }

    void setEvenWeek() {
        isEven = true;
        twWeekType.setText("Нижняя неделя");

        weekAdapter.updateWeek(schedule.getEvenWeek(), true);
    }
}
