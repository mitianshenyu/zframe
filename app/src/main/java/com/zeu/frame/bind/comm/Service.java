package com.zeu.frame.bind.comm;

import android.content.Intent;
import android.os.IBinder;

import com.zeu.frame.bind.Binders;

/**
 * Created by zeu on 2016/11/29.
 */

public abstract class Service extends android.app.Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return Binders.getContainer();
    }
}
