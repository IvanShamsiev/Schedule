package com.example.schedule.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.logic.SheetsHelper;
import com.example.schedule.logic.StartHelper;
import com.example.schedule.model.Lesson;
import com.example.schedule.model.Schedule;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.schedule.ui.MainActivity.scheduleFileName;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button btnUnder = findViewById(R.id.btnUnder);

        btnUnder.setOnClickListener(btn -> StartHelper.getBranches(getBranchesCallback));


        Button btnLoad = findViewById(R.id.btnLoad);
        btnLoad.setOnClickListener(btn -> {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    Callback getBranchesCallback = new Callback() {
        LinkedHashMap<String, String> branchesEntries;

        Handler getBranchesHandler = new Handler(msg -> {
            new AlertDialog.Builder(StartActivity.this)
                    .setItems(branchesEntries.keySet().toArray(new String[]{}),
                            (dialogInterface, i) -> StartHelper.getBranch(
                                    new ArrayList<>(branchesEntries.values()).get(i), getBranchCallback))
                    .show();
            return true;
        });

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

        HashMap<String, HashMap<String, HashMap<Integer, List<Lesson>>>> coursesMap;

        Handler getBranchHandler = new Handler(msg -> {
            openCoursesDialog(new TreeMap<>(coursesMap));
            return true;
        });

        void openCoursesDialog(TreeMap<String, HashMap<String, HashMap<Integer, List<Lesson>>>> coursesMap) {
            new AlertDialog.Builder(StartActivity.this)
                    .setItems(coursesMap.keySet().toArray(new String[]{}), (dialogInterface, i) -> {
                        openGroupsDialog(new TreeMap<>(new ArrayList<>(coursesMap.values()).get(i)));
                        dialogInterface.dismiss();
                    })
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
                        Intent intent = new Intent(StartActivity.this, MainActivity.class);
                        startActivity(intent);
                    })
                    .show();
        }

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            coursesMap = SheetsHelper.getCoursesMap(response.body().byteStream());
            getBranchHandler.sendEmptyMessage(0);
        }
    };



}
