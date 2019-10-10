package com.example.schedule;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ScheduleApplication extends Application {

    public static final String scheduleFileName = "schedule.json";
    public static final String branchesUrl = "getBranches.php";
    public static final String serverKpfu = "kpfu";
    public static final String serverApp = "appServer";
    public static final String checkUpdateUrl = "checkUpdate.php";
    public static final String url = "https://schedule2171112.000webhostapp.com/";
    public static final List<String> dayOfWeek = Arrays.asList(
            "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота");


    private static final int CURRENT_SCHEDULE_VERSION = 1;
    private static final String SCHEDULE_VERSION_PREF = "schedule_version";

    @Override
    public void onCreate() {
        super.onCreate();

        checkScheduleVersion();
    }


    private void checkScheduleVersion() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int version = sharedPreferences.getInt(SCHEDULE_VERSION_PREF, -1);
        if (version == CURRENT_SCHEDULE_VERSION) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SCHEDULE_VERSION_PREF, CURRENT_SCHEDULE_VERSION);
        editor.commit();


        try {
            FileOutputStream outputStream = openFileOutput(scheduleFileName, MODE_PRIVATE);
            outputStream.write(new byte[0]);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static Handler toastHandler = new Handler(msg -> {
        Toast.makeText((Context) ((Object[]) msg.obj)[0], (String) ((Object[]) msg.obj)[1],
                Toast.LENGTH_SHORT).show();
        return true;
    });

    public static void showToast(Context ctx, String str) {
        Message msg = new Message();
        msg.obj = new Object[] {ctx, str};
        toastHandler.sendMessage(msg);
    }


}
