package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.CharacterArrayObserver;
import com.zeu.frame.bind.observer.DataChangedObserver;

/**
 * Created by zeyu on 2016/11/28.
 */

public class CharacterArray extends Data<CharacterArrayObserver> {
    public CharacterArray() {
        this((char[])null);
    }

    public CharacterArray(char[] value) {
        mValue = value;
    }

    public CharacterArray(String name) {
        super(name);
    }

    public CharacterArray(String name, char[] value) {
        super(name, value);
    }

    public CharacterArray(String name, char[] value, boolean attach) {
        super(name, value, attach);
    }

    public CharacterArray(String name, char[] value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public CharacterArray(String module, String name) {
        super(module, name, null);
    }

    public CharacterArray(String module, String name, char[] value) {
        super(module, name, value);
    }

    public CharacterArray(String module, String name, char[] value, boolean attach) {
        super(module, name, value, attach);
    }

    public CharacterArray(String module, String name, char[] value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }
    public char[] get() {
        return (char[])mValue;
    }

    @Override
    public String toString() {
        String info = null;
        if (null != mValue) {
            for (char val : (char[])mValue) {
                if (null != info) info += " ";
                info += val;
            }
        } else return "null";
        return info;
    }

    public boolean set(char[] value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(char[] value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(char[] value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(char[] value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(char[] value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(char[] value) {
        return setData(value, null, true, null);
    }

    public boolean clear(boolean exeAddedObserver) {
        return set((char[])null, exeAddedObserver);
    }

    public boolean clear() {
        return set((char[])null);
    }

    @Override
    protected Object onExecLocalObserver(CharacterArrayObserver characterArrayObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != characterArrayObserver) ? characterArrayObserver.onChanged((char[])curr.mValue, (char[])last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected CharacterArray(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        int len = in.readInt();
        if (len > 0) {
            mValue = new char[len];
            in.readCharArray((char[])mValue);
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
        int len = (null != mValue) ? ((char[])mValue).length: 0;
        dest.writeInt(len);
        if (len > 0) {
            dest.writeCharArray((char[])mValue);
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
            char[] values = null;
            if (object instanceof char[]) {
                values = (char[]) object;
            } else if (object instanceof CharacterArray) {
                values = (char[])((CharacterArray) object).mValue;
            } else if (null != object) {
                return this;
            }

            if (null != values) {
                mValue = new char[values.length];
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
        char[] value;
        if (obj instanceof char[]) {
            value = (char[]) obj;
        } else if (obj instanceof CharacterArray) {
            value = (char[])((CharacterArray)obj).mValue;
        } else return false;

        if (null == mValue) {
            if (null == value) {
                return true;
            }
        } else if (null != value && ((char[])mValue).length == value.length) {
            for (int i = 0; i < value.length; i++) {
                if (((char[])mValue)[i] != value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
