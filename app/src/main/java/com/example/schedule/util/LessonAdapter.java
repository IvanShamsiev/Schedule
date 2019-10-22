package com.example.schedule.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.schedule.R;
import com.example.schedule.model.Lesson;
import com.example.schedule.ui.AboutLessonActivity;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons;

    public LessonAdapter(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lesson,
                viewGroup, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder lessonViewHolder, int i) {
        lessonViewHolder.bind(lessons.get(i));
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {

        TextView beginTime, endTime, name, teacher, location, type;

        Lesson lesson;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind views
            beginTime = itemView.findViewById(R.id.timeLessonBegin);
            endTime = itemView.findViewById(R.id.timeLessonEnd);
            name = itemView.findViewById(R.id.lessonName);
            teacher = itemView.findViewById(R.id.lessonTeacher);
            location = itemView.findViewById(R.id.lessonLocation);
            type = itemView.findViewById(R.id.lessonType);

            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(itemView.getContext(), AboutLessonActivity.class);
                intent.putExtra("lesson", lesson);
                itemView.getContext().startActivity(intent);
            });
        }

        void bind(Lesson lesson) {
            this.lesson = lesson;

            beginTime.setText(lesson.getBeginTime());
            endTime.setText(lesson.getEndTime());
            name.setText(lesson.getName());
            teacher.setText(lesson.getTeacher());
            location.setText(lesson.getLocation());
            type.setText(lesson.getType());
        }
    }
}
