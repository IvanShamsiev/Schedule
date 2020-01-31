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

    var group: Group? = null

    private fun setSchedule(json: String) {
        group = Gson().fromJson(json, Group::class.java)
    }

    fun loadGroup(): Boolean {
        var json: String? = null
        try {
            val br = BufferedReader(InputStreamReader(context!!.openFileInput(GROUP_FILE)))

            val jsonBuilder = StringBuilder()
            var s: String?
            while (br.readLine().also { s = it } != null) jsonBuilder.append(s)
            br.close()

            json = jsonBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return if (json != null && json.isNotEmpty()) {
            setSchedule(json)
            true
        } else {
            false
        }
    }

    fun saveGroup(group: Group): Boolean {
        return try {
            val jsonGroup: String = Gson().toJson(group)
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
