package com.example.schedule.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.model.Lesson;
import com.example.schedule.ui.MainActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.example.schedule.ui.MainActivity.weekEvenStyle;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private static int pagesCount = Integer.MAX_VALUE;
    public static int middlePos = pagesCount / 2;

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View dayView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_page, parent, false);
        DayViewHolder dayViewHolder = new DayViewHolder(dayView);
        return dayViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Calendar date = (Calendar) MainActivity.currentDate.clone();
        date.add(Calendar.DATE, position - middlePos);
        holder.bind(date);
    }

    @Override
    public int getItemCount() {
        return pagesCount;
    }


    class DayViewHolder extends RecyclerView.ViewHolder {

        RecyclerView lessonsRecyclerView;
        TextView textDate, textEvenWeek;
        TextView textNoLessons;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);

            lessonsRecyclerView = itemView.findViewById(R.id.recyclerView);
            lessonsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(),
                    LinearLayoutManager.VERTICAL, false));
            lessonsRecyclerView.addItemDecoration(new DividerItemDecoration(itemView.getContext(),
                    DividerItemDecoration.VERTICAL));

            textDate = itemView.findViewById(R.id.textDate);
            textEvenWeek = itemView.findViewById(R.id.textEvenWeek);

            textNoLessons = itemView.findViewById(R.id.textNoLessons);
        }

        void bind(Calendar date) {

            String month = date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            month = month.substring(0, 1).toUpperCase() + month.substring(1);
            textDate.setText(String.format(Locale.getDefault(), "%d %s",
                    date.get(Calendar.DAY_OF_MONTH), month));

            String upWeek = weekEvenStyle ? "Верхняя неделя" : "Нечётная неделя";
            String downWeek = weekEvenStyle ? "Нижняя неделя" : "Чётная неделя";
            textEvenWeek.setText((ScheduleHelper.isEven(date) ? downWeek : upWeek));


            if (ScheduleHelper.getSchedule() == null) return;
            List<Lesson> lessons = ScheduleHelper.getSchedule().getLessons(date);

            if (lessons.size() != 0) {
                lessonsRecyclerView.setVisibility(View.VISIBLE);
                textNoLessons.setVisibility(View.GONE);
                lessonsRecyclerView.setAdapter(new LessonAdapter(lessons));
            }
            else {
                lessonsRecyclerView.setVisibility(View.GONE);
                textNoLessons.setVisibility(View.VISIBLE);
                lessonsRecyclerView.setAdapter(null);
            }
        }
    }
}
