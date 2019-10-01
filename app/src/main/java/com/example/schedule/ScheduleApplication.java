package com.example.schedule;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class ScheduleApplication extends Application {

    public static final String scheduleFileName = "schedule.json";
    public static final String branchesUrl = "getBranches.php";
    public static final String checkUpdateUrl = "checkUpdate.php";
    public static final String url = "https://schedule2171112.000webhostapp.com/";
    public static final List<String> dayOfWeek = Arrays.asList(
            "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота");

    @Override
    public void onCreate() {
        super.onCreate();
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
