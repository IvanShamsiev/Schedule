package com.example.schedule.util

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.schedule.R

class LoadDialog(private val fragmentManager: FragmentManager) {

    private val loadDialog: LoadDialogFragment = LoadDialogFragment()

    private val handler = Handler()

    fun show(text: String?) {
        loadDialog.text = text
        loadDialog.show(fragmentManager, "loadDialog")
    }

    fun changeText(text: String?) {
        handler.post {
            if (loadDialog.dialog != null)
                loadDialog.dialog!!.findViewById<TextView>(R.id.textView).text = text
        }
    }

    fun close() {
        loadDialog.dismiss()
    }

    class LoadDialogFragment : DialogFragment() {

        var text: String? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.dialog_load, container, false)
            view.findViewById<TextView>(R.id.textView).text = text
            return view
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = Dialog(context!!, theme)
            dialog.setTitle("Пожалуйста, подождите")
            return dialog
        }
    }
}