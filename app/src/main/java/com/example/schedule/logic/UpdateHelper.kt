package com.example.schedule.logic

import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import com.example.schedule.BuildConfig
import com.example.schedule.ScheduleApplication
import com.example.schedule.util.LoadDialog
import com.example.schedule.util.buildSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import java.io.File

class UpdateHelper(private val context: Context, private val fragmentManager: FragmentManager) {

    private lateinit var loadDialog: LoadDialog

    fun checkUpdate() {
        loadDialog = LoadDialog(fragmentManager)
        ServerHelper.checkUpdates()
                .observeOn(AndroidSchedulers.mainThread())
                .buildSubscribe(
                        onSubscribe = { loadDialog.show("Проверка обновлений") },
                        onNext = {
                            if (it.version == BuildConfig.VERSION_NAME) {
                                Toast.makeText(context, "У вас установлена последняя версия приложения", Toast.LENGTH_LONG).show()
                            } else openUpdateDialog(it.version, it.url)
                        },
                        onComplete = { loadDialog.close() },
                        onError = {
                            loadDialog.close()
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }
                )
    }

    private fun openUpdateDialog(newVersion: String, newVersionUrl: String) {
        AlertDialog.Builder(context)
                .setMessage("Текущая версия: " + BuildConfig.VERSION_NAME + "\n" + "Новая версия: " + newVersion)
                .setPositiveButton("Обновить") { _, _ ->
                    update(context, newVersionUrl)
                    Toast.makeText(context, "Скачивание обновления", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Отмена", null)
                .show()
    }

    private fun update(context: Context, url: String) { // Set path for file

        val destination = context.getExternalFilesDir("update").toString() + "/"
        val fileName = "ScheduleUpdate.apk"
        val filePath = destination + fileName

        // Check if file already exists
        val file = File(filePath)
        if (file.exists()) file.delete()

        // Set Download Manager request
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle("ScheduleUpdate.apk")
        request.setDescription("Скачивание обновления")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationUri(Uri.fromFile(file))

        // Get download service and enqueue file
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        if (manager == null) {
            Toast.makeText(context, "Ошибка: На вашем устройстве отсутсвует менеджер загрузок", Toast.LENGTH_LONG).show()
            return
        }
        //val downloadId = manager.enqueue(request)

        // Set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                val intentInstall = Intent(Intent.ACTION_VIEW)
                val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
                intentInstall.setDataAndType(uri, "application/vnd.android.package-archive")
                intentInstall.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

                if (context.packageManager.resolveActivity(intentInstall, PackageManager.MATCH_DEFAULT_ONLY) == null) {
                    Toast.makeText(context, "Ошибка: на устройстве отсутсвует менеджер установки приложений", Toast.LENGTH_LONG).show()
                    return
                }

                context.startActivity(intentInstall)
                context.unregisterReceiver(this)
                onDestroy()
            }
        }

        // Register receiver for when .apk download is compete
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun onDestroy() {

    }
}