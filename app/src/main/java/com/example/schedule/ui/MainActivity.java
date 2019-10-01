package com.example.schedule.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.schedule.R;
import com.example.schedule.logic.ScheduleHelper;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.example.schedule.ScheduleApplication.dayOfWeek;
import static com.example.schedule.ScheduleApplication.scheduleFileName;

public class MainActivity extends AppCompatActivity {

    // Calendars for navigationTitle and viewPager
    public static Calendar currentDate;
    public static Calendar pageDate;

    // Shared preferences
    static SharedPreferences preferences;
    public static boolean weekEvenStyle; // false: В-Н; true: Ч-Н
    public static boolean showNavigationLayout; // false: hide, true: show

    // Request code for StartActivity
    public static final int startActivityRequestCode = 2;

    // UI
    private ViewPager2 viewPager;
    private DatePickerDialog dataChoiceDialog;

    // UI for navigation layout
    private LinearLayout navigationLayout;
    private TextView navigationTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ser preferences
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


        return true;
    }




    public static int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

}
