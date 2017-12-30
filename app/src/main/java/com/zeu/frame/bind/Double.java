package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.DoubleObserver;


/**
 * Created by zeyu on 2016/11/28.
 */

public class Double extends Data<DoubleObserver> {
    public Double() {
        this(0);
    }

    public Double(double value) {
        mValue = value;
    }

    public Double(String name) {
        super(name, (double)0);
    }

    public Double(String name, double value) {
        super(name, value);
    }

    public Double(String name, double value, boolean attach) {
        super(name, value, attach);
    }

    public Double(String name, double value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public Double(String module, String name) {
        super(module, name, (double)0);
    }

    public Double(String module, String name, double value) {
        super(module, name, value);
    }

    public Double(String module, String name, double value, boolean attach) {
        super(module, name, value, attach);
    }

    public Double(String module, String name, double value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public double get() {
        return (double)mValue;
    }

    public boolean set(double value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(double value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(double value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(double value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(double value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(double value) {
        return setData(value, null, true, null);
    }

    @Override
    public String toString() {
        return ""+mValue;
    }

    @Override
    protected Object onExecLocalObserver(DoubleObserver doubleObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != doubleObserver) ? doubleObserver.onChanged((double)curr.mValue, (double)last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected Double(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = in.readDouble();
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
        dest.writeDouble((double)mValue);
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param value
     */
    public Data copy(Object value) {
        super.copy(value);
        if (null != value) {
            if (value.getClass().equals(double.class) || value instanceof java.lang.Double) {
                mValue = (double) value;
            } else if (value instanceof Double) {
                mValue = ((Double) value).mValue;
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
            if ((obj.getClass().equals(double.class) || obj instanceof java.lang.Double) && (double)mValue == (double) obj) {
                return true;
            } else if (obj instanceof Double && mValue == ((Double) obj).mValue) {
                return true;
            }
        }
        return false;
    }
}
