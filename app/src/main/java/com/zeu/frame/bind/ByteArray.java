package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.ByteArrayObserver;
import com.zeu.frame.bind.observer.DataChangedObserver;

/**
 * Created by zeyu on 2016/11/28.
 */

public class ByteArray extends Data<ByteArrayObserver> {
    public ByteArray() {
        this((byte[])null);
    }

    public ByteArray(byte[] value) {
        mValue = value;
    }

    public ByteArray(String name) {
        super(name);
    }

    public ByteArray(String name, byte[] value) {
        super(name, value);
    }

    public ByteArray(String name, byte[] value, boolean attach) {
        super(name, value, attach);
    }

    public ByteArray(String name, byte[] value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public ByteArray(String module, String name) {
        super(module, name, null);
    }

    public ByteArray(String module, String name, byte[] value) {
        super(module, name, value);
    }

    public ByteArray(String module, String name, byte[] value, boolean attach) {
        super(module, name, value, attach);
    }

    public ByteArray(String module, String name, byte[] value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public byte[] get() {
        return (byte[])mValue;
    }

    @Override
    public String toString() {
        String info = null;
        if (null != mValue) {
            for (byte val : (byte[])mValue) {
                if (null != info) info += " ";
                info += val;
            }
        } else return "null";
        return info;
    }

    public boolean set(byte[] value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(byte[] value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(byte[] value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(byte[] value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(byte[] value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(byte[] value) {
        return setData(value, null, true, null);
    }

    public boolean clear(boolean exeAddedObserver) {
        return set((byte[])null, exeAddedObserver);
    }

    public boolean clear() {
        return set((byte[])null);
    }

    @Override
    protected Object onExecLocalObserver(ByteArrayObserver byteArrayObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != byteArrayObserver) ? byteArrayObserver.onChanged((byte[])curr.mValue, (byte[])last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected ByteArray(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = new byte[in.readInt()];
        in.readByteArray((byte[])mValue);
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
        int len = (null != mValue) ? ((byte[])mValue).length: 0;
        dest.writeInt(len);
        if (len > 0) {
            dest.writeByteArray((byte[])mValue);
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
            byte[] values = null;
            if (object instanceof byte[]) {
                values = (byte[]) object;
            } else if (object instanceof ByteArray) {
                values = (byte[])((ByteArray) object).mValue;
            } else if (null != object) {
                return this;
            }

            if (null != values) {
                mValue = new byte[values.length];
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
        byte[] value;
        if (obj instanceof byte[]) {
            value = (byte[]) obj;
        } else if (obj instanceof ByteArray) {
            value = (byte[])((ByteArray)obj).mValue;
        } else return false;

        if (null == mValue) {
            if (null == value) {
                return true;
            }
        } else if (null != value && ((byte[])mValue).length == value.length) {
            for (int i = 0; i < value.length; i++) {
                if (((byte[])mValue)[i] != value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
