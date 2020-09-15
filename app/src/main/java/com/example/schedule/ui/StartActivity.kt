package com.example.schedule.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.schedule.R
import com.example.schedule.ScheduleApplication
import com.example.schedule.ScheduleApplication.Companion.CHOSE_FILE_REQUEST_CODE
import com.example.schedule.logic.ScheduleHelper.group
import com.example.schedule.logic.ScheduleHelper.saveGroup
import com.example.schedule.logic.ServerHelper
import com.example.schedule.logic.SheetsHelper.getSchedule
import com.example.schedule.model.Branch
import com.example.schedule.model.Course
import com.example.schedule.model.Schedule
import com.example.schedule.util.LoadDialog
import com.example.schedule.util.buildSubscribe
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_start.*
import java.io.InputStream

class StartActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var loadDialog: LoadDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ScheduleApplication.currentTheme)
        setContentView(R.layout.activity_start)

        loadDialog = LoadDialog(supportFragmentManager)

        ScheduleApplication.checkEvening(PreferenceManager.getDefaultSharedPreferences(this))

        val background = if (ScheduleApplication.isDarkTheme)
            R.drawable.choose_group_button_dark_bg
        else R.drawable.choose_group_button_light_bg
        btnDownload.setBackgroundResource(background)
        btnDownload.setOnClickListener {
            ServerHelper.getKfuBranches().subscribeToGetBranches()
        }
        btnChooseFromFile.setOnClickListener {
            chooseTableFromFile()
        }
        btnDownloadFromAppServer.setOnClickListener {
            ServerHelper.getServerBranches().subscribeToGetBranches()
        }
        btnSettings.setOnClickListener {
            startActivity(Intent(this, PreferencesActivity::class.java))
        }

        if (group != null) setResult(Activity.RESULT_OK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOSE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Observable.just(data)
                    .subscribeOn(Schedulers.io())
                    .flatMap { intent -> ObservableSource<InputStream> {
                        val fileUri = intent.data
                        if (fileUri == null) {
                            it.onError(Throwable("Не удалось определить путь до таблицы"))
                            return@ObservableSource
                        }
                        val inputStream = contentResolver.openInputStream(fileUri)
                        it.onNext(inputStream)
                        it.onComplete()
                    }}
                    .flatMap { inputStream -> ObservableSource<Schedule> {
                        val schedule = getSchedule(inputStream)
                        if (schedule != null) {
                            it.onNext(schedule)
                            it.onComplete()
                        } else it.onError(Throwable("Не удалось прочитать таблицу"))
                    }}
                    .observeOn(AndroidSchedulers.mainThread())
                    .buildSubscribe(
                            onSubscribe = { loadDialog.show("Чтение таблицы") },
                            onNext = { openCoursesDialog(it.courses) },
                            onComplete = { loadDialog.close() },
                            onError = {
                                it.printStackTrace()
                                loadDialog.close()
                                Toast.makeText(this@StartActivity, it.message, Toast.LENGTH_LONG).show()
                            })
                    .also { compositeDisposable.add(it) }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun Observable<List<Branch>>.subscribeToGetBranches(): Disposable {
        return this.observeOn(AndroidSchedulers.mainThread()).buildSubscribe(
                onSubscribe = { loadDialog.show("Загрузка списка групп") },
                onNext = { showBranchDialog(it) },
                onComplete = {loadDialog.close() },
                onError = {
                    loadDialog.close()
                    Toast.makeText(this@StartActivity, it.message, Toast.LENGTH_LONG).show()
                }
        ).also { compositeDisposable.add(it) }
    }

    private fun showBranchDialog(branches: List<Branch>) {
        AlertDialog.Builder(this)
                .setItems(branches.map{ it.name }.toTypedArray()) { _, pos ->
                    val value = branches[pos].value
                    if (value is String) {
                        ServerHelper.callForSchedule(value).subscribeToGetSchedule()
                    } else {
                        val hashMap = LinkedHashMap<String, Any>(value as Map<String, Any>)
                        showBranchDialog(hashMap.map { Branch(it.key, it.value) })
                    }
                }
                .show()
    }

    private fun Observable<Schedule>.subscribeToGetSchedule(): Disposable {
        return this.observeOn(AndroidSchedulers.mainThread()).buildSubscribe(
                onSubscribe = { loadDialog.show("Загрузка таблицы") },
                onNext = {
                    loadDialog.changeText("Чтение таблицы")
                    openCoursesDialog(it.courses)
                },
                onComplete = { loadDialog.close() },
                onError = {
                    it.printStackTrace()
                    loadDialog.close()
                    Toast.makeText(this@StartActivity, it.message, Toast.LENGTH_LONG).show()
                }
        )
    }

    private fun chooseTableFromFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, "Выберите таблицу"), CHOSE_FILE_REQUEST_CODE)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "Серьёзно? Установи файловый менеджер", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCoursesDialog(courses: List<Course>) {
        AlertDialog.Builder(this@StartActivity)
                .setItems(courses.map { it.name }.toTypedArray()) { _, pos ->
                    openGroupsDialog(courses[pos])
                }
                .show()
    }

    private fun openGroupsDialog(course: Course) {
        val sortedGroups = course.groups.sortedBy { it.name }
        AlertDialog.Builder(this@StartActivity)
                .setItems(sortedGroups.map { it.name }.toTypedArray()) { _, pos ->
                    saveGroup(sortedGroups[pos])
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .show()
    }
}