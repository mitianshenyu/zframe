package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.DoubleArrayObserver;
/**
 * Created by zeyu on 2016/11/28.
 */

public class DoubleArray extends Data<DoubleArrayObserver> {
    public DoubleArray() {
        super();
    }

    public DoubleArray(double[] value) {
        mValue = value;
    }

    public DoubleArray(String name) {
        super(name);
    }

    public DoubleArray(String name, double[] value) {
        super(name, value);
    }

    public DoubleArray(String name, double[] value, boolean attach) {
        super(name, value, attach);
    }

    public DoubleArray(String name, double[] value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public DoubleArray(String module, String name) {
        super(module, name, null);
    }

    public DoubleArray(String module, String name, double[] value) {
        super(module, name, value);
    }

    public DoubleArray(String module, String name, double[] value, boolean attach) {
        super(module, name, value, attach);
    }

    public DoubleArray(String module, String name, double[] value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }
    public double[] get() {
        return (double[])mValue;
    }

    @Override
    public String toString() {
        String info = null;
        if (null != mValue) {
            for (double val : (double[])mValue) {
                if (null != info) info += " ";
                info += val;
            }
        } else return "null";
        return info;
    }

    public boolean set(double[] value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(double[] value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(double[] value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(double[] value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(double[] value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(double[] value) {
        return setData(value, null, true, null);
    }

    public boolean clear(boolean exeAddedObserver) {
        return set((double[])null, exeAddedObserver);
    }

    public boolean clear() {
        return set((double[])null);
    }

    @Override
    protected Object onExecLocalObserver(DoubleArrayObserver doubleArrayObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != doubleArrayObserver) ? doubleArrayObserver.onChanged((double[])curr.mValue, (double[])last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected DoubleArray(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        int len = in.readInt();
        if (len > 0) {
            mValue = new double[len];
            in.readDoubleArray((double[])mValue);
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
        int len = (null != mValue) ? ((double[])mValue).length: 0;
        dest.writeInt(len);
        if (len > 0) {
            dest.writeDoubleArray((double[])mValue);
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
            double[] values = null;
            if (object instanceof double[]) {
                values = (double[]) object;
            } else if (object instanceof DoubleArray) {
                values = (double[])((DoubleArray) object).mValue;
            } else if (null != object) {
                return this;
            }

            if (null != values) {
                mValue = new double[values.length];
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
        double[] value;
        if (obj instanceof double[]) {
            value = (double[]) obj;
        } else if (obj instanceof DoubleArray) {
            value = (double[])((DoubleArray)obj).mValue;
        } else return false;

        if (null == mValue) {
            if (null == value) {
                return true;
            }
        } else if (null != value && ((double[])mValue).length == value.length) {
            for (int i = 0; i < value.length; i++) {
                if (((double[])mValue)[i] != value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
