package com.example.exprosic.spongebook2.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * Created by exprosic on 4/17/2016.
 */
public class Debugging {
    public static void makeRawToast(Context context, int duration, String msg) {
        Toast.makeText(context, msg, duration).show();
    }

    public static void makeToast(Context context, int duration, String format, Object[] args) {
        Toast.makeText(context, String.format(Locale.CHINESE, format, args), duration).show();
    }

    public static void myAssert(boolean assertion, String msg) {
        if (!assertion)
            throw new AssertionError(msg);
    }

    public static void setLayoutParam(View view, String fieldName, Object value) {
        Class paramClass = view.getLayoutParams().getClass();
        try {
            Field field = paramClass.getField(fieldName);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            field.set(params, value);
            view.setLayoutParams(params);
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }
}
