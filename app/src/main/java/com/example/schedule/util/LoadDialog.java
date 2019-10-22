package com.example.schedule.util;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.schedule.R;

public class LoadDialog {

    private LoadDialogFragment loadDialog;
    private FragmentManager fragmentManager;

    public LoadDialog(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        loadDialog = new LoadDialogFragment();
    }

    public void show(String text) {
        if (loadDialog == null) return;
        loadDialog.setText(text);
        loadDialog.show(fragmentManager, "loadDialog");
    }

    private Handler changeHandler = new Handler(msg -> {
        if (loadDialog.getDialog() != null)
            ((TextView) loadDialog.getDialog().findViewById(R.id.textView)).setText((String) msg.obj);
        return true;
    });

    public void changeText(String text) {
        changeHandler.sendMessage(changeHandler.obtainMessage(0, text));
    }

    public void close() {
        if (loadDialog != null) loadDialog.dismiss();
    }

    public static class LoadDialogFragment extends DialogFragment {

        String text;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_load, container, false);
            ((TextView) view.findViewById(R.id.textView)).setText(text);
            return view;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}