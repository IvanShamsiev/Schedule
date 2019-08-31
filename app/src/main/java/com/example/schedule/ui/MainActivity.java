package com.example.schedule.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.model.Schedule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static Schedule schedule;
    static Calendar currentDate;
    public static Calendar pageDate;


    ViewPager viewPager;
    static WeekFragment.SectionsPagerAdapter adapter;
    static SharedPreferences preferences;
    static boolean lessonNames; // false: short; true: full
    public static boolean weekEvenStyle; // false: В-Н; true: Ч-Н

    public static int page = 1000;

    public static final String scheduleFileName = "schedule.json";
    public static final String url = "https://schedule2171112.000webhostapp.com/";
    public static final String[] months = {"Января", "Февраля", "Марта", "Апреля", "Мая", "Июня", "Июля",
            "Августа", "Сентября", "Октября", "Ноября", "Декабря"};

    private static final String[] dayOfWeek = {"Воскресенье", "Поненельник", "Вторник", "Среда",
            "Четверг", "Пятница", "Суббота"};

    AlertDialog changeScheduleDialog;

    //boolean currentConfigChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        currentDate = new GregorianCalendar();
        pageDate = new GregorianCalendar();

        viewPager = findViewById(R.id.container);

        changeScheduleDialog = new AlertDialog.Builder(this)
                .setTitle("Введите пароль")
                .setView(getLayoutInflater().inflate(R.layout.change_schedule_dialog, null))
                .setPositiveButton("Ок", checkPass)
                .setNegativeButton("Отмена", null)
                .create();
    }


    @Override
    protected void onResume() {
        lessonNames = preferences.getBoolean("full_lesson_names", false);
        weekEvenStyle = preferences.getString("week_even_style", "0").equals("0");

        try { ScheduleHelper.loadSchedule(openFileInput(scheduleFileName), callback); }
        catch (FileNotFoundException e) { ScheduleHelper.downloadSchedule(callback); }

        schedule = ScheduleHelper.getInstance(lessonNames);

        setAdapter();

        super.onResume();
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("isOrientationChanged", true);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        currentConfigChanged = savedInstanceState.getBoolean("isOrientationChanged", false);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0 && resultCode == 1) ScheduleHelper.downloadSchedule(callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.preferences).setIntent(
                new Intent(this, PreferencesActivity.class));
        return true;
    }


    private boolean setAdapter() {
        setTitle(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK)-1]);

        adapter = new WeekFragment.SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(page);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                if (arg0 - page == -1) { // left
                    page--;
                    pageDate.add(Calendar.DAY_OF_MONTH, -1);
                } else if (arg0 - page == 1) { // right
                    page++;
                    pageDate.add(Calendar.DAY_OF_MONTH, +1);
                }

                setTitle(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK)-1]);
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {}
            public void onPageScrollStateChanged(int arg0) {}
        });

        return true;
    }





    public void showCalendar(MenuItem item) {
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this, dateSetListener, year, month, day);
        dialog.setTitle("Выберите дату");
        dialog.show();
    }

    DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
        pageDate.set(year, month, dayOfMonth);
        viewPager.removeAllViews();
        setAdapter();

    };




    public void downloadSchedule(@Nullable MenuItem item) {
        ScheduleHelper.downloadSchedule(callback);
    }



    public void changeSchedule(@Nullable MenuItem item) {
        changeScheduleDialog.show();
    }

    DialogInterface.OnClickListener checkPass = (dialog, which) -> {
        EditText editTextPass = ((AlertDialog) dialog).findViewById(R.id.editTextPass);
        String pass = editTextPass.getText().toString();

        editTextPass.setText("");

        if (pass.equals("02281488")) {
            Intent changeSchedule = new Intent(this, ScheduleChanger.class);
            startActivityForResult(changeSchedule, 0);
        } else Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show();


    };

    Callback callback = new Callback() {

        Handler setAdapterHandler = new Handler(msg -> setAdapter());

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Toast.makeText(MainActivity.this, "Не удалось загрузить расписание",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResponse(Call call, @NonNull Response response) {
            String json;
            try {json = response.body().string();}
            catch (IOException | NullPointerException e) {e.printStackTrace(); return;}

            try {
                ScheduleHelper.saveSchedule(json, openFileOutput(scheduleFileName , MODE_PRIVATE));
                ScheduleHelper.setSchedule(json);

                schedule = ScheduleHelper.getInstance(lessonNames);

                setAdapterHandler.sendEmptyMessage(0);
            } catch (FileNotFoundException e) { e.printStackTrace(); }

            onUpdateSchedule.sendEmptyMessage(0);
        }
    };

    Handler onUpdateSchedule = new Handler(msg -> {
        Toast.makeText(MainActivity.this, "Расписание успешно обновлено!",
                Toast.LENGTH_SHORT).show();
        return true;
    });





}
