package com.example.schedule.ui;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ScheduleChanger extends AppCompatActivity {

    private EditText editSchedule;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_changer);

        editSchedule = findViewById(R.id.changed_schedule);
        sendButton = findViewById(R.id.setSchedule);

        ScheduleHelper.downloadSchedule(onDownloadCallback);
    }

    Callback onDownloadCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Toast.makeText(ScheduleChanger.this, "Не удалось загрузить расписание",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            onDownloadHandler.sendEmptyMessage(0);
        }
    };

    Callback onScheduleChangedCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Toast.makeText(ScheduleChanger.this,
                    "Отправка нового расписания не удалась", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            onChangedHandler.sendMessage(onChangedHandler.obtainMessage(0,
                    response.body().string()));
        }
    };

    Handler onDownloadHandler = new Handler(msg -> {
        editSchedule.setText(ScheduleHelper.getStringSchedule());
        sendButton.setOnClickListener(btn -> ScheduleHelper.sendSchedule(
                onScheduleChangedCallback, editSchedule.getText().toString()));
        return true;
    });

    Handler onChangedHandler = new Handler(msg -> {
        Toast.makeText(ScheduleChanger.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
        setResult(1);
        finish();
        return true;
    });
}
