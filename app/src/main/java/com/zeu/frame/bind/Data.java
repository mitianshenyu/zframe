package com.zeu.frame.bind;

import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zeu.frame.bind.callback.DataViewCallback;
import com.zeu.frame.bind.comm.ILockerSource;
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

/**
 * Created by zeu on 2016/11/29.
 * 此为数据视图
 */
public class Data<OBSERVER> implements DataViewCallback, Parcelable {
    /**
     * 所有的数据, 先设置本地的, 然后再将数据同步到远程, 修改远程的， 如果 本地和远程断开连接, 则在连接上远程的时候, 以远程数据为基准
     */
    protected Object mValue;
    protected String mLocker;
    protected Handler mHandler;
    protected String mThisDataName;
    protected String mThisModuleName;
    //是否支持IBinder的远程数据(AIDL)
    protected boolean mSupportRemote = true;
    //默认不计算LastValue, 应为LastValue, 在传递大数据的时候, 容易造成GC卡顿
    protected boolean mEnableLastValue = false;
    protected Object mUniqueName = new Object();
    OnLocalAttachListener mOnLocalAttachListener;
    OnRemoteAttachListener mOnRemoteAttachListener;
    protected List<OBSERVER> mLocalObservers = new ArrayList<>(); //所有注册的, 将在添加到本地

    public Data() {
    }

    public Data(String name) {
        this("", name);
    }

    /**
     * 默认不连接到后台
     * @param name
     * @param value
     */
    protected Data(String name, Object value) {
        this("", name, value, false);
    }

    protected Data(String name, Object value, boolean attach) {
        this("", name, value, attach, false);
    }

    /**
     * @param name
     * @param value 值
     * @param attach 是否连接, 默认不连接
     * @param focus 以此数据为准, 连接上远程的时候, 会将本地址设置到远程
     */
    protected Data(String name, Object value, boolean attach, boolean focus) {
        this("", name, value, attach, focus);
    }

    /**
     * 默认不连接到后台
     * @param name
     * @param value
     */
    protected Data(String module, String name, Object value) {
        this(module, name, value, false);
    }

    protected Data(String module, String name, Object value, boolean attach) {
        this(module, name, value, attach, false);
    }

    /**
     * @param name
     * @param value 值
     * @param attach 是否连接, 默认不连接
     * @param focus 以此数据为准, 连接上远程的时候, 会将本地址设置到远程
     */
    protected Data(String module, String name, Object value, boolean attach, boolean focus) {
        this(module, name);
        copy(value);
        if (attach) {
            attach(focus);
        }
    }

