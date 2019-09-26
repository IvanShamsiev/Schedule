package com.example.schedule.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContentResolverCompat;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.logic.SheetsHelper;
import com.example.schedule.logic.StartHelper;
import com.example.schedule.model.Lesson;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.schedule.ui.MainActivity.scheduleFileName;

public class StartActivity extends AppCompatActivity {

    private static final int CHOSE_FILE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(btn -> StartHelper.getBranches(getBranchesCallback));

        Button btnChoose = findViewById(R.id.btnChoose);
        btnChoose.setOnClickListener(btn -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            try {
                startActivityForResult(Intent.createChooser(intent, "Выберите таблицу"), CHOSE_FILE_CODE);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Серьёзно? Установи файловый менеджер", Toast.LENGTH_SHORT).show();
            }
        });

        setResult(RESULT_CANCELED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOSE_FILE_CODE && resultCode == RESULT_OK) {
            try {
                Uri fileUri = data.getData();
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                new Thread(() -> {
                    coursesMap = SheetsHelper.getCoursesMap(inputStream);
                    getBranchHandler.sendEmptyMessage(0);
                }).start();

            } catch (Exception e) {
                Toast.makeText(this, "Не удалось прочитать таблицу", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    Callback getBranchesCallback = new Callback() {
        LinkedHashMap<String, Object> branchesEntries;

        Handler getBranchesHandler = new Handler(msg -> {
            showDialog(branchesEntries);
            return true;
        });

        void showDialog(LinkedHashMap<String, Object> map) {
            new AlertDialog.Builder(StartActivity.this)
                    .setItems(map.keySet().toArray(new String[]{}), (dialogInterface, i) -> {
                        Object value = new ArrayList<>(map.values()).get(i);
                        if (value instanceof String) StartHelper.getBranch((String) value, getBranchCallback);
                        else showDialog(new LinkedHashMap<>((Map) value));
                    })
                    .show();
        }

        @Override
        public void onFailure(Call call, IOException e) {
            Toast.makeText(StartActivity.this, "Не удалось загрузить список отделений",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String branchesJson;
            try {branchesJson = response.body().string();}
            catch (IOException | NullPointerException e) {
                Toast.makeText(StartActivity.this, "Произошла ошибка при чтении списка отделений",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            branchesEntries = new Gson().fromJson(branchesJson, LinkedHashMap.class);

            getBranchesHandler.sendEmptyMessage(0);
        }
    };

    Callback getBranchCallback = new Callback() {

        @Override
        public void onFailure(Call call, IOException e) {
            Toast.makeText(StartActivity.this, "Не удалось загрузить список групп",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            coursesMap = SheetsHelper.getCoursesMap(response.body().byteStream());
            getBranchHandler.sendEmptyMessage(0);
        }
    };

    HashMap<String, HashMap<String, HashMap<Integer, List<Lesson>>>> coursesMap;

    Handler getBranchHandler = new Handler(msg -> {
        openCoursesDialog(new TreeMap<>(coursesMap));
        return true;
    });

    void openCoursesDialog(TreeMap<String, HashMap<String, HashMap<Integer, List<Lesson>>>> coursesMap) {
        new AlertDialog.Builder(StartActivity.this)
                .setItems(coursesMap.keySet().toArray(new String[]{}), (dialogInterface, i) ->
                        openGroupsDialog(new TreeMap<>(new ArrayList<>(coursesMap.values()).get(i)))
                )
                .show();
    }

    void openGroupsDialog(TreeMap<String, HashMap<Integer, List<Lesson>>> groupsMap) {
        new AlertDialog.Builder(StartActivity.this)
                .setItems(groupsMap.keySet().toArray(new String[]{}), (dialogInterface, i) -> {
                    HashMap<Integer, List<Lesson>> weekMap = new ArrayList<>(groupsMap.values()).get(i);
                    String json = new Gson().toJson(weekMap);
                    json = "{\"week\":" + json + "}";
                    System.out.println(json);
                    try { ScheduleHelper.saveSchedule(json, openFileOutput(scheduleFileName, MODE_PRIVATE)); }
                    catch (FileNotFoundException e) { e.printStackTrace(); }

                    onSuccess();
                })
                .show();
    }



}
