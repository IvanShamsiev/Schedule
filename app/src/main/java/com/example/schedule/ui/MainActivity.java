package com.example.schedule.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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
import java.util.Date;
import java.util.GregorianCalendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static Schedule schedule;

    public static Calendar currentDate;
    public static Calendar pageDate;

    static SharedPreferences preferences;
    static boolean lessonNames; // false: short; true: full
    public static boolean weekEvenStyle; // false: В-Н; true: Ч-Н

    public static final String scheduleFileName = "schedule.json";
    public static final String url = "https://schedule2171112.000webhostapp.com/";
    public static final String[] months = {"Января", "Февраля", "Марта", "Апреля", "Мая", "Июня", "Июля",
            "Августа", "Сентября", "Октября", "Ноября", "Декабря"};

    public static final String[] dayOfWeek = {"Воскресенье", "Поненельник", "Вторник", "Среда",
            "Четверг", "Пятница", "Суббота"};

    private ViewPager viewPager;
    private AlertDialog changeScheduleDialog;
    private DatePickerDialog dataChoiceDialog;

    //boolean currentConfigChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        currentDate = new GregorianCalendar();
        pageDate = new GregorianCalendar();

        viewPager = findViewById(R.id.viewPager);

        changeScheduleDialog = new AlertDialog.Builder(this)
                .setTitle("Введите пароль")
                .setView(getLayoutInflater().inflate(R.layout.change_schedule_dialog, null))
                .setPositiveButton("Ок", (dialog, which) -> {
                    EditText editTextPass = ((AlertDialog) dialog).findViewById(R.id.editTextPass);
                    String pass = editTextPass.getText().toString();
                    if (pass.equals("02281488")) {
                        Intent changeSchedule = new Intent(this, ScheduleChanger.class);
                        startActivityForResult(changeSchedule, 0);
                    } else Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.cancel())
                .create();


        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            pageDate.set(year, month, dayOfMonth);
            setAdapter();
        };
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);
        dataChoiceDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        dataChoiceDialog.setTitle("Выберите дату");
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
        menu.findItem(R.id.calendar).setOnMenuItemClickListener(item -> {
            dataChoiceDialog.show();
            return true;
        });
        menu.findItem(R.id.reloadSchedule).setOnMenuItemClickListener(item -> {
            ScheduleHelper.downloadSchedule(callback);
            return true;
        });
        menu.findItem(R.id.changeSchedule).setOnMenuItemClickListener(item -> {
            changeScheduleDialog.show();
            return true;
        });
        return true;
    }



    private void setAdapter() {
        setAdapter(new DayFragment.DaysPagerAdapter(getSupportFragmentManager()));
    }

    private void setAdapter(DayFragment.DaysPagerAdapter adapter) {
        viewPager.removeAllViews();
        viewPager.setAdapter(adapter);
        int leftDays = daysBetween(pageDate.getTime(), currentDate.getTime());
        int currentStartPage = DayFragment.middlePos - leftDays;
        viewPager.setCurrentItem(currentStartPage);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int page) {
                pageDate = (Calendar) currentDate.clone();
                pageDate.add(Calendar.DATE, page - DayFragment.middlePos);
                setTitle(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK) - 1]);
            }
            @Override public void onPageScrolled(int i, float v, int i1) { }
            @Override public void onPageScrollStateChanged(int i) { }
        });

        setTitle(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK)-1]);
    }

    public static int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }







    Callback callback = new Callback() {

        Handler setAdapterHandler = new Handler(msg -> {
            setAdapter();
            return true;
        });

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