    public Data(String module, String name) {
        mThisDataName = name;
        mThisModuleName = module;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    /**
     * 同步数据
     * @param dataView 与yesSelfNoOther一起判断是否执行监听
     * @param yesSelfNoOther
     * @param curr
     * @param param
     * @param execObserver
     * @return
     * @throws RemoteException
     */

    @Override
    public boolean onChanged(String dataName, String dataView, boolean yesSelfNoOther, Data curr, Data param, boolean execObserver) {
        Slog.d("sync from remote:", "data:"+dataName, "view:"+dataView, "yesSelfNoOther:"+yesSelfNoOther, (curr instanceof Packet)?"":("data:"+curr));
        if (null == dataView || (yesSelfNoOther && dataView.equals(string())) || (!yesSelfNoOther && !dataView.equals(string()))) {
            if (execObserver) {
                Data last = (!(this instanceof Packet) || mEnableLastValue) ? clone(this) : null;
                if (null != curr && null != curr.mValue) {
                    mValue = curr.mValue; //对象数据, 只传递引用
                } else {
                    copy(curr);  //基本数据, 复制数据
                }
                execLocalObservers(null, true, Data.this, last, param);
            } else {
                if (null != curr && null != curr.mValue) {
                    mValue = curr.mValue; //对象数据, 只传递引用
                } else {
                    copy(curr);  //基本数据, 复制数据
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onLocalAttach() {
        if (null != mOnLocalAttachListener) {
            mOnLocalAttachListener.onAttached();
        }
    }

    @Override
    public void onLocalDetach() {
        if (null != mOnLocalAttachListener) {
            mOnLocalAttachListener.onDetached();
        }
    }

    @Override
    public void onRemoteAttach(IRemoter remoter) {
        if (null != mOnRemoteAttachListener) {
            mOnRemoteAttachListener.onAttached(remoter);
        }
    }

    @Override
    public void onRemoteDetach(IRemoter remoter) {
        if (null != mOnRemoteAttachListener) {
            mOnRemoteAttachListener.onDetached(remoter);
        }
    }

    public void setOnLocalAttachListener(OnLocalAttachListener listener) {
        mOnLocalAttachListener = listener;
    }

    public void setOnRemoteAttachListener(OnRemoteAttachListener listener) {
        mOnRemoteAttachListener = listener;
    }

    public void attach(String module, Handler handler, boolean fucos) {
        if (null != module) {
            mThisModuleName = module;
        }
        Binders.Container container = Binders.getContainer(mThisModuleName);
        if (null != container) {
            container.registerDataView(mThisDataName, string(), this, this, fucos, true);
        }
        mHandler = handler;
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
        Binders.Container container = Binders.getContainer(mThisModuleName);
        if (null != container) {
            container.unregisterDataView(mThisDataName, string());
        }
        if (removeAllObservers) {
            clearObservers();
        }
    }

    public void detach() {
        detach(false);
    }

    public String name() {
        return mThisDataName;
    }

    /**
     * 设置值
     * @param value 值
     * @param observer 临时监听
     * @param exeObserver 不执行注册过的监听
     * @return
     */
    protected boolean setData(Object value, DataChangedObserver observer, boolean exeObserver, Data param) {
        boolean ret = false;
        Slog.d("setData:"+mThisDataName, (value instanceof Packet)?"":("data:"+value));
        if (null != mThisDataName) {//先将数据, 与本地数据仓库, 比对之后, 再决定是否同步数据和执行监听
            Binders.Container container = Binders.getContainer(mThisModuleName);
            if (null != container) {
                try {
                    ret = container.setData(mThisDataName, string(), observer, exeObserver, mEnableLastValue, create(this).copy(value), param);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (this != value && !equals(value)) {//没有设置字符串, 当作本地对象, 不相等 进入监听
            Data last = (!(this instanceof Packet) || mEnableLastValue) ? clone(this) : null;
            if (null == mValue) {
                copy(value); //基本数据, 复制数据
            } else {
                mValue = value; //对象数据, 只传递引用
            }
            execLocalObservers(null, exeObserver, Data.this, last, param);
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
    /**
     * 通知数据改变, 不会同步本地数据, 只会执行监听
     * @param dataView
     *      仓库服务端,通过一个String,维护一个对象,其可以与多个应用共享,而每个应用可以控制数据的改变通知
     *      dataView == null : 将会通知所有对象
     *      dataView != null :
     *          selfOrOther == true : 通知自己
     *          selfOrOther == false: 通知除自己之外的对象
     * @param onlySelf
     *          false : 通知除自己之外的对象
     *          true: 通知自己
     * @param toCompareValue
     *          在通知的时候可以传入比对的值,如果不想等则执行通知
     * @param param
     *          传入参数
     */
    public void notifyDataChanged(String dataView, boolean onlySelf, Data toCompareValue, Data param) {
        if (null != mThisDataName) {
            Binders.Container container = Binders.getContainer(mThisModuleName);
            if (null != container) {
                try {
                    container.notifyDataChanged(mThisDataName, dataView, true, onlySelf, mEnableLastValue, toCompareValue, param);
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
        notifyDataChangedAll(compareWithRemote?this:null, param);
    }

    public void notifyDataChangedAll(Data param) {
        notifyDataChangedAll(null, param);
    }

    public void notifyDataChangedAll(boolean compareWithRemote) {
        notifyDataChangedAll(compareWithRemote?this:null, null);
    }

    public void notifyDataChangedAll() {
        notifyDataChangedAll(null, null);
    }

    public void notifyDataChanged(boolean onlySelf, Data toCompareValue, Data param) {
        notifyDataChanged(string(), onlySelf, toCompareValue, param);
    }

    public void notifyDataChanged(boolean onlySelf, boolean compareWithRemote, Data param) {
        notifyDataChanged(onlySelf, compareWithRemote?this:null, param);
    }

    public void notifyDataChanged(boolean onlySelf, Data param) {
        notifyDataChanged(onlySelf, null, param);
    }

    public void notifyDataChanged(boolean onlySelf, boolean compareWithRemote) {
        notifyDataChanged(onlySelf, compareWithRemote?this:null, null);
    }

    public void notifyDataChanged(boolean onlySelf) {
        notifyDataChanged(onlySelf, null, null);
    }

    public boolean lock() {
        if (null != mThisDataName && null == mLocker) {
            Binders.Container container = Binders.getContainer(mThisModuleName);
            if (null != container) {
                try {
                    mLocker = container.lock(mThisDataName, new ILockerSource.Stub() {
                        @Override
                        public String lockerName() throws RemoteException {
                            return Data.this.string();
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return (null != mLocker);
    }

    public boolean unlock() {
        boolean ret = false;
        if (null != mThisDataName && null != mLocker) {
            Binders.Container container = Binders.getContainer(mThisModuleName);
            if (null != container) {
                try {
                    ret = container.unlock(mThisDataName, mLocker);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    public boolean unlockAll() {
        boolean ret = false;
        if (null != mThisDataName) {
            Binders.Container container = Binders.getContainer(mThisModuleName);
            if (null != container) {
                try {
                    ret = container.unlockAll(mThisDataName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    /**
     * 将数据同步到本地和远程, 会出本地和远程的监听
     * @param exeObserver
     * @param param
     * @return
     */
    protected boolean syncData(DataChangedObserver observer, boolean exeObserver, Data param) {
        boolean ret = false;
        //正常运行的时候, 以本地数据为准, 当连接到远程的时候, 将远程数据同步到本地,
        execLocalObservers(null, exeObserver, Data.this, this, param);

        if (null != mThisDataName) {
            Binders.Container container = Binders.getContainer(mThisModuleName);
            if (null != container) {
                //不管数据想不想等, 数据改变才设置远程监听
                try {
                    ret = container.setData(mThisDataName, string(), observer, exeObserver, mEnableLastValue, this, param);
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

    /**
     * 是LastValue有效, LastValue 在处理大数据的时候, 频繁的申请和释放内存, 使得GC清理频繁, 导致软件卡顿
     * @param have
     * @param <T>
     * @return
     */
    public <T extends Data> T enableLastValue(boolean have) {
        mEnableLastValue = have;
        return (T)this;
    }

    /**
     * 绑定监听,先将监听添加到本地, 然后在注册到Remoter, 当对象销毁的时候, 注销远程监听
     * @param observer
     * @return
     */
    public boolean addObserver(boolean execFirst, OBSERVER observer) {
        boolean ret = false;
        if (null != observer) {
            synchronized (mLocalObservers) {
                ret = mLocalObservers.add(observer);
            }

            Binders.Container container = Binders.getContainer(mThisModuleName);
            if (null != container) {
                Data packet = container.getData(mThisDataName, this);
                if (!equals(packet)) {
                    Data last = (!(this instanceof Packet) || mEnableLastValue) ? clone(this) : null;
                    if (null != packet && null != packet.mValue) {
                        mValue = packet.mValue;
                    } else {
                        copy(packet);
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
        if (null != observer) {
            synchronized (mLocalObservers) {
                ret = mLocalObservers.remove(observer);
            }
        }
        return ret;
    }

    /**
     * 清除所有的监听, 以防止内存泄漏
     */
    public void clearObservers() {
        synchronized (mLocalObservers) {
            mLocalObservers.clear();
        }
    }

    protected Object onExecLocalObserver(OBSERVER observer, @NonNull Data curr, @NonNull Data last, Data param) {
        return null;
    }

    protected Object execLocalObserver(OBSERVER observer, @NonNull Data curr, @NonNull Data last, Data param) {
        return onExecLocalObserver(observer, curr, last, param);
    }

    private void doExecLocalObservers(@Nullable OBSERVER observer, boolean execObserver, @NonNull Data curr, @NonNull Data last, @Nullable Data param) {
        //先同步远程的值
        if (null != observer) {
            execLocalObserver(observer, curr, last, param);
        }
        //同步本地值
        if (execObserver) {
            synchronized (mLocalObservers) {
                for (OBSERVER iInterface : mLocalObservers) {
                    execLocalObserver(iInterface, curr, last, param);
                }
            }
        }
    }

    protected void execLocalObservers(@Nullable final OBSERVER observer, final boolean execObserver, @NonNull Data curr, @NonNull final Data last, @Nullable final Data param) {
        Slog.d("start to exec local observers:", "name"+mUniqueName);
        if (null != mHandler) {
            //异步执行, 在执行监听之前可能数据已经改变, 所以必须为克隆, 不能直接传递this
            final Data finalCurr = clone(curr);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    doExecLocalObservers(observer, execObserver, finalCurr, last, param);
                }
            });
        } else {
            doExecLocalObservers(observer, execObserver, curr, last, param);
        }
        Slog.d("exec local observers finish:");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     *  crate a Parcel by clone(no need be overloaded)
     *  new Packet(null).copy();
     * @param value
     * @return
     */
    public synchronized static <T extends Data> T clone(T value) {
        Object obj = null;
        if (value instanceof Data) {
            Constructor constructor = null;
            try {
                constructor = value.getClass().getDeclaredConstructor(Parcel.class);
                constructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                new CloneNotSupportedException("Make sure the 'protected " + value.getClass().getSimpleName() + "(Parcel in, int flag)'" + "constructor has been implemented:" + value.getClass().getName()).printStackTrace();
            }

            if (null != constructor) {
                try {
                    obj = constructor.newInstance((Parcel) null);
                    if (obj instanceof Data) {
                        ((T) obj).mThisDataName = (value).mThisDataName;
                        ((T) obj).copy(value);
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return ((T) obj);
    }

    protected Data create(Object value) {
        Data pack = null;
        try {
            Constructor constructor = getClass().getDeclaredConstructor(Parcel.class);
            constructor.setAccessible(true);
            if (null != constructor) {
                Object obj = constructor.newInstance((Parcel) null);
                if (obj instanceof Data) {
                    pack = (Data)obj;
                    if (value instanceof Data) {
                        pack.mThisDataName = ((Data) value).mThisDataName;
                    } else {
                        pack.mThisDataName = this.mThisDataName;
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            new CloneNotSupportedException("Make sure the 'protected "+getClass().getSimpleName()+"(Parcel in, int flag)'"+"constructor has been implemented:"+getClass().getName()).printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return pack;
    }

    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return (Data) Binders.createObjFromParcel(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

    /**
     * (Must be overloaded)
     * @param in
     */
    protected Data(Parcel in, int flags) {
        if ((null != in) && (flags & 1) != 0) {
            in.readString(); //预先将class读取,但不保存
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
    public void writeToParcel(Parcel dest, int flags) {
        if (null != dest && (flags & 1) != 0) {
            dest.writeString(getClass().getName());
        }
    }

    public String string() {
        return mUniqueName.toString();
    }

    /**
     * copy content from parcel(Must be overloaded)
     * @param value
     */
    public Data copy(Object value) {
        if (mValue instanceof Data) {
            ((Data) mValue).copy(value);
        } else {
            mValue = value;
        }
        return this;
    }

    /**
     * (Must be overloaded
     */
    @Override
    public boolean equals(Object obj) {
        if (null != obj) {
            if (obj.getClass().isArray()) {
                if (null != mValue && mValue.getClass().isArray()) {
                    int len = Array.getLength(obj);
                    if (len == Array.getLength(mValue)) {
                        for (int i = 0; i < len; i++) {
                            Object a = Array.get(obj, i);
                            Object b = Array.get(obj, i);
                            if (null == a) {
                                if (b != null) {
                                    return false;
                                }
                            } else {
                                if (b == null || !a.equals(b)) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                }
            } else {
                return obj.equals(mValue);
            }
        } else {
            return mValue == null;
        }
        return false;
    }
}
