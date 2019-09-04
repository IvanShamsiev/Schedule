package com.example.schedule.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;
import com.example.schedule.model.Schedule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // Main schedule
    public static Schedule schedule;

    // Calendars for navigationTitle and viewPager
    public static Calendar currentDate;
    public static Calendar pageDate;

    // Shared preferences
    static SharedPreferences preferences;
    public static boolean lessonNames; // false: short; true: full
    public static boolean weekEvenStyle; // false: В-Н; true: Ч-Н
    public static boolean showNavigationLayout; // false: hide, true: show
    private static boolean prefsUpdate = false; // for update on change prefs

    // Constants
    public static final String scheduleFileName = "schedule.json";
    public static final String url = "https://schedule2171112.000webhostapp.com/";
    public static final String[] months = {"Января", "Февраля", "Марта", "Апреля", "Мая", "Июня", "Июля",
            "Августа", "Сентября", "Октября", "Ноября", "Декабря"};
    public static final String[] dayOfWeek = {"Воскресенье", "Поненельник", "Вторник", "Среда",
            "Четверг", "Пятница", "Суббота"};

    // UI
    public ViewPager viewPager;
    private AlertDialog changeScheduleDialog;
    private DatePickerDialog dataChoiceDialog;

    // UI for navigation layout
    private LinearLayout navigationLayout;
    private TextView navigationTitle;


    public static int currentPos = 1;
    private static boolean pageSelectedFromMenu = false;



    TextView textDate, textEvenWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






        textDate = findViewById(R.id.textDate);
        textEvenWeek = findViewById(R.id.textEvenWeek);



        // Ser prefs
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> prefsUpdate = true);

        // Set dates
        currentDate = new GregorianCalendar();
        pageDate = new GregorianCalendar();

        // Set viewPager
        viewPager = findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int page) {
                if (pageSelectedFromMenu) {
                    currentPos = page;
                    pageSelectedFromMenu = false;
                    return;
                }

                if (page - currentPos == 1) pageDate.add(Calendar.DATE, 1);
                if (page - currentPos == -1) pageDate.add(Calendar.DATE, -1);
                currentPos = page;


                if (showNavigationLayout) navigationTitle.setText(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK) - 1]);
                else setTitle(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK) - 1]);
                textDate.setText(String.format(Locale.getDefault(), "%d %s",
                        pageDate.get(Calendar.DAY_OF_MONTH), months[currentDate.get(Calendar.MONTH)]));
                String upWeek = weekEvenStyle ? "Верхняя неделя" : "Нечётная неделя";
                String downWeek = weekEvenStyle ? "Нижняя неделя" : "Чётная неделя";
                textEvenWeek.setText((ScheduleHelper.isEven(pageDate) ? downWeek : upWeek));

                /*pageDate = (Calendar) currentDate.clone();
                pageDate.add(Calendar.DATE, page - DayFragment.middlePos);
                currentPos = page;
                if (showNavigationLayout) navigationTitle.setText(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK) - 1]);
                else setTitle(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK) - 1]);*/
            }
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }
            @Override public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int current = viewPager.getCurrentItem();
                    if (current == DayFragment.pagesCount - 1) current = 1;
                    else if (current == 0) current = DayFragment.pagesCount - 2;
                    viewPager.setCurrentItem(current, false);
                }
            }
        });

        // Set dialog for change schedule
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


        // Set dialog for peek date for schedule
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            pageSelectedFromMenu = true;

            pageDate.set(year, month, dayOfMonth);

            if (showNavigationLayout) navigationTitle.setText(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK) - 1]);
            else setTitle(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK) - 1]);
            textDate.setText(String.format(Locale.getDefault(), "%d %s",
                    pageDate.get(Calendar.DAY_OF_MONTH), months[currentDate.get(Calendar.MONTH)]));

            int leftDays = daysBetween(currentDate.getTime(), pageDate.getTime());
            int page = (leftDays + 1) % 14;
            if (page <= 0) page += 14;
            viewPager.setCurrentItem(page, false);
        };
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);
        dataChoiceDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        dataChoiceDialog.setTitle("Выберите дату");



        // Set navigation layout
        navigationLayout = findViewById(R.id.navigationLayout);
        navigationTitle = findViewById(R.id.twTitle);
        ImageButton btnLeft = findViewById(R.id.btnLeft);
        ImageButton btnRight = findViewById(R.id.btnRight);
        btnLeft.setOnClickListener(btn -> viewPager.setCurrentItem(currentPos - 1));
        btnRight.setOnClickListener(btn -> viewPager.setCurrentItem(currentPos + 1));



        // Update schedule state
        updateScheduleState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefsUpdate) {
            lessonNames = preferences.getBoolean("full_lesson_names", false);
            weekEvenStyle = preferences.getString("week_even_style", "0").equals("0");
            showNavigationLayout = preferences.getBoolean("show_navigation_layout", false);
            navigationLayout.setVisibility(showNavigationLayout ? View.VISIBLE : View.GONE);
            if (showNavigationLayout) setTitle(R.string.app_name);

            schedule = ScheduleHelper.getInstance(lessonNames);
            setAdapter();

            prefsUpdate = false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0 && resultCode == 1) ScheduleHelper.downloadSchedule(downloadCallback);
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
            ScheduleHelper.downloadSchedule(downloadCallback);
            return true;
        });
        menu.findItem(R.id.changeSchedule).setOnMenuItemClickListener(item -> {
            changeScheduleDialog.show();
            return true;
        });
        return true;
    }



    private void updateScheduleState() {
        lessonNames = preferences.getBoolean("full_lesson_names", false);
        weekEvenStyle = preferences.getString("week_even_style", "0").equals("0");
        showNavigationLayout = preferences.getBoolean("show_navigation_layout", false);
        navigationLayout.setVisibility(showNavigationLayout ? View.VISIBLE : View.GONE);

        try { ScheduleHelper.loadSchedule(openFileInput(scheduleFileName), downloadCallback); }
        catch (FileNotFoundException e) { ScheduleHelper.downloadSchedule(downloadCallback); }

        schedule = ScheduleHelper.getInstance(lessonNames);

        setAdapter();
    }

    private void setAdapter() {
        viewPager.removeAllViews();
        viewPager.setAdapter(new DayFragment.DaysPagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(1);

        /*int leftDays = daysBetween(pageDate.getTime(), currentDate.getTime());
        int currentStartPage = DayFragment.middlePos - leftDays;
        viewPager.setCurrentItem(currentStartPage);*/



        if (showNavigationLayout) navigationTitle.setText(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK) - 1]);
        else setTitle(dayOfWeek[pageDate.get(Calendar.DAY_OF_WEEK) - 1]);
    }

    public static int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }







    Callback downloadCallback = new Callback() {

        Handler onFailure = new Handler(msg -> {
            Toast.makeText(MainActivity.this, "Не удалось загрузить расписание",
                    Toast.LENGTH_SHORT).show();
            return true;
        });

        Handler onUpdateSchedule = new Handler(msg -> {
            updateScheduleState();
            Toast.makeText(MainActivity.this, "Расписание успешно обновлено!",
                    Toast.LENGTH_SHORT).show();
            return true;
        });

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            onFailure.sendEmptyMessage(0);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            String json;
            try {json = response.body().string();}
            catch (IOException | NullPointerException e) {e.printStackTrace(); return;}

            try { ScheduleHelper.saveSchedule(json, openFileOutput(scheduleFileName , MODE_PRIVATE)); }
            catch (FileNotFoundException e) { e.printStackTrace(); }

            onUpdateSchedule.sendEmptyMessage(0);
        }
    };

}
