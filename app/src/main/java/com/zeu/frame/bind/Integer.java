package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.IntegerObserver;

/**
 * Created by zeyu on 2016/11/28.
 */

public class Integer extends Data<IntegerObserver> {
    public Integer() {
        this(0);
    }

    public Integer(int value) {
        mValue = value;
    }

    public Integer(String name) {
        super(name, 0);
    }

    public Integer(String name, int value) {
        super(name, value);
    }

    public Integer(String name, int value, boolean attach) {
        super(name, value, attach);
    }

    public Integer(String name, int value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public Integer(String module, String name) {
        super(module, name, 0);
    }

    public Integer(String module, String name, int value) {
        super(module, name, value);
    }

    public Integer(String module, String name, int value, boolean attach) {
        super(module, name, value, attach);
    }

    public Integer(String module, String name, int value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public int get() {
        return (int) mValue;
    }

    @Override
    public String toString() {
        return ""+mValue;
    }

    public String hexString() {
        return ""+ java.lang.Integer.toHexString((int)mValue);
    }

    public boolean set(int value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(int value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(int value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(int value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(int value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(int value) {
        return setData(value, null, true, null);
    }

    @Override
    protected Object onExecLocalObserver(IntegerObserver integerObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != integerObserver) ? integerObserver.onChanged((int)curr.mValue, (int)last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected Integer(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = in.readInt();
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
        dest.writeInt((int)mValue);
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param value
     */
    public Data copy(Object value) {
        super.copy(value);
        if (null != value) {
            if (value.getClass().equals(int.class) || value instanceof java.lang.Integer) {
                mValue = value;
            } else if (value instanceof Integer) {
                mValue = ((Integer) value).mValue;
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
            if ((obj.getClass().equals(int.class) || obj instanceof java.lang.Integer) && (int)mValue == (int) obj) {
                return true;
            } else if (obj instanceof Integer && mValue == ((Integer) obj).mValue) {
                return true;
            }
        }
        return false;
    }
}
