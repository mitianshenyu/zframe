package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.StrinsObserver;


/**
 * Created by zeyu on 2016/11/28.
 */

public class Strins extends Data<StrinsObserver> {
    public Strins() {
        super(null);
    }

    public Strins(String value) {
        mValue = value;
    }

    public Strins(String name, String value) {
        super(name, value);
    }

    public Strins(String name, String value, boolean attach) {
        super(name, value, attach);
    }

    public Strins(String name, String value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public Strins(String module, String name, String value) {
        super(module, name, value);
    }

    public Strins(String module, String name, String value, boolean attach) {
        super(module, name, value, attach);
    }

    public Strins(String module, String name, String value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public String get() {
        return (String) mValue;
    }

    @Override
    public String toString() {
        return ""+mValue;
    }

    public boolean set(String value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(String value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(String value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(String value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(String value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(String value) {
        return setData(value, null, true, null);
    }

    @Override
    protected Object onExecLocalObserver(StrinsObserver strinsObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != strinsObserver) ? strinsObserver.onChanged((String) curr.mValue, (String)last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected Strins(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = in.readString();
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
        dest.writeString((String) mValue);
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param value
     */
    public Data copy(Object value) {
        super.copy(value);
        if (value instanceof String) {
            mValue = value;
        } else if (value instanceof Strins) {
            mValue = ((Strins)value).mValue;
        } else if (null == value) {
            mValue = null;
        }
        return this;
    }

    /**
     * (Must be overloaded
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String && obj.equals(mValue)) {
            return true;
        } else if (obj instanceof Strins) {
            String str = (String)((Strins)obj).mValue;
            if (null == str) {
                if (null == mValue) {
                    return true;
                }
            } else if (str.equals(mValue)) {
                return true;
            }
        }
        return false;
    }
}
