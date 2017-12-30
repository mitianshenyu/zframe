package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.CharacterObserver;
import com.zeu.frame.bind.observer.DataChangedObserver;


/**
 * Created by zeyu on 2016/11/28.
 */

public class Character extends Data<CharacterObserver> {
    public Character() {
       this((char) 0);
    }

    public Character(char value) {
        mValue = value;
    }

    public Character(String name) {
        super(name, (char)0);
    }

    public Character(String name, char value) {
        super(name, value);
    }

    public Character(String name, char value, boolean attach) {
        super(name, value, attach);
    }

    public Character(String name, char value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public Character(String module, String name) {
        super(module, name, (char)0);
    }

    public Character(String module, String name, char value) {
        super(module, name, value);
    }

    public Character(String module, String name, char value, boolean attach) {
        super(module, name, value, attach);
    }

    public Character(String module, String name, char value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }
    public boolean set(char value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(char value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(char value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(char value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(char value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(char value) {
        return setData(value, null, true, null);
    }

    @Override
    public String toString() {
        return ""+mValue;
    }

    @Override
    protected Object onExecLocalObserver(CharacterObserver characterObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != characterObserver) ? characterObserver.onChanged((char)curr.mValue, (char)last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected Character(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = (char) in.readInt();
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
        dest.writeInt((int)mValue);
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param value
     */
    public Data copy(Object value) {
        super.copy(value);
        if (null != value) {
            if (value.getClass().equals(char.class) || value instanceof java.lang.Character) {
                mValue = (char) value;
            } else if (value instanceof Character) {
                mValue = ((Character) value).mValue;
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
            if ((obj.getClass().equals(char.class) || obj instanceof java.lang.Character) && (char)mValue == (char) obj) {
                return true;
            } else if (obj instanceof Character && mValue == ((Character) obj).mValue) {
                return true;
            }
        }
        return false;
    }
}
