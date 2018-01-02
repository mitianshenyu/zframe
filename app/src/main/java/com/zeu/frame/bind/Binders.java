package com.zeu.frame.bind;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;

import com.zeu.frame.bind.Data;
import com.zeu.frame.bind.callback.DataViewCallback;
import com.zeu.frame.bind.callback.DeadObjectCallback;
import com.zeu.frame.bind.callback.IDataSyncCallback;
import com.zeu.frame.bind.callback.DataSyncCallback;
import com.zeu.frame.bind.comm.ILockerSource;
import com.zeu.frame.bind.comm.IRemoter;
import com.zeu.frame.bind.comm.Model;
import com.zeu.frame.bind.listener.OnRemoteAttachListener;
import com.zeu.frame.bind.observer.IDataChangedObserver;
import com.zeu.frame.bind.observer.IPacketObserver;
import com.zeu.frame.bind.Packet;

import com.zeu.frame.log.Slog;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by devil on 2016/11/30.
 * 1.此框架不作其他复杂的逻辑处理, 只是观察数据的改变, 数据改变了就会进入监听,
 * 至于什么原因导致的数据改变, 请自己添加对应的变量或者逻辑来判断
 *
 * 2.整个java都不适合数据的创建,立马释放,多次重复此过程, 而此框架在PacketArray和? extends Packet的拷贝过程中,
 * 会clone和拷贝多次, 会让变量刚创建又释放, 导致GC清扫和释放内存,打印出Suspending all threads took的警告
 * 解决方法: 使用简单变量或者空变量(Packet)的notifyDataChanged(Data) or notifyDataChangedAll(Data)来传递大数据, 这样可以避免频繁的申请和释放内存
 */

public class Binders {
    static Map<String, Container> sContainer = new HashMap<>();
    //如果在同一个进程, 服务中的sRemoter和界面中的sRemoter为同一个对象, 所以建议在服务中重新new一个Remoter
    public static Container getContainer(String moduleName, boolean createIfNotExist) {
        Container container;
        if (null != moduleName) {
            synchronized (sContainer) {
                container = sContainer.get(moduleName);
            }
            if (null == container && createIfNotExist) {
                container = new Container();
                synchronized (sContainer) {
                    sContainer.put(moduleName, container);
                }
            }
            return container;
        }
        return null;
    }

    public static Container getContainer(String moduleName) {
        return getContainer(moduleName, true);
    }

    public static Container getContainer(boolean createIfNotExist) {
        return getContainer("", createIfNotExist);
    }

    public static Container getContainer() {
        return getContainer("", true);
    }

    public static boolean attach(Context context, String moduleName, String packName, String className, String receiverAction) {
        Container container = getContainer(moduleName);
        if (null != container) {
            container.setAttachServiceIntent(packName, className);
            container.setAttachServiceReadyFilter(receiverAction);
            return container.attachService(context);
        }
        return false;
    }

    public static boolean attach(Context context, String moduleName, ComponentName componentName, String receiverAction) {
        Container container = getContainer(moduleName);
        if (null != container) {
            container.setAttachServiceIntent(componentName);
            container.setAttachServiceReadyFilter(receiverAction);
            return container.attachService(context);
        }
        return false;
    }

    public static boolean attach(Context context, String moduleName, Intent serviceIntent, String receiverAction) {
        Container container = getContainer(moduleName);
        if (null != container) {
            container.setAttachServiceIntent(serviceIntent);
            container.setAttachServiceReadyFilter(receiverAction);
            return container.attachService(context);
        }
        return false;
    }

    public static void detach(String moduleName) {
        Container container = getContainer(moduleName);
        if (null != container) {
            container.detachService();
        }
    }

    public static boolean attach(Context context, String packName, String className, String receiverAction) {
        return attach(context, "", packName, className, receiverAction);
    }

    public static boolean attach(Context context, ComponentName componentName, String receiverAction) {
        return attach(context, "", componentName, receiverAction);
    }

    public static boolean attach(Context context, Intent serviceIntent, String receiverAction) {
        return attach(context, "", serviceIntent, receiverAction);
    }

    public static void detach() {
        detach("");
    }

    protected static Intent getServiceIntent(Context context, Class<?> cls) {
        Intent intent = null;
        if (null != context && null != cls) {
            intent = new Intent(context, cls).setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }
        return intent;
    }

    protected static Intent getServiceIntent(ComponentName componentName) {
        Intent intent = null;
        if (null != componentName) {
            intent = new Intent().setComponent(componentName).setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }
        return intent;
    }

