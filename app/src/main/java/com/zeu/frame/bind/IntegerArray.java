package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.IntegerArrayObserver;
/**
 * Created by zeyu on 2016/11/28.
 */

public class IntegerArray extends Data<IntegerArrayObserver> {
    public IntegerArray() {
        this((int[]) null);
    }

    public IntegerArray(int[] value) {
        mValue = value;
    }

    public IntegerArray(String name) {
        super(name);
    }

    public IntegerArray(String name, int[] value) {
        super(name, value);
    }

    public IntegerArray(String name, int[] value, boolean attach) {
        super(name, value, attach);
    }

    public IntegerArray(String name, int[] value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public IntegerArray(String module, String name) {
        super(module, name, null);
    }

    public IntegerArray(String module, String name, int[] value) {
        super(module, name, value);
    }

    public IntegerArray(String module, String name, int[] value, boolean attach) {
        super(module, name, value, attach);
    }

    public IntegerArray(String module, String name, int[] value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public int[] get() {
        return (int[])mValue;
    }

    @Override
    public String toString() {
        String info = null;
        if (null != mValue) {
            for (int val : (int[])mValue) {
                if (null != info) info += " ";
                info += val;
            }
        } else return "null";
        return info;
    }

    public String hexString() {
        String info = null;
        if (null != mValue) {
            for (int val : (int[])mValue) {
                if (null != info) info += " ";
                info += java.lang.Integer.toHexString(val);
            }
        } else return "null";
        return info;
    }

    public boolean set(int[] value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(int[] value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(int[] value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(int[] value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(int[] value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(int[] value) {
        return setData(value, null, true, null);
    }

    public boolean clear(boolean exeAddedObserver) {
        return set((int[])null, exeAddedObserver);
    }

    public boolean clear() {
        return set((int[])null);
    }

    @Override
    protected Object onExecLocalObserver(IntegerArrayObserver integerArrayObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != integerArrayObserver) ? integerArrayObserver.onChanged((int[])curr.mValue, (int[])last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected IntegerArray(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        int len = in.readInt();
        if (len > 0) {
            mValue = new int[len];
            in.readIntArray((int[])mValue);
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
        int len = (null != mValue) ? ((int[])mValue).length: 0;
        dest.writeInt(len);
        if (len > 0) {
            dest.writeIntArray((int[])mValue);
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
            int[] values = null;
            if (object instanceof int[]) {
                values = (int[]) object;
            } else if (object instanceof IntegerArray) {
                values = (int[])((IntegerArray) object).mValue;
            } else if (null != object) {
                return this;
            }

            if (null != values) {
                mValue = new int[values.length];
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
        int[] value;
        if (obj instanceof int[]) {
            value = (int[]) obj;
        } else if (obj instanceof IntegerArray) {
            value = (int[])((IntegerArray)obj).mValue;
        } else return false;

        if (null == mValue) {
            if (null == value) {
                return true;
            }
        } else if (null != value && ((int[])mValue).length == value.length) {
            for (int i = 0; i < value.length; i++) {
                if (((int[])mValue)[i] != value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
