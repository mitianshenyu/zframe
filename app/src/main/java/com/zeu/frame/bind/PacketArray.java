package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.PacketArrayObserver;

import java.lang.reflect.Array;

/**
 * Created by zeu on 2017/6/6.
 */

public class PacketArray<T extends Packet> extends Data<PacketArrayObserver> {
    public PacketArray() {
        this((T[])null);
    }

    public PacketArray(T[] value) {
        mValue = value;
    }

    public PacketArray(String name) {
        super(name);
    }

    public PacketArray(String name, T[] value) {
        super(name, value);
    }

    public PacketArray(String name, T[] value, boolean attach) {
        super(name, value, attach);
    }

    public PacketArray(String name, T[] value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public PacketArray(String module, String name) {
        super(module, name, null);
    }

    public PacketArray(String module, String name, T[] value) {
        super(module, name, value);
    }

    public PacketArray(String module, String name, T[] value, boolean attach) {
        super(module, name, value, attach);
    }

    public PacketArray(String module, String name, T[] value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public T[] get() {
        return (T[])mValue;
    }

    @Override
    public String toString() {
        String info = null;
        if (null != mValue) {
            for (Packet val : (T[])mValue) {
                if (null != info) info += " ";
                info += ((null != val) ? val.string() : "null");
            }
        } else return "null";
        return info;
    }

    public boolean set(T[] value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(T[] value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(T[] value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(T[] value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(T[] value, Data Data) {
        return setData(value, null, true, Data);
    }

    public boolean set(T[] value) {
        return setData(value, null, true, null);
    }

    public boolean clear(boolean exeAddedObserver) {
        return set((T[])null, exeAddedObserver);
    }

    public boolean clear() {
        return set((T[])null);
    }

    @Override
    protected Object onExecLocalObserver(PacketArrayObserver iPacketArrayObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return  (iPacketArrayObserver != null) ? iPacketArrayObserver.onChanged((T[])curr.mValue, (T[])last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected PacketArray(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        mValue = Binders.createArrayFromParcel(in);
    }


    /**
     * 分别将每个packet写进去
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
        Binders.writeArrayParcel(dest, mValue, flags);
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param object
     */
    public Data copy(Object object) {
        super.copy(object);
        if (object instanceof PacketArray) {
            object = ((PacketArray) object).mValue;
            if (null == object) {
                mValue = null;
                return this;
            }
        }

        if (null != object) {
            try {
                Class cls = object.getClass();
                if (cls.isArray() && Data.class.isAssignableFrom(cls.getComponentType())) {
                    int len = Array.getLength(object);
                    Object arr = Array.newInstance(cls.getComponentType(), len);
                    if (null != arr) {
                        System.arraycopy(object, 0, arr, 0, len);
                    }
                    mValue = arr;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * (Must be overloaded
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Data) {
            obj = ((Data)obj).mValue;
        }
        if (null == mValue) {
            if (null == obj) {
                return true;
            }
        } else if (null != obj) {
            if (obj.getClass().isArray()) {
                try {
                    int len = Array.getLength(obj);
                    if (len == Array.getLength(mValue)) {
                        for (int i = 0; i < len; i++) {
                            Object a = Array.get(mValue, i);
                            Object b = Array.get(obj, i);
                            if (null == a) {
                                if (null != b) {
                                    return false;
                                }
                            } else if (null == b || !b.equals(a)) {
                                return false;
                            }
                        }
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return obj.equals(mValue);
            }
        }
        return false;
    }
}
