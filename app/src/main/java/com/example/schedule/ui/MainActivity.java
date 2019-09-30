package com.example.schedule.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.schedule.BuildConfig;
import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Для всех fff

    // Calendars for navigationTitle and viewPager
    public static Calendar currentDate;
    public static Calendar pageDate;

    // Shared preferences
    static SharedPreferences preferences;
    public static boolean weekEvenStyle; // false: В-Н; true: Ч-Н
    public static boolean showNavigationLayout; // false: hide, true: show

    // Constants
    public static final String scheduleFileName = "schedule.json";
    public static final String branchesUrl = "getBranches.php";
    public static final String checkUpdateUrl = "checkUpdate.php";
    public static final String updateUrl = "update.php";
    public static final String url = "https://schedule2171112.000webhostapp.com/";
    public static final List<String> dayOfWeek = Arrays.asList(
            "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота");

    public static final int startActivityRequestCode = 2;


    // UI
    private ViewPager2 viewPager;
    private AlertDialog changeScheduleDialog;
    private DatePickerDialog dataChoiceDialog;

    // UI for navigation layout
    private LinearLayout navigationLayout;
    private TextView navigationTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ser prefs
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set dates
        currentDate = new GregorianCalendar();
        pageDate = new GregorianCalendar();

        // Set UI
        viewPager = findViewById(R.id.viewPager);
        navigationLayout = findViewById(R.id.navigationLayout);
        navigationTitle = findViewById(R.id.twTitle);


        loadSchedule();
    }

    private void loadSchedule() {
        try {
            ScheduleHelper.loadSchedule(openFileInput(scheduleFileName));
            loadActivity();
        } catch (IOException e) {
            onFileNotAvailable();
        }
    }

    private void onFileNotAvailable() {
        startActivityForResult(new Intent(this, StartActivity.class), startActivityRequestCode);
    }

    private void loadActivity() {

        // Set viewPager
        viewPager.setAdapter(new DayAdapter());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                pageDate = (Calendar) currentDate.clone();
                pageDate.add(Calendar.DATE, position - DayAdapter.middlePos);
                if (showNavigationLayout)
                    navigationTitle.setText(dayOfWeek.get(pageDate.get(Calendar.DAY_OF_WEEK) - 1));
                else setTitle(dayOfWeek.get(pageDate.get(Calendar.DAY_OF_WEEK) - 1));
            }
        });
        viewPager.setCurrentItem(DayAdapter.middlePos, false);


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
            Calendar newPageDate = (Calendar) currentDate.clone();
            newPageDate.set(year, month, dayOfMonth);
            int daysBetween = daysBetween(newPageDate.getTime(), currentDate.getTime());
            viewPager.setCurrentItem(DayAdapter.middlePos - daysBetween);
            pageDate.set(year, month, dayOfMonth);
        };
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);
        dataChoiceDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        dataChoiceDialog.setTitle("Выберите дату");


        // Set navigation layout
        ImageButton btnLeft = findViewById(R.id.btnLeft);
        ImageButton btnRight = findViewById(R.id.btnRight);
        btnLeft.setOnClickListener(btn -> viewPager.setCurrentItem(viewPager.getCurrentItem() - 1));
        btnRight.setOnClickListener(btn -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));


    }

    @Override
    protected void onResume() {
        super.onResume();

        weekEvenStyle = preferences.getString("week_even_style", "0").equals("0");
        showNavigationLayout = preferences.getBoolean("show_navigation_layout", false);
        navigationLayout.setVisibility(showNavigationLayout ? View.VISIBLE : View.GONE);

        String day = dayOfWeek.get(pageDate.get(Calendar.DAY_OF_WEEK) - 1);
        if (showNavigationLayout) {
            setTitle(R.string.app_name);
            navigationTitle.setText(day);
        } else setTitle(day);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if (requestCode == 0 && resultCode == 1) ScheduleHelper.downloadSchedule(downloadCallback);
        if (requestCode == startActivityRequestCode) {
            if (resultCode == RESULT_OK) loadSchedule();
            if (resultCode == RESULT_CANCELED) finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.typeWeeks).setIntent(
                new Intent(this, FullScheduleActivity.class));
        menu.findItem(R.id.preferences).setIntent(
                new Intent(this, PreferencesActivity.class));
        menu.findItem(R.id.calendar).setOnMenuItemClickListener(item -> {
            dataChoiceDialog.show();
            return true;
        });
        menu.findItem(R.id.reloadSchedule).setOnMenuItemClickListener(item -> {
            //ScheduleHelper.downloadSchedule(downloadCallback);
            startActivityForResult(new Intent(this, StartActivity.class), startActivityRequestCode);
            return true;
        });
        menu.findItem(R.id.changeSchedule).setOnMenuItemClickListener(item -> {
            changeScheduleDialog.show();
            return true;
        });


        return true;
    }

    /*private void setAdapter() {
        //dayAdapter.notifyDataSetChanged();


        viewPager.setAdapter(new DayAdapter());
        int leftDays = daysBetween(pageDate.getTime(), currentDate.getTime());
        int currentStartPage = DayAdapter.middlePos - leftDays;
        viewPager.setCurrentItem(currentStartPage, false);


        if (showNavigationLayout) navigationTitle.setText(dayOfWeek.get(pageDate.get(Calendar.DAY_OF_WEEK) - 1));
        else setTitle(dayOfWeek.get(pageDate.get(Calendar.DAY_OF_WEEK) - 1));
    }*/

    public static int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }







    /*Callback downloadCallback = new Callback() {

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

            try { ScheduleHelper.saveSchedule(json, openFileOutput(scheduleFileName, MODE_PRIVATE)); }
            catch (FileNotFoundException e) { e.printStackTrace(); }

            onUpdateSchedule.sendEmptyMessage(0);
        }
    };*/

}
