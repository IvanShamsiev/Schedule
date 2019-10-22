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
import com.example.schedule.model.Lesson;
import com.example.schedule.util.LoadDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

import static com.example.schedule.ScheduleApplication.branchesUrl;
import static com.example.schedule.ScheduleApplication.currentTheme;
import static com.example.schedule.ScheduleApplication.serverApp;
import static com.example.schedule.ScheduleApplication.serverKpfu;
import static com.example.schedule.ScheduleApplication.showToast;
import static com.example.schedule.ScheduleApplication.scheduleFileName;
import static com.example.schedule.ScheduleApplication.url;

public class StartActivity extends AppCompatActivity {

    private static final int CHOSE_FILE_CODE = 1;

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
        btnSettings.setOnClickListener(btn -> startActivity(PreferencesActivity.newIntent(this)));


        if (ScheduleHelper.getSchedule() != null) setResult(RESULT_OK);
        else setResult(RESULT_CANCELED);
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
                    try { coursesMap = SheetsHelper.getCoursesMap(inputStream); }
                    catch (Exception e) { showToast(StartActivity.this, "Не удалось прочитать таблицу"); return; }
                    finally { loadDialog.close(); }
                    getBranchHandler.sendEmptyMessage(0);
                }).start();

            } catch (Exception e) {
                showToast(StartActivity.this, "Не удалось прочитать таблицу");
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
                        if (value instanceof String) {
                            loadDialog.show("Загрузка таблицы");
                            ServerHelper.call((String) value, getBranchCallback);
                        }
                        else showDialog(new LinkedHashMap<String, Object>((Map) value));
                    })
                    .show();
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call call, IOException e) {
            loadDialog.close();
            showToast(StartActivity.this, "Не удалось загрузить список отделений");
        }

        @Override
        @EverythingIsNonNull
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

            branchesEntries = new Gson().fromJson(branchesJson, LinkedHashMap.class);

            getBranchesHandler.sendEmptyMessage(0);
        }
    };

    Callback getBranchCallback = new Callback() {

        @Override
        @EverythingIsNonNull
        public void onFailure(Call call, IOException e) {
            loadDialog.close();
            showToast(StartActivity.this, "Не удалось загрузить список групп");
        }

        @Override
        @EverythingIsNonNull
        public void onResponse(Call call, Response response) {
            loadDialog.changeText("Чтение таблицы");
            try {
                if (response.body() == null) throw new NullPointerException("Тело ответа серверо равно null");
                coursesMap = SheetsHelper.getCoursesMap(response.body().byteStream());
            }
            catch (Exception e) { showToast(StartActivity.this, "Не удалось прочитать таблицу"); return; }
            finally { loadDialog.close(); }
            getBranchHandler.sendEmptyMessage(0);
        }

    };

    HashMap<String, HashMap<String, HashMap<Integer, List<Lesson>>>> coursesMap;

    Handler getBranchHandler = new Handler(msg -> {
        if (coursesMap == null) Toast.makeText(this, "Не удалось прочитать расписание", Toast.LENGTH_SHORT).show();
        else openCoursesDialog(new TreeMap<>(coursesMap));
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
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("week", new Gson().toJsonTree(weekMap));
                    String json = jsonObject.toString();
                    try { ScheduleHelper.saveSchedule(json, openFileOutput(scheduleFileName, MODE_PRIVATE)); }
                    catch (IOException e) { e.printStackTrace(); }

                    onSuccess();
                })
                .show();
    }



    public static Intent newIntent(Context ctx) {
        return new Intent(ctx, StartActivity.class);
    }

}
