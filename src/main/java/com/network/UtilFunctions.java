package com.network;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilFunctions {
    public static String getTimestamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
}
