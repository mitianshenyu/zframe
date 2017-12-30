package com.zeu.frame.bind;

import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.zeu.frame.BuildConfig;
import com.zeu.frame.bind.Binders.Container;
import com.zeu.frame.bind.callback.DataViewCallback;
import com.zeu.frame.bind.comm.ILockerSource.Stub;
import com.zeu.frame.bind.comm.IRemoter;
import com.zeu.frame.bind.listener.OnLocalAttachListener;
import com.zeu.frame.bind.listener.OnRemoteAttachListener;
import com.zeu.frame.bind.observer.DataChangedObserver;
import com.zeu.frame.log.Slog;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Data<OBSERVER> implements DataViewCallback, Parcelable {
    public static final Creator<Data> CREATOR = new Creator<Data>() {
        public Data createFromParcel(Parcel in) {
            return (Data) Binders.createObjFromParcel(in);
        }

        public Data[] newArray(int size) {
            return new Data[size];
        }
    };
    protected boolean mEnableLastValue;
    protected Handler mHandler;
    protected List<OBSERVER> mLocalObservers;
    protected String mLocker;
    OnLocalAttachListener mOnLocalAttachListener;
    OnRemoteAttachListener mOnRemoteAttachListener;
    protected boolean mSupportRemote;
    protected String mThisDataName;
    protected String mThisModuleName;
    protected Object mUniqueName;
    protected Object mValue;

    public Data() {
        this.mSupportRemote = true;
        this.mEnableLastValue = false;
        this.mUniqueName = new Object();
        this.mLocalObservers = new ArrayList();
    }

    public Data(String name) {
        this(BuildConfig.FLAVOR, name);
    }

    protected Data(String name, Object value) {
        this(BuildConfig.FLAVOR, name, value, false);
    }

    protected Data(String name, Object value, boolean attach) {
        this(BuildConfig.FLAVOR, name, value, attach, false);
    }

    protected Data(String name, Object value, boolean attach, boolean focus) {
        this(BuildConfig.FLAVOR, name, value, attach, focus);
    }

    protected Data(String module, String name, Object value) {
        this(module, name, value, false);
    }

    protected Data(String module, String name, Object value, boolean attach) {
        this(module, name, value, attach, false);
    }

    protected Data(String module, String name, Object value, boolean attach, boolean focus) {
        this(module, name);
        copy(value);
        if (attach) {
            attach(focus);
        }
    }

    public Data(String module, String name) {
        this.mSupportRemote = true;
        this.mEnableLastValue = false;
        this.mUniqueName = new Object();
        this.mLocalObservers = new ArrayList();
        this.mThisDataName = name;
        this.mThisModuleName = module;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public boolean onChanged(String dataName, String dataView, boolean yesSelfNoOther, Data curr, Data param, boolean execObserver) {
        String[] strArr = new String[5];
        strArr[0] = "sync from remote:";
        strArr[1] = "data:" + dataName;
        strArr[2] = "view:" + dataView;
        strArr[3] = "yesSelfNoOther:" + yesSelfNoOther;
        strArr[4] = curr instanceof Packet ? BuildConfig.FLAVOR : "data:" + curr;
        Slog.d(strArr);
        if (dataView != null && ((!yesSelfNoOther || !dataView.equals(string())) && (yesSelfNoOther || dataView.equals(string())))) {
            return false;
        }
        if (execObserver) {
            Data last;
            if (!(this instanceof Packet) || this.mEnableLastValue) {
                last = clone(this);
            } else {
                last = null;
            }
            if (curr == null || curr.mValue == null) {
                copy(curr);
            } else {
                this.mValue = curr.mValue;
            }
            execLocalObservers(null, true, this, last, param);
            return true;
        } else if (curr == null || curr.mValue == null) {
            copy(curr);
            return true;
        } else {
            this.mValue = curr.mValue;
            return true;
        }
    }

    public void onLocalAttach() {
        if (this.mOnLocalAttachListener != null) {
            this.mOnLocalAttachListener.onAttached();
        }
    }

    public void onLocalDetach() {
        if (this.mOnLocalAttachListener != null) {
            this.mOnLocalAttachListener.onDetached();
        }
    }

    public void onRemoteAttach(IRemoter remoter) {
        if (this.mOnRemoteAttachListener != null) {
            this.mOnRemoteAttachListener.onAttached(remoter);
        }
    }

    public void onRemoteDetach(IRemoter remoter) {
        if (this.mOnRemoteAttachListener != null) {
            this.mOnRemoteAttachListener.onDetached(remoter);
        }
    }

    public void setOnLocalAttachListener(OnLocalAttachListener listener) {
        this.mOnLocalAttachListener = listener;
    }

    public void setOnRemoteAttachListener(OnRemoteAttachListener listener) {
        this.mOnRemoteAttachListener = listener;
    }

    public void attach(String module, Handler handler, boolean fucos) {
        if (module != null) {
            this.mThisModuleName = module;
        }
        Container container = Binders.getContainer(this.mThisModuleName);
        if (container != null) {
            container.registerDataView(this.mThisDataName, string(), this, this, fucos, true);
        }
        this.mHandler = handler;
    }

    public void attach(String module, boolean fucos) {
        attach(module, null, fucos);
    }

    public void attach(String module, Handler handler) {
        attach(module, handler, false);
    }

    public void attach(Handler handler, boolean fucos) {
        attach(null, handler, fucos);
    }

    public void attach(boolean fucos) {
        attach(null, null, fucos);
    }

    public void attach(Handler handler) {
        attach(null, handler, false);
    }

    public void attach() {
        attach(null, null, false);
    }

    public void detach(boolean removeAllObservers) {
        Container container = Binders.getContainer(this.mThisModuleName);
        if (container != null) {
            container.unregisterDataView(this.mThisDataName, string());
        }
        if (removeAllObservers) {
            clearObservers();
        }
    }

    public void detach() {
        detach(false);
    }

    public String name() {
        return this.mThisDataName;
    }

    protected boolean setData(Object value, DataChangedObserver observer, boolean exeObserver, Data param) {
        boolean ret = false;
        String[] strArr = new String[2];
        strArr[0] = "setData:" + this.mThisDataName;
        strArr[1] = value instanceof Packet ? BuildConfig.FLAVOR : "data:" + value;
        Slog.d(strArr);
        if (this.mThisDataName != null) {
            Container container = Binders.getContainer(this.mThisModuleName);
            if (container != null) {
                try {
                    ret = container.setData(this.mThisDataName, string(), observer, exeObserver, this.mEnableLastValue, create(this).copy(value), param);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (!(this == value || equals(value))) {
            Data last;
            if (!(this instanceof Packet) || this.mEnableLastValue) {
                last = clone(this);
            } else {
                last = null;
            }
            if (this.mValue == null) {
                copy(value);
            } else {
                this.mValue = value;
            }
            execLocalObservers(null, exeObserver, this, last, param);
        }
        return ret;
    }

    public boolean set(Data value, DataChangedObserver observer, boolean exeObserver, Data param) {
        return setData(value, observer, exeObserver, param);
    }

    public boolean set(Data value, boolean exeObserver, Data param) {
        return setData(value, null, exeObserver, param);
    }

    public boolean set(Data value, boolean exeObserver) {
        return setData(value, null, exeObserver, null);
    }

    public boolean set(Data value, DataChangedObserver observer) {
        return setData(value, observer, true, null);
    }

    public boolean set(Data value) {
        return setData(value, null, true, null);
    }

    public boolean set(Data value, Data param) {
        return setData(value, null, true, param);
    }

    public void notifyDataChanged(String dataView, boolean onlySelf, Data toCompareValue, Data param) {
        if (this.mThisDataName != null) {
            Container container = Binders.getContainer(this.mThisModuleName);
            if (container != null) {
                try {
                    container.notifyDataChanged(this.mThisDataName, dataView, true, onlySelf, this.mEnableLastValue, toCompareValue, param);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void notifyDataChangedAll(Data toCompareValue, Data param) {
        notifyDataChanged(null, false, toCompareValue, param);
    }

    public void notifyDataChangedAll(boolean compareWithRemote, Data param) {
        notifyDataChangedAll(compareWithRemote ? this : null, param);
    }

    public void notifyDataChangedAll(Data param) {
        notifyDataChangedAll(null, param);
    }

    public void notifyDataChangedAll(boolean compareWithRemote) {
        Data data;
        if (compareWithRemote) {
            data = this;
        } else {
            data = null;
        }
        notifyDataChangedAll(data, null);
    }

    public void notifyDataChangedAll() {
        notifyDataChangedAll(null, null);
    }

    public void notifyDataChanged(boolean onlySelf, Data toCompareValue, Data param) {
        notifyDataChanged(string(), onlySelf, toCompareValue, param);
    }

    public void notifyDataChanged(boolean onlySelf, boolean compareWithRemote, Data param) {
        notifyDataChanged(onlySelf, compareWithRemote ? this : null, param);
    }

    public void notifyDataChanged(boolean onlySelf, Data param) {
        notifyDataChanged(onlySelf, null, param);
    }

    public void notifyDataChanged(boolean onlySelf, boolean compareWithRemote) {
        Data data;
        if (compareWithRemote) {
            data = this;
        } else {
            data = null;
        }
        notifyDataChanged(onlySelf, data, null);
    }

    public void notifyDataChanged(boolean onlySelf) {
        notifyDataChanged(onlySelf, null, null);
    }

    public boolean lock() {
        if (this.mThisDataName != null && this.mLocker == null) {
            Container container = Binders.getContainer(this.mThisModuleName);
            if (container != null) {
                try {
                    this.mLocker = container.lock(this.mThisDataName, new Stub() {
                        public String lockerName() throws RemoteException {
                            return Data.this.string();
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        if (this.mLocker != null) {
            return true;
        }
        return false;
    }

    public boolean unlock() {
        boolean ret = false;
        if (!(this.mThisDataName == null || this.mLocker == null)) {
            Container container = Binders.getContainer(this.mThisModuleName);
            if (container != null) {
                try {
                    ret = container.unlock(this.mThisDataName, this.mLocker);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    public boolean unlockAll() {
        boolean ret = false;
        if (this.mThisDataName != null) {
            Container container = Binders.getContainer(this.mThisModuleName);
            if (container != null) {
                try {
                    ret = container.unlockAll(this.mThisDataName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    protected boolean syncData(DataChangedObserver observer, boolean exeObserver, Data param) {
        boolean ret = false;
        execLocalObservers(null, exeObserver, this, this, param);
        if (this.mThisDataName != null) {
            Container container = Binders.getContainer(this.mThisModuleName);
            if (container != null) {
                try {
                    ret = container.setData(this.mThisDataName, string(), observer, exeObserver, this.mEnableLastValue, this, param);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    public boolean syncData(boolean exeObserver, Data param) {
        return syncData(null, exeObserver, param);
    }

    public boolean syncData(boolean exeObserver) {
        return setData(this, null, exeObserver, null);
    }

    public boolean syncData(DataChangedObserver observer) {
        return syncData(observer, true, null);
    }

    public boolean syncData() {
        return syncData(null, true, null);
    }

    public boolean syncData(Data param) {
        return syncData(null, true, param);
    }

    public <T extends Data> T enableLastValue(boolean have) {
        this.mEnableLastValue = have;
        return (T)this;
    }

    public boolean addObserver(boolean execFirst, OBSERVER observer) {
        boolean ret = false;
        if (observer != null) {
            synchronized (this.mLocalObservers) {
                ret = this.mLocalObservers.add(observer);
            }
            Container container = Binders.getContainer(this.mThisModuleName);
            if (container != null) {
                Data packet = container.getData(this.mThisDataName, this);
                if (!equals(packet)) {
                    Data last;
                    if (!(this instanceof Packet) || this.mEnableLastValue) {
                        last = clone(this);
                    } else {
                        last = null;
                    }
                    if (packet == null || packet.mValue == null) {
                        copy(packet);
                    } else {
                        this.mValue = packet.mValue;
                    }
                    execLocalObservers(null, true, this, last, null);
                } else if (execFirst) {
                    execLocalObserver(observer, this, this, null);
                }
            }
        }
        return ret;
    }

    public boolean addObserver(OBSERVER observer) {
        return addObserver(false, observer);
    }

    public boolean removeObserver(OBSERVER observer) {
        boolean ret = false;
        if (observer != null) {
            synchronized (this.mLocalObservers) {
                ret = this.mLocalObservers.remove(observer);
            }
        }
        return ret;
    }

    public void clearObservers() {
        synchronized (this.mLocalObservers) {
            this.mLocalObservers.clear();
        }
    }

    protected Object onExecLocalObserver(OBSERVER observer, @NonNull Data curr, @NonNull Data last, Data param) {
        return null;
    }

    protected Object execLocalObserver(OBSERVER observer, @NonNull Data curr, @NonNull Data last, Data param) {
        return onExecLocalObserver(observer, curr, last, param);
    }

    private void doExecLocalObservers(@Nullable OBSERVER observer, boolean execObserver, @NonNull Data curr, @NonNull Data last, @Nullable Data param) {
        if (observer != null) {
            execLocalObserver(observer, curr, last, param);
        }
        if (execObserver) {
            synchronized (this.mLocalObservers) {
                for (OBSERVER iInterface : this.mLocalObservers) {
                    execLocalObserver(iInterface, curr, last, param);
                }
            }
        }
    }

    protected void execLocalObservers(@Nullable OBSERVER observer, boolean execObserver, @NonNull Data curr, @NonNull Data last, @Nullable Data param) {
        Slog.d("start to exec local observers:", "name" + this.mUniqueName);
        if (this.mHandler != null) {
            final Data finalCurr = clone(curr);
            final OBSERVER observer2 = observer;
            final boolean z = execObserver;
            final Data data = last;
            final Data data2 = param;
            this.mHandler.post(new Runnable() {
                public void run() {
                    Data.this.doExecLocalObservers(observer2, z, finalCurr, data, data2);
                }
            });
        } else {
            doExecLocalObservers(observer, execObserver, curr, last, param);
        }
        Slog.d("exec local observers finish:");
    }

    public int describeContents() {
        return 0;
    }

    public static synchronized <T extends Data> T clone(T value) {
        Data data;
        synchronized (Data.class) {
            Object obj;
            if (value instanceof Data) {
                Constructor constructor = null;
                try {
                    constructor = value.getClass().getDeclaredConstructor(new Class[]{Parcel.class});
                    constructor.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    new CloneNotSupportedException("Make sure the 'protected " + value.getClass().getSimpleName() + "(Parcel in, int flag)'" + "constructor has been implemented:" + value.getClass().getName()).printStackTrace();
                }
                if (constructor != null) {
                    try {
                        Object obj2 = constructor.newInstance(new Object[]{(Parcel) null});
                        if (obj2 instanceof Data) {
                            ((Data) obj2).mThisDataName = value.mThisDataName;
                            ((Data) obj2).copy(value);
                        }
                        obj = obj2;
                    } catch (InstantiationException e2) {
                        e2.printStackTrace();
                        obj = null;
                    } catch (IllegalAccessException e3) {
                        e3.printStackTrace();
                        obj = null;
                    } catch (InvocationTargetException e4) {
                        e4.printStackTrace();
                        obj = null;
                    }
                    data = (Data) obj;
                }
            }
            obj = null;
            data = (Data) obj;
        }
        return (T)data;
    }

    protected Data create(Object value) {
        Data pack = null;
        try {
            Constructor constructor = getClass().getDeclaredConstructor(new Class[]{Parcel.class});
            constructor.setAccessible(true);
            if (constructor != null) {
                Object obj = constructor.newInstance(new Object[]{(Parcel) null});
                if (obj instanceof Data) {
                    pack = (Data) obj;
                    if (value instanceof Data) {
                        pack.mThisDataName = ((Data) value).mThisDataName;
                    } else {
                        pack.mThisDataName = this.mThisDataName;
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            new CloneNotSupportedException("Make sure the 'protected " + getClass().getSimpleName() + "(Parcel in, int flag)'" + "constructor has been implemented:" + getClass().getName()).printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InstantiationException e3) {
            e3.printStackTrace();
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
        }
        return pack;
    }

    protected Data(Parcel in, int flags) {
        this.mSupportRemote = true;
        this.mEnableLastValue = false;
        this.mUniqueName = new Object();
        this.mLocalObservers = new ArrayList();
        if (in != null && (flags & 1) != 0) {
            in.readString();
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null && (flags & 1) != 0) {
            dest.writeString(getClass().getName());
        }
    }

    public String string() {
        return this.mUniqueName.toString();
    }

    public Data copy(Object value) {
        if (this.mValue instanceof Data) {
            ((Data) this.mValue).copy(value);
        } else {
            this.mValue = value;
        }
        return this;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            if (this.mValue != null) {
                z = false;
            }
            return z;
        } else if (!obj.getClass().isArray()) {
            return obj.equals(this.mValue);
        } else {
            if (this.mValue == null || !this.mValue.getClass().isArray()) {
                return false;
            }
            int len = Array.getLength(obj);
            if (len != Array.getLength(this.mValue)) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                Object a = Array.get(obj, i);
                Object b = Array.get(obj, i);
                if (a == null) {
                    if (b != null) {
                        return false;
                    }
                } else if (b == null || !a.equals(b)) {
                    return false;
                }
            }
            return true;
        }
    }
}
