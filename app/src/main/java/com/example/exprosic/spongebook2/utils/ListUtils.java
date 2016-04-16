package com.example.exprosic.spongebook2.utils;

import java.util.List;

/**
 * Created by exprosic on 4/17/2016.
 */
public class ListUtils {
    public static <T> String concatenate(List<T> list) {
        return concatenate(list, " ");
    }

    public static <T> String concatenate(List<T> list, String delimiter) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (T item: list) {
            if (isFirst)
                builder.append(delimiter);
            isFirst = false;
            builder.append(item.toString());
        }
        return builder.toString();
    }
}
