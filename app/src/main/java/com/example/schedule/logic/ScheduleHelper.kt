package com.example.schedule.logic

import android.content.Context
import com.example.schedule.ScheduleApplication.GROUP_FILE
import com.example.schedule.model.Group
import com.google.gson.Gson
import java.io.*
import java.util.*

object ScheduleHelper {

    var context: Context? = null
        set(value) { field = value?.applicationContext }

    private val simpleGson = Gson()

    var group: Group? = null

    fun loadGroup(context: Context): Group? {
        var json: String? = null
        try {
            val br = BufferedReader(InputStreamReader(context.openFileInput(GROUP_FILE)))

            val jsonBuilder = StringBuilder()
            var s: String?
            while (br.readLine().also { s = it } != null) jsonBuilder.append(s)
            br.close()

            json = jsonBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        println("Json: ${json == null} $json")

        return if (json != null && json.isNotEmpty()) {
            simpleGson.fromJson(json, Group::class.java)
        } else {
            null
        }
    }

    fun loadGroup(): Boolean {
        val group = loadGroup(context!!)
        return if (group != null) {
            this.group = group
            true
        } else false
    }

    private fun setSchedule(json: String) {
        group = simpleGson.fromJson(json, Group::class.java)
    }

    fun saveGroup(group: Group): Boolean {
        return try {
            val jsonGroup: String = simpleGson.toJson(group)
            val bw = BufferedWriter(OutputStreamWriter(context!!.openFileOutput(GROUP_FILE, Context.MODE_PRIVATE)))
            bw.write(jsonGroup)
            bw.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun isEven(date: Calendar): Boolean { // true - Нижняя, false - Верхняя
        return date[Calendar.WEEK_OF_YEAR] % 2 == 0
    }
}
