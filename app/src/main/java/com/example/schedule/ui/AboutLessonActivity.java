package com.example.schedule.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.schedule.R;
import com.example.schedule.model.Lesson;

public class AboutLessonActivity extends AppCompatActivity {

    Lesson lesson;

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

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new AboutLessonAdapter());

        lesson = getIntent().getParcelableExtra("lesson");
    }

    class AboutLessonAdapter extends RecyclerView.Adapter<AboutLessonAdapter.LessonViewHolder> {

        @NonNull
        @Override
        public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about_lesson, parent, false);
            LessonViewHolder lessonViewHolder = new LessonViewHolder(view);
            return lessonViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return lesson.getNames().size();
        }

        class LessonViewHolder extends RecyclerView.ViewHolder {

            TextView twName, twValue;

            public LessonViewHolder(@NonNull View itemView) {
                super(itemView);

                twName = itemView.findViewById(R.id.twName);
                twValue = itemView.findViewById(R.id.twValue);
            }

            void bind(int position) {
                twName.setText(lesson.getNames().get(position));
                twValue.setText(lesson.getValues().get(position));
            }
        }
    }
}
