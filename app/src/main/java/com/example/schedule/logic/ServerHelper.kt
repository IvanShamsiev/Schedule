package com.example.schedule.logic

import com.example.schedule.model.Branch
import com.example.schedule.model.Schedule
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.schedulers.Schedulers
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object ServerHelper {

    private const val branchesUrl = "getBranches.php"
    private const val serverKpfu = "kpfu"
    private const val serverApp = "appServer"
    const val checkUpdateUrl = "checkUpdate.php"
    const val serverUrl = "https://schedule2171112.000webhostapp.com/"

    private const val kfuBranchesUrl = "$serverUrl$branchesUrl?server=$serverKpfu"
    private const val serverBranchesUrl = "$serverUrl$branchesUrl?server=$serverApp"


    private val client = OkHttpClient()

    @JvmStatic
    fun justCall(url: String, callback: Callback) {
        val request = Request.Builder()
                .url(url)
                .build()
        client.newCall(request).enqueue(callback)
    }

    fun getKfuBranches(): Observable<List<Branch>> = callForBranches(kfuBranchesUrl)

    fun getServerBranches(): Observable<List<Branch>> = callForBranches(serverBranchesUrl)

    private fun call(url: String): Observable<Response> {
        val request = Request.Builder()
                .url(url)
                .build()

        val observable: Observable<Response> = Observable.defer<Response> {
            try {
                val response: Response = client.newCall(request).execute()
                Observable.just(response)
            } catch (e: IOException) {
                Observable.error(e)
            }
        }
        return observable
                .subscribeOn(Schedulers.io())
    }

    private fun callForString(url: String): Observable<String> {
        return call(url).flatMap { response ->
            ObservableSource<String> {
                try {
                    val responseBody = response.body()
                            ?: throw NullPointerException("Тело ответа сервера равно null")
                    val str = responseBody.string()
                    it.onNext(str)
                    it.onComplete()
                    return@ObservableSource
                } catch (e: IOException) {
                    it.onError(Throwable("Произошла ошибка при чтении ответа сервера"))
                    return@ObservableSource
                } catch (e: NullPointerException) {
                    it.onError(e)
                    return@ObservableSource
                }
            }
        }
    }

    private fun callForBranches(url: String): Observable<List<Branch>> {
        return callForString(url)
                .subscribeOn(Schedulers.computation())
                .flatMap { str ->
            ObservableSource<List<Branch>> {
                try {
                    val hashMapType = object : TypeToken<LinkedHashMap<String, Any>>() {}.type
                    val hashMap = Gson().fromJson<LinkedHashMap<String, Any>>(str, hashMapType)

                    val branches = hashMap.map { entry -> Branch(entry.key, entry.value) }
                    it.onNext(branches)
                    it.onComplete()
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    it.onError(Throwable("Произошла ошибка при чтении расписаний отделений"))
                }
            }
        }
    }

    fun callForSchedule(url: String): Observable<Schedule> {
        return call(url).flatMap { response ->
            ObservableSource<Schedule> {
                try {
                    val responseBody = response.body()
                            ?: throw NullPointerException("Тело ответа серверо равно null")
                    val schedule = SheetsHelper.getSchedule(responseBody.byteStream())
                            ?: throw NullPointerException("Не удалось прочитать таблицу с расписанием")
                    it.onNext(schedule)
                    it.onComplete()
                } catch (e: NullPointerException) {
                    it.onError(e)
                    e.printStackTrace()
                }
            }
        }
    }



}