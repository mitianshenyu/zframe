package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.StrinsArrayObserver;

/**
 * Created by zeyu on 2016/11/28.
 */

public class StrinsArray extends Data<StrinsArrayObserver> {
    public StrinsArray() {
        this((String[]) null);
    }

    public StrinsArray(String[] value) {
        mValue = value;
    }

    public StrinsArray(String name) {
        super(name);
    }

    public StrinsArray(String name, String[] value) {
        super(name, value);
    }

    public StrinsArray(String name, String[] value, boolean attach) {
        super(name, value, attach);
    }

    public StrinsArray(String name, String[] value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public StrinsArray(String module, String name) {
        super(module, name, null);
    }

    public StrinsArray(String module, String name, String[] value) {
        super(module, name, value);
    }

    public StrinsArray(String module, String name, String[] value, boolean attach) {
        super(module, name, value, attach);
    }

    public StrinsArray(String module, String name, String[] value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public String[] get() {
        return (String[])mValue;
    }

    @Override
    public String toString() {
        String info = null;
        if (null != mValue) {
            for (String str : (String[])mValue) {
                if (null != info) info += " ";
                info += str;
            }
        } else return "null";
        return info;
    }

    public boolean set(String[] value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(String[] value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(String[] value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(String[] value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(String[] value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(String[] value) {
        return setData(value, null, true, null);
    }

    public boolean clear(boolean exeAddedObserver) {
        return set((String[])null, exeAddedObserver);
    }

    public boolean clear() {
        return set((String[])null);
    }

    @Override
    protected Object onExecLocalObserver(StrinsArrayObserver strinsArrayObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != strinsArrayObserver) ? strinsArrayObserver.onChanged((String[])curr.mValue, (String[])last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected StrinsArray(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        int len = in.readInt();
        if (len > 0) {
            mValue = new String[len];
        }
        in.readStringArray((String[])mValue);
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
        int len = (null != mValue) ? ((String[])mValue).length: 0;
        dest.writeInt(len);
        if (len > 0) {
            dest.writeStringArray((String[])mValue);
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
            String[] values = null;
            if (object instanceof String[]) {
                values = (String[]) object;
            } else if (object instanceof StrinsArray) {
                values = (String[])((StrinsArray) object).mValue;
            } else if (null != object) {
                return this;
            }

            if (null != values) {
                mValue = new String[values.length];
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
        String[] value;
        if (obj instanceof String[]) {
            value = (String[]) obj;
        } else if (obj instanceof StrinsArray) {
            value = (String[])((StrinsArray)obj).mValue;
        } else return false;

        if (null == mValue) {
            if (null == value) {
                return true;
            }
        } else if (null != value && ((String[])mValue).length == value.length) {
            for (int i = 0; i < value.length; i++) {
                if (((String[])mValue)[i] != value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
