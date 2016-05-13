package com.example.exprosic.spongebook2.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by exprosic on 4/17/2016.
 */
public class StringUtils {
    @NonNull
    public static String nullToEmpty(String string) {
        return string==null ?"" :string;
    }

    @Nullable
    public static String emptyToNull(String string) {
        return string.equals("") ?null :string;
    }

    public static String[] toStrings(Object... args) {
        String[] strArgs = new String[args.length];
        for (int i=0; i<args.length; ++i)
            strArgs[i] = args[i].toString();
        return strArgs;
    }
}
