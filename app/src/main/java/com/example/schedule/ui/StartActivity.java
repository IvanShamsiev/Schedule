package com.example.schedule.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.logic.SheetsHelper;
import com.example.schedule.logic.ServerHelper;
import com.example.schedule.model.Course;
import com.example.schedule.model.Group;
import com.example.schedule.model.Schedule;
import com.example.schedule.util.LoadDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.schedule.ScheduleApplication.branchesUrl;
import static com.example.schedule.ScheduleApplication.currentTheme;
import static com.example.schedule.ScheduleApplication.serverApp;
import static com.example.schedule.ScheduleApplication.serverKpfu;
import static com.example.schedule.ScheduleApplication.showToast;
import static com.example.schedule.ScheduleApplication.url;

public class StartActivity extends AppCompatActivity {

    private static final int CHOSE_FILE_CODE = 1;

    private Handler handler = new Handler();

    private LoadDialog loadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(currentTheme);
        setContentView(R.layout.activity_start);

        loadDialog = new LoadDialog(getSupportFragmentManager());

        Button btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(btn -> {
            loadDialog.show("Загрузка списка групп");
            ServerHelper.call(url + branchesUrl + "?server=" + serverKpfu, getBranchesCallback);
        });

        Button btnChoose = findViewById(R.id.btnChooseFromFile);
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

        Button btnDownloadFromAppServer = findViewById(R.id.btnDownloadFromAppServer);
        btnDownloadFromAppServer.setOnClickListener(btn -> {
            loadDialog.show("Загрузка списка групп");
            ServerHelper.call(url + branchesUrl + "?server=" + serverApp, getBranchesCallback);
        });

        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(btn -> startActivity(new Intent(this, PreferencesActivity.class)));

        if (ScheduleHelper.INSTANCE.getGroup() != null) setResult(RESULT_OK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOSE_FILE_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Uri fileUri = data.getData();
                if (fileUri == null) { showToast(StartActivity.this, "Не удалось определить путь до таблицы"); return; }
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                new Thread(() -> {
                    loadDialog.show("Чтение таблицы");
                    Schedule schedule;
                    try { schedule = SheetsHelper.INSTANCE.getSchedule(inputStream); }
                    catch (Exception e) {
                        showToast(StartActivity.this, "Не удалось прочитать таблицу");
                        e.printStackTrace();
                        return;
                    } finally { loadDialog.close(); }
                    handler.post(() -> {
                        if (schedule == null) Toast.makeText(this, "Не удалось прочитать расписание", Toast.LENGTH_SHORT).show();
                        else openCoursesDialog(schedule.getCourses());
                    });
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                showToast(StartActivity.this, "Не удалось прочитать таблицу");
            }
        }
    }

    Callback getBranchesCallback = new Callback() {

        void showBranchDialog(LinkedHashMap<String, Object> map) {
            new AlertDialog.Builder(StartActivity.this)
                    .setItems(map.keySet().toArray(new String[]{}), (dialogInterface, i) -> {
                        Object value = new ArrayList<>(map.values()).get(i);
                        if (value instanceof String) {
                            loadDialog.show("Загрузка таблицы");
                            ServerHelper.call((String) value, getBranchCallback);
                        }
                        else showBranchDialog(new LinkedHashMap<String, Object>((Map) value));
                    })
                    .show();
        }

        @Override
        public void onFailure(Call call, IOException e) {
            loadDialog.close();
            showToast(StartActivity.this, "Не удалось загрузить список отделений");
        }

        @Override
        public void onResponse(Call call, Response response) {
            loadDialog.close();
            String branchesJson;
            try {
                if (response.body() == null) throw new NullPointerException("Тело ответа сервера равно null");
                branchesJson = response.body().string();
            }
            catch (IOException | NullPointerException e) {
                showToast(StartActivity.this, "Произошла ошибка при чтении списка отделений");
                return;
            }

            Type type = new TypeToken<LinkedHashMap<String, Object>>(){}.getType();
            LinkedHashMap<String, Object> branchesEntries = new Gson().fromJson(branchesJson, type);
            handler.post(() -> showBranchDialog(branchesEntries));
        }
    };

    Callback getBranchCallback = new Callback() {

        @Override
        public void onFailure(Call call, IOException e) {
            loadDialog.close();
            showToast(StartActivity.this, "Не удалось загрузить список групп");
        }

        @Override
        public void onResponse(Call call, Response response) {
            loadDialog.changeText("Чтение таблицы");
            try {
                if (response.body() == null) throw new NullPointerException("Тело ответа серверо равно null");
                Schedule schedule = SheetsHelper.INSTANCE.getSchedule(response.body().byteStream());
                handler.post(() -> {
                    if (schedule == null) showToast(StartActivity.this,"Не удалось прочитать расписание");
                    else openCoursesDialog(schedule.getCourses());
                });
            } catch (NullPointerException e) {
                showToast(StartActivity.this, "Не удалось прочитать таблицу");
                e.printStackTrace();
            } finally { loadDialog.close(); }
        }

    };

    void openCoursesDialog(List<Course> courses) {
        List<String> coursesNames = new ArrayList<>(courses.size());
        for (Course c: courses) coursesNames.add(c.getName());
        new AlertDialog.Builder(StartActivity.this)
                .setItems(coursesNames.toArray(new String[]{}), (dialogInterface, i) ->
                        openGroupsDialog(courses.get(i))
                )
                .show();
    }

    void openGroupsDialog(Course course) {
        List<Group> groups = course.getGroups();
        List<String> groupsNames = new ArrayList<>(groups.size());
        for (Group g: groups) groupsNames.add(g.getName());

        new AlertDialog.Builder(StartActivity.this)
                .setItems(groupsNames.toArray(new String[]{}), (dialogInterface, i) -> {
                    ScheduleHelper.INSTANCE.saveGroup(groups.get(i));
                    setResult(RESULT_OK);
                    finish();
                })
                .show();
    }



    public static Intent newIntent(Context ctx) {
        return new Intent(ctx, StartActivity.class);
    }



}
