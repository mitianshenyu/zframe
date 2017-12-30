package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.FloatObserver;
/**
 * Created by zeyu on 2016/11/28.
 */

public class Float extends Data<FloatObserver> {
    public Float() {
        this(0);
    }

    public Float(float value) {
        mValue = value;
    }

    public Float(String name) {
        super(name, (float)0);
    }

    public Float(String name, float value) {
        super(name, value);
    }

    public Float(String name, float value, boolean attach) {
        super(name, value, attach);
    }

    public Float(String name, float value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public Float(String module, String name) {
        super(module, name, (float)0);
    }

    public Float(String module, String name, float value) {
        super(module, name, value);
    }

    public Float(String module, String name, float value, boolean attach) {
        super(module, name, value, attach);
    }

    public Float(String module, String name, float value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public float get() {
        return (float)mValue;
    }

    public boolean set(float value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(float value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(float value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(float value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(float value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(float value) {
        return setData(value, null, true, null);
    }

    @Override
    public String toString() {
        return ""+mValue;
    }

    @Override
    protected Object onExecLocalObserver(FloatObserver floatObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != floatObserver) ? floatObserver.onChanged((float)curr.mValue, (float)last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected Float(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = in.readFloat();
    }

    /**
     * (Must be overloaded, super.writeToParcel(...), Must be placed above the user code)
     * aidl 依次写入函数索引,函数入口参数方向, 参数值, 然后写入定义的变量, 我的做法是在写入变量之前插入了类名
     * 读取的顺序必须严格按照写入的顺序
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        super.writeToParcel(dest, 1);
        if (null == dest) return;
        dest.writeFloat((float)mValue);
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param value
     */
    public Data copy(Object value) {
        super.copy(value);
        if (null != value) {
            if (value.getClass().equals(float.class) || value instanceof java.lang.Float) {
                mValue = (float) value;
            } else if (value instanceof Float) {
                mValue = ((Float) value).mValue;
            }
        }
        return this;
    }

    /**
     * (Must be overloaded
     */
    @Override
    public boolean equals(Object obj) {
        if (null != obj) {
            if ((obj.getClass().equals(float.class) || obj instanceof java.lang.Float) && (float)mValue == (float) obj) {
                return true;
            } else if (obj instanceof Float && mValue == ((Float) obj).mValue) {
                return true;
            }
        }
        return false;
    }
}
