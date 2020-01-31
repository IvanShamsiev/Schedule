package com.example.schedule.ui

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.BuildConfig
import com.example.schedule.R
import com.example.schedule.ScheduleApplication
import kotlinx.android.synthetic.main.activity_about_app.*
import kotlinx.android.synthetic.main.item_about_app.view.*

class AboutAppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ScheduleApplication.currentTheme)
        setContentView(R.layout.activity_about_app)
        title = "О приложении"

        val lines = listOf(
                "Название приложения: ${getString(R.string.app_name)}",
                "Версия приложения: ${BuildConfig.VERSION_NAME}",
                """Наша группа в ВК: <a href="${getString(R.string.vk_href)}">${getString(R.string.vk_string)}</a>"""
        )

        val data = lines.map { mapOf("aboutAppField" to it) }
        val from = arrayOf("aboutAppField")
        val to = intArrayOf(R.id.textView)

        listView.adapter = object: SimpleAdapter(this, data, R.layout.item_about_app, from, to) {
            override fun setViewText(v: TextView?, text: String?) {
                v?.text = Html.fromHtml(text)
                v?.movementMethod = LinkMovementMethod.getInstance() // Чтобы можно было нажать как на ссылку
            }
        }

        //recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        //recyclerView.adapter = AboutAppAdapter()
    }
}