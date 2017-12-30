package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.BooleanObserver;
import com.zeu.frame.bind.observer.DataChangedObserver;

/**
 * Created by zeu on 2016/11/28.
 */

public class Boolean extends Data<BooleanObserver> {
    public Boolean() {
        this(false);
    }

    public Boolean(boolean value) {
        mValue = value;
    }

    public Boolean(String name) {
        super(name, false);
    }

    public Boolean(String name, boolean value) {
        super(name, value);
    }

    public Boolean(String name, boolean value, boolean attach) {
        super(name, value, attach);
    }

    public Boolean(String name, boolean value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public Boolean(String module, String name) {
        super(module, name, false, false, false);
    }

    public Boolean(String module, String name, boolean value) {
        super(module, name, value, false, false);
    }

    public Boolean(String module, String name, boolean value, boolean attach) {
        super(module, name, value, attach, false);
    }

    public Boolean(String module, String name, boolean value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    protected boolean set(boolean value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(boolean value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(boolean value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(boolean value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(boolean value) {
        return setData(value, null, true, null);
    }

    public boolean set(boolean value, Data param) {
        return setData(value, null, true, param);
    }

    public boolean get() {
        return  (boolean)mValue;
    }

    @Override
    public String toString() {
        return ""+mValue;
    }

    @Override
    protected Object onExecLocalObserver(BooleanObserver booleanObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != booleanObserver) ? booleanObserver.onChanged((boolean)curr.mValue, (boolean)last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected Boolean(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = in.readInt() == 0 ? false : true;
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
        dest.writeInt((boolean)mValue?1:0);
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param value
     */
    public Data copy(Object value) {
        if (null != value) {
            if (value.getClass().equals(boolean.class) || value instanceof java.lang.Boolean) {
                mValue = value;
            } else if (value instanceof Boolean) {
                mValue = ((Boolean) value).mValue;
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
            if ((obj.getClass().equals(boolean.class) || obj instanceof java.lang.Boolean) && (boolean)mValue == (boolean) obj) {
                return true;
            } else if (obj instanceof Boolean && mValue == ((Boolean) obj).mValue) {
                return true;
            }
        }
        return false;
    }
}
