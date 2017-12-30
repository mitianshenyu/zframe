package com.zeu.frame.bind;
import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.PacketObserver;

import java.lang.reflect.Array;

/**
 * Created by zeu on 2016/11/29.
 * 此为数据视图
 */
public class Packet<T extends Data> extends Data<PacketObserver> {
    public Packet() {
        super();
    }

    public Packet(T value) {
        super(null, value);
    }

    public Packet(String name) {
        super(name);
    }

    public Packet(String name, T value) {
        super(name, value);
    }

    public Packet(String name, T value, boolean attach) {
        super(name, value, attach);
    }

    public Packet(String name, T value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public Packet(String module, String name) {
        super(module, name);
    }

    public Packet(String module, String name, T value) {
        super(module, name, value);
    }

    public Packet(String module, String name, T value, boolean attach) {
        super(module, name, value, attach);
    }

    public Packet(String module, String name, T value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public T value() {
        return (T) mValue;
    }

    @Override
    protected Object onExecLocalObserver(PacketObserver packetObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != packetObserver) ? packetObserver.onChanged((Packet) curr, (Packet) last, param) : null;
    }

    /**
     * (Must be overloaded), 如果不支持远程, 必须继承, 但可以不写值
     * @param in
     */
    protected Packet(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        int type = in.readInt();
        if (type == 1) {
            mValue = Binders.createObjFromParcel(in);
        } else if (type == 2) {
            mValue = Binders.createArrayFromParcel(in);
        } else {
            mValue = null;
        }
    }

    /**
     * (Must be overloaded, super.writeToParcel(...), Must be placed above the user code)
     * aidl 依次写入函数索引,函数入口参数方向, 参数值, 然后写入定义的变量, 我的做法是在写入变量之前插入了类名
     * 读取的顺序必须严格按照写入的顺序, 如果不支持远程, 无需继承
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        super.writeToParcel(dest, 1);
        if (null == dest) return;
        if (mValue == null) {
            dest.writeInt(0); //null
        } else if (mValue.getClass().isArray()) {
            dest.writeInt(2); //array
            Binders.writeArrayParcel(dest, mValue, flags);
        } else {
            dest.writeInt(1); //not array
            dest.writeInt(1); //写方向
            ((Data)mValue).writeToParcel(dest, flags); //写入值
        }
    }

    @Override
    public Data copy(Object object) {
        if (null != object) {
            if (object instanceof Data && object.getClass().equals(this.getClass())) {
                mValue = ((Data) object).mValue;
            } else if (null != mValue && mValue.getClass().equals(object.getClass())) {
                mValue = object;
            }
        }
        return this;
    }

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
