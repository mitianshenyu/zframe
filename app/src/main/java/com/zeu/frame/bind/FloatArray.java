package com.zeu.frame.bind;

import android.support.annotation.NonNull;

import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.bind.observer.FloatArrayObserver;

/**
 * Created by zeyu on 2016/11/28.
 */

public class FloatArray extends Data<FloatArrayObserver> {
    public FloatArray() {
        this((float[])null);
    }

    public FloatArray(float[] value) {
        mValue = value;
    }

    public FloatArray(String name) {
        super(name);
    }

    public FloatArray(String name, float[] value) {
        super(name, value);
    }

    public FloatArray(String name, float[] value, boolean attach) {
        super(name, value, attach);
    }

    public FloatArray(String name, float[] value, boolean attach, boolean focus) {
        super(name, value, attach, focus);
    }

    public FloatArray(String module, String name) {
        super(module, name, null);
    }

    public FloatArray(String module, String name, float[] value) {
        super(module, name, value);
    }

    public FloatArray(String module, String name, float[] value, boolean attach) {
        super(module, name, value, attach);
    }

    public FloatArray(String module, String name, float[] value, boolean attach, boolean focus) {
        super(module, name, value, attach, focus);
    }

    public float[] get() {
        return (float[])mValue;
    }

    @Override
    public String toString() {
        String info = null;
        if (null != mValue) {
            for (float val : (float[])mValue) {
                if (null != info) info += " ";
                info += val;
            }
        } else return "null";
        return info;
    }

    public boolean set(float[] value, DataChangedObserver observer, boolean exeAddedObserver, Data param) {
        return setData(value, observer, exeAddedObserver, param);
    }

    public boolean set(float[] value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(float[] value, boolean exeAddedObserver) {
        return setData(value, null, exeAddedObserver, null);
    }

    public boolean set(float[] value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(float[] value, Data packet) {
        return setData(value, null, true, packet);
    }

    public boolean set(float[] value) {
        return setData(value, null, true, null);
    }

    public boolean clear(boolean exeAddedObserver) {
        return set((float[])null, exeAddedObserver);
    }

    public boolean clear() {
        return set((float[])null);
    }

    @Override
    protected Object onExecLocalObserver(FloatArrayObserver floatArrayObserver, @NonNull Data curr, @NonNull Data last, Data param) {
        return (null != floatArrayObserver) ? floatArrayObserver.onChanged((float[])curr.mValue, (float[])last.mValue, param) : null;
    }

    /**
     * (Must be overloaded)
     * @param in
     */
    protected FloatArray(android.os.Parcel in) {
        super(in, 0);
        if (null == in) return;
        int len = in.readInt();
        if (len > 0) {
            mValue = new float[len];
            in.readFloatArray((float[])mValue);
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
        int len = (null != mValue) ? ((float[])mValue).length: 0;
        dest.writeInt(len);
        if (len > 0) {
            dest.writeFloatArray((float[])mValue);
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
            float[] values = null;
            if (object instanceof float[]) {
                values = (float[]) object;
            } else if (object instanceof FloatArray) {
                values = (float[])((FloatArray) object).mValue;
            } else if (null != object) {
                return this;
            }

            if (null != values) {
                mValue = new float[values.length];
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
        float[] value;
        if (obj instanceof float[]) {
            value = (float[]) obj;
        } else if (obj instanceof FloatArray) {
            value = (float[])((FloatArray)obj).mValue;
        } else return false;

        if (null == mValue) {
            if (null == value) {
                return true;
            }
        } else if (null != value && ((float[])mValue).length == value.length) {
            for (int i = 0; i < value.length; i++) {
                if (((float[])mValue)[i] != value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
