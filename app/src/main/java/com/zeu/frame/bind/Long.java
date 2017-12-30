package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.LongObserver;
/**
 * Created by zeyu on 2016/11/28.
 */

public class Long extends Data<LongObserver> {
    public Long() {
        this(0);
    }

    public Long(long value) {
        mValue = value;
    }

    public Long(String name) {
        super(name, (long)0);
    }

    public Long(String name, long value) {
        super(name, value);
    }

    public Long(String name, long value, boolean attach) {
        super(name, value, attach);
    }

    public Long(String name, long value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public Long(String module, String name) {
        super(module, name, (long)0);
    }

    public Long(String module, String name, long value) {
        super(module, name, value);
    }

    public Long(String module, String name, long value, boolean attach) {
        super(module, name, value, attach);
    }

    public Long(String module, String name, long value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public long get() {
        return (long)mValue;
    }

    public boolean set(long value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(long value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(long value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(long value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(long value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(long value) {
        return setData(value, null, true, null);
    }

    @Override
    public String toString() {
        return ""+mValue;
    }

    public String hexString() {
        return ""+ java.lang.Long.toHexString((long)mValue);
    }

    @Override
    protected Object onExecLocalObserver(LongObserver longObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != longObserver) ? longObserver.onChanged((long)curr.mValue, (long)last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected Long(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = in.readLong();
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
        dest.writeLong((long)mValue);
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param value
     */
    public Data copy(Object value) {
        super.copy(value);
        if (null != value) {
            if (value.getClass().equals(long.class) || value instanceof java.lang.Long) {
                mValue = value;
            } else if (value instanceof Long) {
                mValue = ((Long) value).mValue;
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
            if ((obj.getClass().equals(long.class) || obj instanceof java.lang.Long) && (long)mValue == (long) obj) {
                return true;
            } else if (obj instanceof Long && mValue == ((Long) obj).mValue) {
                return true;
            }
        }
        return false;
    }
}
