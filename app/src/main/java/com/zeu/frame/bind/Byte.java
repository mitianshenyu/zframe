package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.ByteObserver;
import com.zeu.frame.bind.observer.DataChangedObserver;


/**
 * Created by zeyu on 2016/11/28.
 */

public class Byte extends Data<ByteObserver> {
    public Byte() {
        this((byte)0);
    }

    public Byte(byte value) {
        mValue = value;
    }

    public Byte(String name) {
        super(name, (byte)0);
    }

    public Byte(String name, byte value) {
        super(name, value);
    }

    public Byte(String name, byte value, boolean attach) {
        super(name, value, attach);
    }

    public Byte(String name, byte value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public Byte(String module, String name) {
        super(module, name, (byte)0);
    }

    public Byte(String module, String name, byte value) {
        super(module, name, value);
    }

    public Byte(String module, String name, byte value, boolean attach) {
        super(module, name, value, attach);
    }

    public Byte(String module, String name, byte value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public byte get() {
        return (byte)mValue;
    }

    @Override
    public String toString() {
        return ""+mValue;
    }

    public boolean set(byte value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(byte value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(byte value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(byte value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(byte value, Data param) {
        return setData(value, null, true, param);
    }

    public boolean set(byte value) {
        return setData(value, null, true, null);
    }

    @Override
    protected Object onExecLocalObserver(ByteObserver byteObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != byteObserver) ? byteObserver.onChanged((byte)curr.mValue, (byte)last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected Byte(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = in.readByte();
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
        dest.writeByte((byte)mValue);
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param value
     */
    public Data copy(Object value) {
        super.copy(value);
        if (null != value) {
            if (value.getClass().equals(byte.class) || value instanceof java.lang.Byte) {
                mValue = value;
            } else if (value instanceof Byte) {
                mValue = ((Byte) value).mValue;
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
            if ((obj.getClass().equals(byte.class) || obj instanceof java.lang.Byte) && (byte)mValue == (byte) obj) {
                return true;
            } else if (obj instanceof Byte && mValue == ((Byte) obj).mValue) {
                return true;
            }
        }
        return false;
    }
}
