package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.LongArrayObserver;

/**
 * Created by zeyu on 2016/11/28.
 */

public class LongArray extends Data<LongArrayObserver> {
    public LongArray() {
        this((long[]) null);
    }

    public LongArray(long[] value) {
        mValue = value;
    }

    public LongArray(String name) {
        super(name);
    }

    public LongArray(String name, long[] value) {
        super(name, value);
    }

    public LongArray(String name, long[] value, boolean attach) {
        super(name, value, attach);
    }

    public LongArray(String name, long[] value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public LongArray(String module, String name) {
        super(module, name, null);
    }

    public LongArray(String module, String name, long[] value) {
        super(module, name, value);
    }

    public LongArray(String module, String name, long[] value, boolean attach) {
        super(module, name, value, attach);
    }

    public LongArray(String module, String name, long[] value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public long[] get() {
        return (long[])mValue;
    }

    @Override
    public String toString() {
        String info = null;
        if (null != mValue) {
            for (long val : (long[])mValue) {
                if (null != info) info += " ";
                info += val;
            }
        } else return "null";
        return info;
    }

    public String hexString() {
        String info = null;
        if (null != mValue) {
            for (long val : (long[])mValue) {
                if (null != info) info += " ";
                info += java.lang.Long.toHexString(val);
            }
        } else return "null";
        return info;
    }

    public boolean set(long[] value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return set(value, observer, exeAddedObserver, param);
    }

    public boolean set(long[] value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(long[] value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(long[] value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(long[] value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(long[] value) {
        return setData(value, null, true, null);
    }

    public boolean clear(boolean exeAddedObserver) {
        return set((long[])null, exeAddedObserver);
    }

    public boolean clear() {
        return set((long[])null);
    }

    @Override
    protected Object onExecLocalObserver(LongArrayObserver longArrayObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != longArrayObserver) ? longArrayObserver.onChanged((long[])curr.mValue, (long[])last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected LongArray(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        int len = in.readInt();
        if (len > 0) {
            mValue = new long[len];
            in.readLongArray((long[])mValue);
        }
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
        int len = (null != mValue) ? ((long[])mValue).length: 0;
        dest.writeInt(len);
        if (len > 0) {
            dest.writeLongArray((long[])mValue);
        }
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param object
     */
    public Data copy(Object object) {
        super.copy(object);
        if (null == object) {
            mValue = null;
        } else {
            long[] values = null;
            if (object instanceof long[]) {
                values = (long[]) object;
            } else if (object instanceof LongArray) {
                values = (long[])((LongArray) object).mValue;
            } else if (null != object) {
                return this;
            }

            if (null != values) {
                mValue = new long[values.length];
                System.arraycopy(values, 0, mValue, 0, values.length);
            } else {
                mValue = null;
            }
        }
        return this;
    }

    /**
     * (Must be overloaded
     */
    @Override
    public boolean equals(Object obj) {
        long[] value;
        if (obj instanceof long[]) {
            value = (long[]) obj;
        } else if (obj instanceof LongArray) {
            value = (long[])((LongArray)obj).mValue;
        } else return false;

        if (null == mValue) {
            if (null == value) {
                return true;
            }
        } else if (null != value && ((long[])mValue).length == value.length) {
            for (int i = 0; i < value.length; i++) {
                if (((long[])mValue)[i] != value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