    /**
     * 启动本地或远程服务
     * @param context
     * @param cls
     * @return
     */
    public static Intent startService(Context context,  Class<?> cls) {
        Intent intent = getServiceIntent(context, cls);
        if (null != intent) {
            try {
                context.startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return intent;
    }

    public static Intent startService(Context context, Intent intent) {
        if (null != intent) {
            try {
                context.startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return intent;
    }

    /**
     * 启动本地或远程服务
     * @param context
     * @param componentName
     * @return
     */
    public static Intent startService(Context context, ComponentName componentName) {
        Intent intent = getServiceIntent(componentName);
        if (null != intent) {
            try {
                context.startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return intent;
    }

    /**
     * 启动本地或远程服务
     * @param context
     * @param pkg
     * @param cls
     * @return
     */
    public static Intent startService(Context context, String pkg, String cls) {
        return (null != pkg && null != cls) ? startService(context, new ComponentName(pkg, cls)) : null;
    }

    /**
     * 停止本地或远程服务,请确保和启动时候的intent一致
     * @param context
     * @return
     */
    public static boolean stopService(Context context, Intent intent) {
        if (null != context && null != intent) {
            try {
                context.stopService(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean stopService(Context context, ComponentName componentName) {
        Intent intent = getServiceIntent(componentName);
        if (null != context && null != intent) {
            try {
                context.stopService(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean stopService(Context context, Class clas) {
        Intent intent = getServiceIntent(context, clas);
        if (null != context && null != intent) {
            try {
                context.stopService(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 绑定本地或远程服务
     * @param context
     * @param cls
     * @param connection
     * @return
     */
    public static boolean bindService(Context context, Class<?> cls, ServiceConnection connection) {
        boolean ret = false;
        if (null != context && null != cls && null != connection) {
            try {
                ret = context.bindService(new Intent(context, cls).setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES), connection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 绑定本地或远程服务
     * @param context
     * @param componentName
     * @param connection
     * @return
     */
    public static boolean bindService(Context context, ComponentName componentName, ServiceConnection connection) {
        boolean ret = false;
        if (null != componentName && null != context) {
            try {
                ret = context.bindService(new Intent().setComponent(componentName).setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES), connection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 绑定本地或远程服务
     * @param context
     * @param pkg
     * @param cls
     * @param connection
     * @return
     */
    public static boolean bindService(Context context, String pkg, String cls, ServiceConnection connection) {
        return (null != pkg && null != cls) ? bindService(context, new ComponentName(pkg, cls), connection) : false;
    }

    public static boolean unbindService(Context context, ServiceConnection connection) {
        boolean ret = false;
        if (null != context) {
            try {
                context.unbindService(connection);
                ret = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static void writeArrayParcel(android.os.Parcel dest, Object value, int flags) {
        if (value != null) {
            int N = Array.getLength(value);
            dest.writeInt(N); //长度
            dest.writeString(value.getClass().getComponentType().getName());
            for (int i = 0; i < N; i++) {
                Object item = Array.get(value, i);
                if (item instanceof Packet) {
                    dest.writeInt(1); //写方向
                    ((Packet)item).writeToParcel(dest, flags); //写入值
                } else {
                    dest.writeInt(0);
                }
            }
        } else {
            dest.writeInt(-1);
        }
    }

    /**
     * 创建Parcel对象
     * 必须将parcelable对象放在函数的入口参数的最后, 这样才能确保读完入口参数(包括入口参数的方向)后, 进入函数中第一个读取的是类名
     * 必须在写入parcelable数据的时候最先写入类名
     * 1.u must write parcel class name at first
     * 2.u must put the parcelable variable at last of the funcition
     @Override
     public void writeToParcel(android.os.Parcel dest, int flags) {
     dest.writeString(getClass().getName()); //必须最先写类名
     dest.writeString(mThisDataName);
     dest.writeString(mModule);
     }
     Parcel getParcel(String dataName, in Parcel defValue); //parcelable类型必须放最后
      * @param parcel
     * @return
     */
    public static Object createObjFromParcel(android.os.Parcel parcel) {
        Object obj = null;
        String className = parcel.readString();//读取类名字
        if (null != className) {
            try {
                Class clas = Class.forName(className);
                Constructor constructor = null;
                try {
                    constructor = clas.getDeclaredConstructor(android.os.Parcel.class);
                    constructor.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    try {
                        constructor = clas.getConstructor(android.os.Parcel.class);
                    } catch (NoSuchMethodException e1) {
                        e1.printStackTrace();
                    }
                }

                if (null != constructor) {
                    obj = constructor.newInstance(parcel);
                }
            }catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    public static Object createArrayFromParcel(android.os.Parcel parcel) {
        Object value = null;
        int len = parcel.readInt();
        if (len > 0) {
            try {
                String clsName = parcel.readString();
                Class<?> cls = Class.forName(clsName);
                value = Array.newInstance(cls, len);
                for (int i = 0; i < len; i++) {
                    if (parcel.readInt() == 1) {
                        Array.set(value, i, Binders.createObjFromParcel(parcel));
                    } else {
                        Array.set(value, i, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * 同步到数据视图Views
     * @param entity 执行的数据实体
     * @param specialDataView 指定同步的数据视图
     * @param yesSelfNoOther 仅仅自己, 当specialDataView不为null的时候有效
     * @param exceptDataView 除外的数据视图, 当specialDataView为null的时候有效
     * @param execObserver
     * @param toCompareValue
     * @param param
     */
    protected static void syncToDataViews(Entity entity, String specialDataView, boolean yesSelfNoOther, String exceptDataView, boolean execObserver, Data toCompareValue, Data param) {
        Slog.d("sync to views(start) "+entity);
        if (null != entity) {
            Data data;
            String name;
            Map<String, DataViewCallback> dataViewCallbacks;
            synchronized (entity) {
                name = entity.mName;
                data = entity.mData;
                dataViewCallbacks = entity.mDataViewCallbacks;
            }
            if (null != data && (null == toCompareValue || !data.equals(toCompareValue))) {
                synchronized (entity) {
                    logd("开始同步数据到View:" + name, "DataView=" + dataViewCallbacks);
                    synchronized (dataViewCallbacks) {
                        if (null != specialDataView) {
                            if (yesSelfNoOther) {
                                DataViewCallback callback = dataViewCallbacks.get(specialDataView);
                                if (null != callback) {
                                    callback.onChanged(name, specialDataView, yesSelfNoOther, data, param, execObserver);
                                }
                            } else {
                                for (Map.Entry<String, DataViewCallback> entry : dataViewCallbacks.entrySet()) {
                                    if (null != entry) {
                                        if (null != entry.getValue() && !specialDataView.equals(entry.getKey())) {
                                            entry.getValue().onChanged(name, specialDataView, yesSelfNoOther, data, param, execObserver);
                                        }
                                    }
                                }
                            }
                        } else {
                            for (Map.Entry<String, DataViewCallback> entry : dataViewCallbacks.entrySet()) {
                                if (null != entry && null != entry.getValue() && !entry.getKey().equals(exceptDataView)) {
                                    entry.getValue().onChanged(name, specialDataView, yesSelfNoOther, data, param, execObserver);
                                }
                            }
                        }
                    }
                    logd("同步数据到Views完成");
                }
            }
        } else{
            logd("对象相等, 不执行监听");
        }
        Slog.d("sync to views(finish)");
    }

    /**
     * 同步容器内的所有数据
     * @param entity
     * @param specialDataView
     * @param yesSelfNoOther
     * @param exceptDataView
     * @param execObserver
     * @param toCompareValue
     * @param param
     */
    protected static void syncToRemoteContainer(Entity entity, String specialDataView, boolean yesSelfNoOther, String exceptDataView, boolean execObserver, Data toCompareValue, Data param) {
        if (null != entity) {
            Data data;
            String name;
            Map<String, IDataSyncCallback> dataSyncCallbacks;
            synchronized (entity) {
                name = entity.mName;
                data = entity.mData;
                dataSyncCallbacks = entity.mDataSyncCallbacks;
            }
            Slog.d("sync to remote(start) : "+name);
            synchronized (dataSyncCallbacks) {
                List<String> removes = new ArrayList<>();
                for (Map.Entry<String, IDataSyncCallback> entry: dataSyncCallbacks.entrySet()) {
                    if (null != entry && null != entry.getValue() && !entry.getKey().equals(exceptDataView)) {
                        try {
                            entry.getValue().onSync(name, specialDataView, yesSelfNoOther, data, toCompareValue, param, execObserver);
                        } catch (DeadObjectException e) {
                            e.printStackTrace();
                            removes.add(entry.getKey());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }

                for (String remove : removes) {
                    dataSyncCallbacks.remove(remove);
                }
            }
            Slog.d("sync to remote(finish)");
        }
    }

    protected static class Attacher extends BroadcastReceiver implements ServiceConnection {
        Context mContext;
        Container mContainer;
        Intent mRemoteServiceAttachIntent;
        IntentFilter mRemoteServiceReadyFilter;

        public Attacher(Container container) {
            mContainer = container;
        }

        public void setAttachServiceIntent(Intent intent) {
            mRemoteServiceAttachIntent = intent;
        }

        public void setAttachServiceIntent(ComponentName componentName) {
            if (null != componentName) {
                mRemoteServiceAttachIntent = new Intent().setComponent(componentName);
            }
        }

        public void setAttachServiceIntent(String packName, String remoteServiceAttachClass) {
            if (null != packName && null != remoteServiceAttachClass) {
                ComponentName componentName = new ComponentName(packName, remoteServiceAttachClass);
                mRemoteServiceAttachIntent = new Intent().setComponent(componentName);
            }
        }

        public void setAttachServiceReadyFilter(String serviceReadyReceiverAction) {
            if (null != serviceReadyReceiverAction) {
                mRemoteServiceReadyFilter = new IntentFilter(serviceReadyReceiverAction);
            }
        }
        public void setAttachServiceReadyFilter(IntentFilter filter) {
            mRemoteServiceReadyFilter = filter;
        }

        protected boolean doAttachRemote(Context context) {
            boolean ret = false;
            Slog.d("尝试连接远程服务");
            if (mRemoteServiceAttachIntent != null) {
                try {
                    ret = context.bindService(mRemoteServiceAttachIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES), this, Context.BIND_AUTO_CREATE);
                } catch (Exception e) {
                    Slog.d("连接远程服务失败"+e.getMessage());
                }
            } else {
                Slog.d("远程服务Intent为空");
            }
            return ret;
        }

        protected void doDetachRemote() {
            Slog.d("尝试断开远程服务");
            if (null != mContext) {
                Slog.d("断开所有远程数据");
                if (null != mContainer) {
                    mContainer.detachRemoter();
                }
                try {
                    Slog.d("断开远程服务连接");
                    mContext.unbindService(this);
                } catch (Exception e) {
                    Slog.d("断开远程服务失败:"+e.getMessage());
                } finally {

                }
            } else {
                Slog.d("尝试断开远程服务");
            }
        }

        public boolean attachService(Context context) {
            boolean ret = false;
            if (null != context) {
                mContext = context;
                ret = this.doAttachRemote(context);
                if (null != mRemoteServiceReadyFilter) {
                    try {
                        context.registerReceiver(this, mRemoteServiceReadyFilter);
                    } catch (Exception e) {
                        Slog.d("attachRemote", e.getMessage());
                    }
                }
            }
            return ret;
        }

        public void detachService() {
            if (null != mContext) {
                doDetachRemote();
                try {
                    mContext.unregisterReceiver(this);
                } catch (Exception e) {
                    Slog.d("detachRemote", e.getMessage());
                }
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (null != mContainer) {
                mContainer.attachRemoter(service);
            }
            Slog.d("远程服务连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (null != mContainer) {
                mContainer.clearAttach();
            }
            Slog.d("远程服务断开");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            doAttachRemote(context);
        }
    }

    protected static class Entity extends DataSyncCallback {
        public String mName = null;
        public Data mData = null;
        public String mRemoteLocker = null;
        protected boolean mFocus = false; //数据焦点, 当绑定的时候, 数据焦点为true则以本数据为准, 去修改远程的数据
        public LinkedHashMap<String, ILockerSource> mLocalLockers = new LinkedHashMap<>();
        public Map<String, DataViewCallback> mDataViewCallbacks = new HashMap<>();
        public Map<String, IDataSyncCallback> mDataSyncCallbacks = new HashMap<>();

        @Override
        public boolean onSync(String dataName, String dataView, boolean yesSelfNoOther, Data curr, Data toCompareValue, Data param, boolean execObserver) throws RemoteException {
            Slog.d("sync data from remote:", "data:"+dataName, "view:"+dataView, "yesSelfNoOther:"+yesSelfNoOther);
            if (null != curr && null != curr.mValue) {
                mData.mValue = curr.mValue;
            } else if (null != mData) {
                mData.copy(curr);
            } else return false;
            //第一次注册远程监听,是用的Center.toString(), 所以specifiedName会等于Center.toString(),这会导致找不到对应的监听callback
            syncToDataViews(this, dataView, yesSelfNoOther, null, execObserver, toCompareValue, param);
            return false;
        }
    }

    /**
     * Created by zeu on 2017/6/27.
     */

    public static class Container extends IRemoter.Stub {
        IRemoter mRemote;
        Attacher mAttacher;
        IPacketObserver mObserverHooker;
        DeadObjectCallback mDeadObjectCallback;
        List<OnRemoteAttachListener> mRemoteAttachmentListener = new ArrayList<>();
        Map<String, Entity> mCenters = new HashMap<>();

        public Container() {
            this(null);
            mAttacher = new Attacher(this);
        }

        public Container(IPacketObserver observerHooker) {
            mObserverHooker = observerHooker;
        }

        protected void doDeadObjectCallback(IRemoter remote, String dataName) {
            if (null != mDeadObjectCallback) {
                mDeadObjectCallback.onDeadObject(remote, dataName);

                synchronized (mCenters) {
                    for (Entity entity : mCenters.values()) {
                        if (null != entity && null != entity.mDataViewCallbacks) {
                            synchronized (entity.mDataViewCallbacks) {
                                for (DataViewCallback callback : entity.mDataViewCallbacks.values()) {
                                    if (null != callback) {
                                        callback.onRemoteDetach(mRemote);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            mRemote = null;
            mAttacher.doAttachRemote(mAttacher.mContext);
        }

        public void setDeadObjectCallback(DeadObjectCallback callback) {
            mDeadObjectCallback = callback;
        }

        public boolean attachService(Context context) {
            return mAttacher.attachService(context);
        }

        public void detachService() {
            mAttacher.detachService();
        }

        public void setAttachServiceIntent(Intent intent) {
            mAttacher.setAttachServiceIntent(intent);
        }

        public void setAttachServiceIntent(ComponentName componentName) {
            mAttacher.setAttachServiceIntent(componentName);
        }

        public void setAttachServiceIntent(String packName, String remoteServiceAttachClass) {
            mAttacher.setAttachServiceIntent(packName, remoteServiceAttachClass);
        }

        public void setAttachServiceReadyFilter(String serviceReadyReceiverAction) {
            mAttacher.setAttachServiceReadyFilter(serviceReadyReceiverAction);
        }
        public void setAttachServiceReadyFilter(IntentFilter filter) {
            mAttacher.setAttachServiceReadyFilter(filter);
        }

        @Override
        public Data getData(String dataName, Data defValue) {
            Data data = null;
            if (null != dataName) {
                Entity entity = mCenters.get(dataName);
                if (null == entity) {
                    return defValue;
                } else {
                    data = entity.mData;
                }
            }
            return data;
        }

        @Override
        public boolean setData(String dataName, String setterSource, IDataChangedObserver observer, boolean execListener, boolean lastValueEnabled, Data value, Data param) throws RemoteException {
            boolean ret = false;
            Slog.d("setData:entry"+dataName, (value instanceof Packet) ? "" : ("data:"+value));
            if (null != dataName) {
                //先将数据与远程数据仓库对比
                if (isRemoteAttached()) {
                    try {
                        ret = mRemote.setData(dataName, setterSource, observer, execListener, lastValueEnabled, value, param);
                        return ret;
                    } catch (DeadObjectException e) {
                        e.printStackTrace();
                        doDeadObjectCallback(mRemote, dataName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ret;
                    }
                }

                //不支持远程的话, 直接同步本地
                Entity entity;
                synchronized (mCenters) {
                    entity = mCenters.get(dataName);
                }
                if (null != entity) {
                    LinkedHashMap<String, ILockerSource> localLockers;
                    synchronized (entity) {
                        localLockers = entity.mLocalLockers;
                    }

                    synchronized (localLockers) {
                        if (!localLockers.isEmpty()) {
                            Map.Entry<String, ILockerSource> entry = new LinkedList<>(localLockers.entrySet()).getLast();
                            if (null != entry) {
                                if (entry.getValue() != null) {
                                    try {
                                        if (setterSource != entry.getValue().lockerName()) {//锁存在, 但锁不正确
                                            return false;
                                        }
                                    } catch (DeadObjectException e) {//锁销毁, 无效
                                        e.printStackTrace();
                                        localLockers.remove(entry.getKey());
                                    }
                                } else {//锁为空, 无效
                                    localLockers.remove(entry.getKey());
                                }
                            }
                        }
                    }

                    Data last = null, curr;
                    boolean unequal = false;
                    synchronized (entity) {
                        curr = entity.mData;
                    }
                    if (null == curr) {
                        if (null != value) {
                            last = null;
                            unequal = true;
                            curr = value;
                        }
                    } else if (!curr.equals(value)) {
                        synchronized (curr) {
                            if (!(curr instanceof Packet) || lastValueEnabled) {
                                last = curr.clone(curr);
                            }
                            curr = value;
                            unequal = true;
                        }
                    }

                    synchronized (entity) {
                        entity.mData = curr;
                    }

                    if (unequal) {
                        if (null != observer) {
                            try {
                                observer.onChanged(curr, last, param);
                            } catch (DeadObjectException e) {
                                e.printStackTrace();
                                doDeadObjectCallback(mRemote, dataName);
                            }
                        }

                        syncToDataViews(entity, null, true, null, execListener, null, param);
                        syncToRemoteContainer(entity, null, true, null, execListener, null, param);
                    }
                }
            }
            Slog.d("setData:exit");
            return ret;
        }

        public boolean registerDataView(String dataName, String dataView, DataViewCallback callback, Data value, boolean focus, boolean supportRemote) {
            boolean ret = false;
            if (null != callback && null != dataName && null != dataView) {
                //先注册到本地
                Entity entity = mCenters.get(dataName);
                if (null != entity) {
                    synchronized (entity.mDataViewCallbacks) {
                        entity.mDataViewCallbacks.put(dataView, callback);
                    }
                    synchronized (entity) {
                        if (focus) {
                            entity.mFocus = focus;
                        }
                        entity.mName = dataName;
                        if ((null == entity.mData && null != value) || (null != entity.mData && !entity.mData.equals(value))) {
                            if (focus) {
                                if (null == entity.mData) {
                                    entity.mData = value;
                                } else {
                                    entity.mData.copy(value);
                                }
                            } else {
                                callback.onChanged(dataName, dataView, true, entity.mData, null, true);
                            }
                        }
                    }
                } else {
                    entity = new Entity();
                    synchronized (entity) {
                        entity.mFocus = focus;
                        entity.mName = dataName;
                        entity.mData = Data.clone(value);
                        entity.mDataViewCallbacks.put(dataView, callback);
                    }
                    synchronized (mCenters) {
                        mCenters.put(dataName, entity);
                    }
                }

                callback.onLocalAttach();

                ret = true;

                //注册到远程
                if (supportRemote && isRemoteAttached()) {
                    try {
                        if (mRemote.registerDataSyncCallback(entity.mName, entity.toString(), entity, entity.mData, focus)) {
                            callback.onRemoteAttach(mRemote);
                        }
                    } catch (DeadObjectException e) {
                        e.printStackTrace();
                        doDeadObjectCallback(mRemote, "registerContentSyncCallback"); //死的对象, 会执行本地
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return ret;
        }

        public boolean unregisterDataView(String dataName, String callbackName) {
            boolean ret = false;
            if (null != dataName) {
                Entity entity = mCenters.get(dataName);
                if (null != entity) {
                    synchronized (entity.mDataViewCallbacks) {
                        DataViewCallback callback = entity.mDataViewCallbacks.get(callbackName);
                        if (null != callback) {
                            entity.mDataViewCallbacks.remove(callbackName);
                            callback.onLocalDetach();
                        }
                        ret = true;
                    }
                }
            }
            return ret;
        }

        @Override
        public boolean registerDataSyncCallback(String dataName, String callbackName, IDataSyncCallback callback, Data value, boolean focus) throws RemoteException {
            if (null != callback && null != dataName && null != callbackName) {
                //先注册到本地
                Entity entity = mCenters.get(dataName);
                if (null != entity) {
                    synchronized (entity) {
                        entity.mName = dataName;
                    }

                    synchronized (entity) {
                        if ((null == entity.mData && null != value) || (null != entity.mData && !entity.mData.equals(value))) {
                            try {
                                if (focus) {
                                    if (null == entity.mData) {
                                        if (null != value) {
                                            entity.mData = value;
                                            syncToDataViews(entity, null, true,null, true, null, null);
                                            syncToRemoteContainer(entity, null, true, null, true, entity.mData, null);
                                        }
                                    } else {
                                        if (!entity.mData.equals(value)) {
                                            if (null != value) {
                                                entity.mData.copy(value);
                                            } else {
                                                entity.mData = null;
                                            }
                                            syncToDataViews(entity, null, true, null, true, null, null);
                                            syncToRemoteContainer(entity, null, true, null, true, entity.mData, null);
                                        }
                                    }
                                } else {
                                    callback.onSync(dataName, null, true, entity.mData, null, null, true);
                                }
                            } catch (DeadObjectException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    synchronized (entity.mDataSyncCallbacks) {
                        entity.mDataSyncCallbacks.put(callbackName, callback);
                    }
                } else {
                    entity = new Entity();
                    synchronized (entity) {
                        entity.mName = dataName;
                        entity.mData = Data.clone(value);
                        entity.mDataSyncCallbacks.put(callbackName, callback);
                        logd("注册远程回调到本地", "dataName=" + dataName, "callbackName=" + callbackName);
                    }
                    synchronized (mCenters) {
                        mCenters.put(dataName, entity);
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean unregisterDataSyncCallback(String dataName, String callbackName) throws RemoteException {
            if (null != dataName && null != callbackName) {
                Entity entity = mCenters.get(dataName);
                if (null != entity) {
                    synchronized (entity.mDataSyncCallbacks) {
                        if (entity.mDataSyncCallbacks.containsKey(callbackName)) {
                            entity.mDataSyncCallbacks.remove(callbackName);
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * 本地控制中心的锁不为空, 且存在远程连接, 会注册锁到远程连接,
         * 每次访问锁的时候, 会返回最新的锁的名字, 这样当锁对象注销, 可以判断并回收锁
         * @param dataName
         * @param locker
         * @return
         * @throws RemoteException
         */
        @Override
        public String lock(String dataName, ILockerSource locker) throws RemoteException {
            String lockerName = null;
            if (null != dataName && null != locker) {
                final Entity entity = mCenters.get(dataName);
                if (null != entity && null != entity.mData) {
                    boolean empty;
                    synchronized (entity.mLocalLockers) {
                        empty = entity.mLocalLockers.isEmpty();
                        lockerName = locker.toString();
                        entity.mLocalLockers.put(lockerName, locker);
                    }

                    if (empty && isRemoteAttached()) {
                        try {
                            entity.mRemoteLocker = mRemote.lock(dataName, new ILockerSource.Stub() {
                                @Override
                                public String lockerName() throws RemoteException {
                                    String name = null;
                                    Map.Entry<String, ILockerSource>  entry = null;
                                    //获取最新的锁
                                    synchronized (entity.mLocalLockers) {
                                        if (!entity.mLocalLockers.isEmpty()) {
                                            entry = new LinkedList<>(entity.mLocalLockers.entrySet()).getLast();
                                        }
                                    }
                                    if (null != entry.getValue()) {
                                        try {
                                            //获取远程锁的名字, 当远程锁为DeadObject时移除锁
                                            name = entry.getValue().lockerName();
                                            if (null == name) {
                                                synchronized (entity.mLocalLockers) {
                                                    entity.mLocalLockers.remove(entry.getKey());
                                                }
                                            }
                                        } catch (DeadObjectException e) {
                                            e.printStackTrace();
                                            synchronized (entity.mLocalLockers) {
                                                entity.mLocalLockers.remove(entry.getKey());
                                            }
                                        }
                                    }
                                    return name;
                                }
                            });
                        } catch (DeadObjectException e) {
                            e.printStackTrace();
                            doDeadObjectCallback(mRemote, dataName);
                        }
                    }
                }
            }
            return lockerName;
        }

        /**
         * 本地控制中心的锁为空则清除远程的锁
         * @param locker
         * @return
         * @throws RemoteException
         */
        @Override
        public boolean unlock(String dataName, String locker) throws RemoteException {
            boolean ret = false;
            if (null != dataName && null != locker) {
                final Entity entity = mCenters.get(dataName);
                if (null != entity && null != entity.mData) {
                    boolean empty;
                    synchronized (entity.mLocalLockers) {
                        entity.mLocalLockers.remove(locker);
                        if (!entity.mLocalLockers.isEmpty()) {
                            empty = entity.mLocalLockers.isEmpty();
                            ret = true;
                        } else {
                            empty = true;
                        }
                    }
                    if (empty && isRemoteAttached()) {
                        if (null != entity.mRemoteLocker) {
                            try {
                                if (mRemote.unlock(dataName, entity.mRemoteLocker)) {
                                    entity.mRemoteLocker = null;
                                }
                            } catch (DeadObjectException e) {
                                e.printStackTrace();
                                doDeadObjectCallback(mRemote, dataName);
                            }
                        }
                    }
                }
            }
            return ret;
        }

        public boolean unlockAll(String dataName) throws RemoteException {
            boolean ret = false;
            if (null != dataName) {
                final Entity entity = mCenters.get(dataName);
                if (null != entity && null != entity.mData) {
                    boolean empty;
                    synchronized (entity.mLocalLockers) {
                        if (!entity.mLocalLockers.isEmpty()) {
                            entity.mLocalLockers.clear();
                            ret = true;
                            empty = false;
                        } else {
                            empty = true;
                        }
                    }

                    if (!empty && isRemoteAttached()) {
                        if (null != entity.mRemoteLocker) {
                            try {
                                if (mRemote.unlock(dataName, entity.mRemoteLocker)) {
                                    entity.mRemoteLocker = null;
                                }
                            } catch (DeadObjectException e) {
                                e.printStackTrace();
                                doDeadObjectCallback(mRemote, dataName);
                            }
                        }
                    }
                }
            }
            return ret;
        }

        public void clearAttach() {
            synchronized (mRemoteAttachmentListener) {
                for (OnRemoteAttachListener listener : mRemoteAttachmentListener) {
                    listener.onDetached(mRemote);
                }
            }
            mRemote = null;
        }

        public IRemoter getAttach() {
            return mRemote;
        }

        public boolean isRemoteAttached() {
            return (null != mRemote && !mRemote.equals(this));
        }

        /**
         * 在链接的时候将本地值复制到远程
         * @param dataName
         * @param value
         */
        public void syncToRemote(String dataName, Data value) {
            if (null != dataName) {
                Entity entity = mCenters.get(dataName);
                if (null != entity && null != entity.mData) {
                    entity.mData.copy(value);
                }
            }
        }

        public boolean addOnRemoteAttachListener(OnRemoteAttachListener listener) {
            boolean ret = false;
            if (null != listener) {
                synchronized (mRemoteAttachmentListener) {
                    if (!mRemoteAttachmentListener.contains(listener)) {
                        ret = mRemoteAttachmentListener.add(listener);
                        if (isRemoteAttached()) {
                            synchronized (mCenters) {
                                for (Entity entity : mCenters.values()) {
                                    if (null != entity) {
                                        listener.onAttached(mRemote);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return ret;
        }

        public boolean removeOnRemoteAttachListener(OnRemoteAttachListener listener) {
            if (null != listener) {
                synchronized (mRemoteAttachmentListener) {
                    return mRemoteAttachmentListener.remove(listener);
                }
            }
            return false;
        }


        /**
         * 如果在同一个应用中, 远程的和本地的使用的是同一个变量, 因此没必要绑定
         * @param remote
         * @param coverExist
         * @return
         */
        public synchronized boolean attachRemoter(IRemoter remote, boolean coverExist) {
            if (null != remote && !remote.equals(this)) {
                if (isRemoteAttached()) {
                    if (coverExist) {
                        detachRemoter();
                        mRemote = null;
                    } else return true;
                }

                //获取远程对象
                synchronized (mCenters) {
                    try {
                        for (Entity entity : mCenters.values()) {
                            if (null != entity && null != entity.mData) {
                                //当设置数值成功的时候, 回调, 确保远程的数据和本地数据一致
                                if (remote.registerDataSyncCallback(entity.mData.name(), entity.toString(), entity, entity.mData, entity.mFocus)) {
                                    synchronized (entity.mDataViewCallbacks) {
                                        for (DataViewCallback callback : entity.mDataViewCallbacks.values()) {
                                            if (null != callback) {
                                                callback.onRemoteAttach(remote);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        mRemote = remote;
                    } catch (DeadObjectException e) {
                        e.printStackTrace();
                        doDeadObjectCallback(remote, "attachRemoter");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                if (null != mRemote) {
                    synchronized (mRemoteAttachmentListener) {
                        for (OnRemoteAttachListener listener : mRemoteAttachmentListener) {
                            listener.onAttached(mRemote);
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean attachRemoter(IBinder service) {
            return attachRemoter(asInterface(service), false);
        }

        public boolean attachRemoter(IRemoter remote) {
            return attachRemoter(remote, false);
        }

        public synchronized void detachRemoter() {
            if (isRemoteAttached()) {
                try {
                    //获取远程对象
                    synchronized (mCenters) {
                        for (final Entity entity : mCenters.values()) {
                            if (null != entity && null != entity.mData) {
                                if (mRemote.unregisterDataSyncCallback(entity.mData.name(), entity.toString())) {
                                    synchronized (entity.mDataViewCallbacks) {
                                        for (DataViewCallback callback : entity.mDataViewCallbacks.values()) {
                                            if (null != callback) {
                                                callback.onRemoteDetach(mRemote);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (DeadObjectException e) {
                    e.printStackTrace();
                    doDeadObjectCallback(mRemote, "detachRemoter");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            synchronized (mRemoteAttachmentListener) {
                for (OnRemoteAttachListener listener : mRemoteAttachmentListener) {
                    listener.onDetached(mRemote);
                }
            }
            mRemote = null;
        }

        /**
         * 通知数据改变,并执行监听
         * @param dataName
         * @param dataView
         * @param onlySelf
         *          true: 仅通自己数据改变
         *          false: 通知其他使用此远程变量的对象数据已经改变
         * @param toCompareValue
         * @param param
         * @throws RemoteException
         */
        @Override
        public void notifyDataChanged(String dataName, String dataView, boolean isLocalInstance, boolean onlySelf, boolean lastValueEnabled, Data toCompareValue, Data param) throws RemoteException {
            if (null != dataName) {
                if (isRemoteAttached()) {
                    try {
                        mRemote.notifyDataChanged(dataName, dataView, false, onlySelf, lastValueEnabled, toCompareValue, param);
                        return;
                    } catch (DeadObjectException e) {
                        e.printStackTrace();
                        doDeadObjectCallback(mRemote, dataName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }

                Entity entity = mCenters.get(dataName);
                if (null != entity) {
                    syncToDataViews(entity, dataView, onlySelf, null, true, toCompareValue, param);
                    syncToRemoteContainer(entity, dataView, onlySelf, entity.mName, true, toCompareValue, param);
                }
            }
        }

        public void addEventObserver() {

        }

        public void storeToFile() {

        }

        public void loadFromFile() {

        }

        public void storeToDb() {

        }

        public void loadFromDb() {

        }

        public static IRemoter get(IBinder service) {
            return Stub.asInterface(service);
        }
    }

    static Slog sSlog;
    static {
        sSlog = Slog.get();
    }
    public static void setLog(Slog.Type type) {
        if (null != type) {
            sSlog.addType(type);
        }
    }

    public static void logd(String ...msg) {
        if (null != sSlog) {
            sSlog._d(1, 0, msg);
        }
    }
}