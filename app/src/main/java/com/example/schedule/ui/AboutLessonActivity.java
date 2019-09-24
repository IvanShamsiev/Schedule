package com.example.schedule.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.TextView;

import com.example.schedule.R;
import com.example.schedule.model.Lesson;

public class AboutLessonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_lesson);

        setTitle("О предмете");

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());


        TextView twBeginTime, twEndTime, twName, twTeacher, twLocation, twType;

        twBeginTime = findViewById(R.id.twBeginTime);
        twEndTime = findViewById(R.id.twEndTime);
        twName = findViewById(R.id.twName);
        twTeacher = findViewById(R.id.twTeacher);
        twLocation = findViewById(R.id.twLocation);
        twType = findViewById(R.id.twType);

        Lesson lesson = getIntent().getParcelableExtra("lesson");

        twBeginTime.setText(lesson.getBeginTime());
        twEndTime.setText(lesson.getEndTime());
        twName.setText(lesson.getName());
        twTeacher.setText(lesson.getTeacher());
        twLocation.setText(lesson.getLocation());
        twType.setText(lesson.getType());
    }
}
