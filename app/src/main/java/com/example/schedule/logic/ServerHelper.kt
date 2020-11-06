package com.example.schedule.logic

import com.example.schedule.model.AppVersion
import com.example.schedule.model.Branch
import com.example.schedule.model.Schedule
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object ServerHelper {

    private const val branchesUrl = "getBranches.php"
    private const val serverKpfu = "kpfu"
    private const val serverApp = "appServer"
    private const val checkUpdateUrl = "checkUpdate.php"
    private const val getEveningUrl = "getEvening.php"
    private const val serverUrl = "https://schedule2171112.000webhostapp.com/"

    private const val kfuBranchesUrl = "$serverUrl$branchesUrl?server=$serverKpfu"
    private const val serverBranchesUrl = "$serverUrl$branchesUrl?server=$serverApp"


    private val simpleGson: Gson = Gson()
    private val client = OkHttpClient()

    /*fun justCall(url: String, callback: Callback) {
        val request = Request.Builder()
                .url(url)
                .build()
        client.newCall(request).enqueue(callback)
    }*/

    fun getKfuBranches(): Observable<List<Branch>> = callForBranches(kfuBranchesUrl)

    fun getServerBranches(): Observable<List<Branch>> = callForBranches(serverBranchesUrl)

    fun checkEvening(): Observable<Boolean> = callForString(serverUrl + getEveningUrl)
            .flatMap { Observable.just(it == "1") }


    private fun call(url: String): Observable<Response> {
        val request = Request.Builder()
                .url(url)
                .build()

        val observable: Observable<Response> = Observable.defer<Response> {
            try {
                val response: Response = client.newCall(request).execute()
                Observable.just(response)
            } catch (e: Exception) {
                Observable.error(e)
            }
        }
        return observable
                .subscribeOn(Schedulers.io())
    }

    private fun <T> getFromResponse(url: String, castFunction: (response: Response) -> T): Observable<T> {
        return call(url).flatMap { response -> ObservableSource<T> {
            try {
                val item = castFunction(response)
                it.onNext(item)
                it.onComplete()
            } catch (t: Throwable) {
                it.onError(t)
            }
            return@ObservableSource
        }}
    }

    private inline fun <reified T> getObject(url: String): Observable<T> {
        return getFromResponse(url) {
            try {
                val responseBody = it.body()
                        ?: throw NullPointerException("Тело ответа сервера равно null")
                val str = responseBody.string()
                return@getFromResponse simpleGson.fromJson<T>(str, T::class.java)
            } catch (e: IOException) {
                throw Exception("Произошла ошибка при чтении ответа сервера")
            }
        }
    }

    private fun callForString(url: String): Observable<String> {
        return getFromResponse(url) {
            val responseBody = it.body() ?: throw Exception("Пустое тело ответа")
            return@getFromResponse responseBody.string()
        }
    }

    private fun callForBranches(url: String): Observable<List<Branch>> {
        return callForString(url)
                .flatMap { str -> ObservableSource<List<Branch>> {
                    try {
                        val hashMap = simpleGson.fromJson<LinkedHashMap<String, Any>>(str, LinkedHashMap<String, Any>()::class.java)

                        val branches = hashMap.map { entry -> Branch(entry.key, entry.value) }
                        it.onNext(branches)
                        it.onComplete()
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                        it.onError(Throwable("Произошла ошибка при чтении расписаний отделений"))
                    }
                }}
    }

    fun callForSchedule(url: String): Observable<Schedule> {
        return getFromResponse(url) {
            val responseBody = it.body()
                    ?: throw NullPointerException("Тело ответа серверо равно null")
            return@getFromResponse SheetsHelper.getSchedule(responseBody.byteStream())
                    ?: throw NullPointerException("Не удалось прочитать таблицу с расписанием")
        }
    }

    @JvmStatic
    fun checkUpdates(): Observable<AppVersion> {
        return getObject<List<String>>(serverUrl + checkUpdateUrl)
                .map { AppVersion(it[0], it[1]) }
    }



}