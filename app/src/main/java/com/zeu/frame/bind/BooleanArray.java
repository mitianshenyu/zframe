package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.BooleanArrayObserver;
import com.zeu.frame.bind.observer.DataChangedObserver;;

/**
 * Created by zeu on 2016/11/28.
 */

public class BooleanArray extends Data<BooleanArrayObserver> {
    public BooleanArray() {
        this((boolean[])null);
    }

    public BooleanArray(boolean[] value) {
        mValue = value;
    }

    public BooleanArray(String name) {
        super(name);
    }

    public BooleanArray(String name, boolean[] value) {
        super(name, value);
    }

    public BooleanArray(String name, boolean[] value, boolean attach) {
        super(name, value, attach);
    }

    public BooleanArray(String name, boolean[] value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public BooleanArray(String module, String name) {
        super(module, name, null);
    }

    public BooleanArray(String module, String name, boolean[] value) {
        super(module, name, value);
    }

    public BooleanArray(String module, String name, boolean[] value, boolean attach) {
        super(module, name, value, attach);
    }

    public BooleanArray(String module, String name, boolean[] value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public boolean[] get() {
        return (boolean[])mValue;
    }

    public boolean set(boolean[] value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(boolean[] value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(boolean[] value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(boolean[] value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(boolean[] value, Data param) {
        return setData(value, null, true, param);
    }

    public boolean set(boolean[] value) {
        return setData(value, null, true, null);
    }

    public boolean clear(boolean exeAddedObserver) {
        return set((boolean[])null, exeAddedObserver);
    }

    public boolean clear() {
        return set((boolean[])null);
    }

    @Override
    public String toString() {
        String info = null;
        if (mValue instanceof boolean[]) {
            for (boolean val : (boolean[])mValue) {
                if (null != info) info += " ";
                info += val;
            }
        } else return "null";
        return info;
    }

    @Override
    protected Object onExecLocalObserver(BooleanArrayObserver booleanArrayObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != booleanArrayObserver) ? booleanArrayObserver.onChanged((boolean[])curr.mValue, (boolean[])last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected BooleanArray(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        int len = in.readInt();
        if (len > 0) {
            mValue = new boolean[len];
            in.readBooleanArray((boolean[]) mValue);
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
        int len = (mValue instanceof boolean[]) ? ((boolean[])mValue).length: 0;
        dest.writeInt(len);
        if (len > 0) {
            dest.writeBooleanArray((boolean[])mValue);
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
            boolean[] values = null;
            if (object instanceof boolean[]) {
                values = (boolean[]) object;
            } else if (object instanceof BooleanArray) {
                values = (boolean[]) ((BooleanArray) object).mValue;
            } else if (null != object) {
                return this;
            }

            if (null != values) {
                mValue = new boolean[values.length];
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
        boolean[] value;
        if (obj instanceof boolean[]) {
            value = (boolean[]) obj;
        } else if (obj instanceof BooleanArray) {
            value = (boolean[]) ((BooleanArray)obj).mValue;
        } else return false;

        if (null == mValue) {
            if (null == value) {
                return true;
            }
        } else if (null != value && ((boolean[])mValue).length == value.length) {
            for (int i = 0; i < value.length; i++) {
                if (((boolean[])mValue)[i] != value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
