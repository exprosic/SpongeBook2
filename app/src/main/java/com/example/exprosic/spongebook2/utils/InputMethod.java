package com.example.exprosic.spongebook2.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by exprosic on 4/16/2016.
 */
public class InputMethod {
    public static final String TAG = InputMethod.class.getSimpleName();

    public static void show(View view) {
        InputMethodManager manager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hide(View view) {
        Log.d(TAG, "hiding input method");
        InputMethodManager manager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
