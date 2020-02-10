package com.onesignal.utils;

import java.util.Date;

public class CurrentDateGenerator implements DateGenerator {

    @Override
    public long getDateInSeconds() {
        return new Date().getTime() / 1000;
    }
}
