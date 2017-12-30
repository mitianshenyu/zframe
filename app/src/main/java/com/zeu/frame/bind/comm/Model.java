package com.zeu.frame.bind.comm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.zeu.frame.bind.Binders;
import com.zeu.frame.bind.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeu on 2017/6/29.
 */

public class Model {
    protected List<Data> mPackets;
    protected String mModuleName;
    public Model() {

    }

    public Model(String module) {
        mModuleName = module;
        if (null != mModuleName) {
            attach(mModuleName);
        }
    }

    /**
     * 连接上数据仓库服务
     * @param context
     * @param packageName
     * @param className
     * @param containerServiceReadyAction
     */

    public void bindRemote(Context context, String packageName, String className, String containerServiceReadyAction) {
        Binders.attach(context, packageName, className, containerServiceReadyAction);
    }

    public void bindRemote(Context context, ComponentName componentName, String containerServiceReadyAction) {
        Binders.attach(context, componentName, containerServiceReadyAction);
    }

    public void bindRemote(Context context, Intent serviceIntent, String containerServiceReadyAction) {
        Binders.attach(context, serviceIntent, containerServiceReadyAction);
    }

    public void unbindRemote() {
        Binders.detach(null==mModuleName?"":mModuleName);
    }

    protected List<Data> checkPackets() {
        List<Data> packets = new ArrayList<>();
        Field[] fields = getClass().getFields();
        for (Field field : fields) {
            if (null != field) {
                try {
                    Object obj = field.get(this);
                    if (obj instanceof Data) {
                        packets.add((Data) obj);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (null != field) {
                try {
                    field.setAccessible(true);
                    Object obj = field.get(this);
                    if (obj instanceof Data) {
                        packets.add((Data) obj);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return packets;
    }

    public List<Data> getPackets() {
        if (null == mPackets) {
            mPackets = checkPackets();
        }
        return mPackets;
    }

    public void attach(String module, Handler handler) {
        for (Data data : getPackets()) {
            if (null != data) {
                data.attach(module, handler);
            }
        }
    }

    public void attach(String module) {
        attach(module, null);
    }

    public void attach(Handler handler) {
        attach(null, handler);
    }

    public void attach() {
        attach(null, null);
    }

    /**
     * 清除所有的监听, 以防止内存泄漏
     * @param clearAllObservers
     */
    public void detach(boolean clearAllObservers) {
        for (Data data : getPackets()) {
            if (null != data) {
                data.detach();
                if (clearAllObservers) {
                    data.clearObservers();
                }
            }
        }
    }

    public void detach() {
        detach(false);
    }

    public void notifyChanged(boolean onlySelf, Data param) {
        for (Data Data : getPackets()) {
            if (null != Data) {
                Data.notifyDataChanged(onlySelf, param);
            }
        }
    }

    public void notifyChanged(boolean onlySelf) {
        for (Data Data : getPackets()) {
            if (null != Data) {
                Data.notifyDataChanged(onlySelf);
            }
        }
    }

    public void notifyChangedAll(boolean onlySelf, Data param) {
        for (Data Data : getPackets()) {
            if (null != Data) {
                Data.notifyDataChangedAll(onlySelf, param);
            }
        }
    }

    public void notifyChangedAll(boolean onlySelf) {
        for (Data Data : getPackets()) {
            if (null != Data) {
                Data.notifyDataChangedAll(onlySelf);
            }
        }
    }

    public void notifyChangedAll() {
        for (Data Data : getPackets()) {
            if (null != Data) {
                Data.notifyDataChangedAll();
            }
        }
    }
}
