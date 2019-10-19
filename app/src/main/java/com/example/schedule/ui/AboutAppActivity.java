package com.example.schedule.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.schedule.BuildConfig;
import com.example.schedule.R;

import java.util.Arrays;
import java.util.List;

import static com.example.schedule.ScheduleApplication.currentTheme;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(currentTheme);
        setContentView(R.layout.activity_about_app);

        setTitle("О приложении");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new AboutAppAdapter());
    }

    private class AboutAppAdapter extends RecyclerView.Adapter<AboutAppAdapter.ViewHolder> {

        List<String> lines;

        AboutAppAdapter() {
            lines = Arrays.asList(
                    "Название приложения: " + getString(R.string.app_name),
                    "Версия приложения: " + BuildConfig.VERSION_NAME,
                    "Наша группа в ВК: " + getString(R.string.vk)
            );
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.item_about_app, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return lines.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView textView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                textView = itemView.findViewById(R.id.textView);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            void bind(int pos) {
                textView.setText(Html.fromHtml(lines.get(pos)));
            }
        }
    }


    public static Intent newIntent(Context ctx) {
        return new Intent(ctx, AboutAppActivity.class);
    }

}
