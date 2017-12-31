package com.zeu.frame.bind;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.zeu.frame.BuildConfig;
import com.zeu.frame.bind.callback.DataSyncCallback;
import com.zeu.frame.bind.callback.DataViewCallback;
import com.zeu.frame.bind.callback.DeadObjectCallback;
import com.zeu.frame.bind.callback.IDataSyncCallback;
import com.zeu.frame.bind.comm.ILockerSource;
import com.zeu.frame.bind.comm.IRemoter;
import com.zeu.frame.bind.comm.IRemoter.Stub;
import com.zeu.frame.bind.listener.OnRemoteAttachListener;
import com.zeu.frame.bind.observer.IDataChangedObserver;
import com.zeu.frame.bind.observer.IPacketObserver;
import com.zeu.frame.log.Slog;
import com.zeu.frame.log.Slog.Type;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Binders {
    static Map<String, Container> sContainer = new HashMap();
    static Slog sSlog = Slog.get();

    protected static class Attacher extends BroadcastReceiver implements ServiceConnection {
        Container mContainer;
        Context mContext;
        Intent mRemoteServiceAttachIntent;
        IntentFilter mRemoteServiceReadyFilter;

        public Attacher(Container container) {
            this.mContainer = container;
        }

        public void setAttachServiceIntent(Intent intent) {
            this.mRemoteServiceAttachIntent = intent;
        }

        public void setAttachServiceIntent(ComponentName componentName) {
            if (componentName != null) {
                this.mRemoteServiceAttachIntent = new Intent().setComponent(componentName);
            }
        }

        public void setAttachServiceIntent(String packName, String remoteServiceAttachClass) {
            if (packName != null && remoteServiceAttachClass != null) {
                this.mRemoteServiceAttachIntent = new Intent().setComponent(new ComponentName(packName, remoteServiceAttachClass));
            }
        }

        public void setAttachServiceReadyFilter(String serviceReadyReceiverAction) {
            if (serviceReadyReceiverAction != null) {
                this.mRemoteServiceReadyFilter = new IntentFilter(serviceReadyReceiverAction);
            }
        }

        public void setAttachServiceReadyFilter(IntentFilter filter) {
            this.mRemoteServiceReadyFilter = filter;
        }

        protected boolean doAttachRemote(Context context) {
            boolean ret = false;
            Slog.d("尝试连接远程服务");
            if (this.mRemoteServiceAttachIntent != null) {
                try {
                    ret = context.bindService(this.mRemoteServiceAttachIntent.setFlags(32), this, 1);
                } catch (Exception e) {
                    Slog.d("连接远程服务失败" + e.getMessage());
                }
            } else {
                Slog.d("远程服务Intent为空");
            }
            return ret;
        }

        protected void doDetachRemote() {
            Slog.d("尝试断开远程服务");
            if (this.mContext != null) {
                Slog.d("断开所有远程数据");
                if (this.mContainer != null) {
                    this.mContainer.detachRemoter();
                }
                try {
                    Slog.d("断开远程服务连接");
                    this.mContext.unbindService(this);
                    return;
                } catch (Exception e) {
                    Slog.d("断开远程服务失败:" + e.getMessage());
                    return;
                }
            }
            Slog.d("尝试断开远程服务");
        }

        public boolean attachService(Context context) {
            boolean ret = false;
            if (context != null) {
                this.mContext = context;
                ret = doAttachRemote(context);
                if (this.mRemoteServiceReadyFilter != null) {
                    try {
                        context.registerReceiver(this, this.mRemoteServiceReadyFilter);
                    } catch (Exception e) {
                        Slog.d("attachRemote", e.getMessage());
                    }
                }
            }
            return ret;
        }

        public void detachService() {
            if (this.mContext != null) {
                doDetachRemote();
                try {
                    this.mContext.unregisterReceiver(this);
                } catch (Exception e) {
                    Slog.d("detachRemote", e.getMessage());
                }
            }
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (this.mContainer != null) {
                this.mContainer.attachRemoter(service);
            }
            Slog.d("远程服务连接");
        }

        public void onServiceDisconnected(ComponentName name) {
            if (this.mContainer != null) {
                this.mContainer.clearAttach();
            }
            Slog.d("远程服务断开");
        }

        public void onReceive(Context context, Intent intent) {
            doAttachRemote(context);
        }
    }

    public static class Container extends Stub {
        Attacher mAttacher;
        Map<String, Entity> mCenters;
        DeadObjectCallback mDeadObjectCallback;
        IPacketObserver mObserverHooker;
        IRemoter mRemote;
        List<OnRemoteAttachListener> mRemoteAttachmentListener;

        public Container() {
            this(null);
            this.mAttacher = new Attacher(this);
        }

        public Container(IPacketObserver observerHooker) {
            this.mRemoteAttachmentListener = new ArrayList();
            this.mCenters = new HashMap();
            this.mObserverHooker = observerHooker;
        }

        protected void doDeadObjectCallback(IRemoter remote, String dataName) {
            if (this.mDeadObjectCallback != null) {
                this.mDeadObjectCallback.onDeadObject(remote, dataName);
                synchronized (this.mCenters) {
                    for (Entity entity : this.mCenters.values()) {
                        if (!(entity == null || entity.mDataViewCallbacks == null)) {
                            synchronized (entity.mDataViewCallbacks) {
                                for (DataViewCallback callback : entity.mDataViewCallbacks.values()) {
                                    if (callback != null) {
                                        callback.onRemoteDetach(this.mRemote);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.mRemote = null;
            this.mAttacher.doAttachRemote(this.mAttacher.mContext);
        }

        public void setDeadObjectCallback(DeadObjectCallback callback) {
            this.mDeadObjectCallback = callback;
        }

        public boolean attachService(Context context) {
            return this.mAttacher.attachService(context);
        }

        public void detachService() {
            this.mAttacher.detachService();
        }

        public void setAttachServiceIntent(Intent intent) {
            this.mAttacher.setAttachServiceIntent(intent);
        }

        public void setAttachServiceIntent(ComponentName componentName) {
            this.mAttacher.setAttachServiceIntent(componentName);
        }

        public void setAttachServiceIntent(String packName, String remoteServiceAttachClass) {
            this.mAttacher.setAttachServiceIntent(packName, remoteServiceAttachClass);
        }

        public void setAttachServiceReadyFilter(String serviceReadyReceiverAction) {
            this.mAttacher.setAttachServiceReadyFilter(serviceReadyReceiverAction);
        }

        public void setAttachServiceReadyFilter(IntentFilter filter) {
            this.mAttacher.setAttachServiceReadyFilter(filter);
        }

        public Data getData(String dataName, Data defValue) {
            Data data = null;
            if (dataName != null) {
                Entity entity = (Entity) this.mCenters.get(dataName);
                if (entity == null) {
                    return defValue;
                }
                data = entity.mData;
            }
            return data;
        }

        @Override
        public boolean setData(String dataName, String setterSource, IDataChangedObserver observer, boolean execListener, boolean lastValueEnabled, Data value, Data param) throws RemoteException {
            boolean ret = false;
            if (null != dataName) {
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
            return ret;
        }

        public boolean registerDataView(String dataName, String dataView, DataViewCallback callback, Data value, boolean focus, boolean supportRemote) {
            boolean ret = false;
            if (!(callback == null || dataName == null || dataView == null)) {
                Entity entity;
                Entity entity2 = (Entity) this.mCenters.get(dataName);
                if (entity2 != null) {
                    synchronized (entity2.mDataViewCallbacks) {
                        entity2.mDataViewCallbacks.put(dataView, callback);
                    }
                    synchronized (entity2) {
                        if (focus) {
                            entity2.mFocus = focus;
                        }
                        entity2.mName = dataName;
                        if ((entity2.mData == null && value != null) || !(entity2.mData == null || entity2.mData.equals(value))) {
                            if (!focus) {
                                callback.onChanged(dataName, dataView, true, entity2.mData, null, true);
                            } else if (entity2.mData == null) {
                                entity2.mData = value;
                            } else {
                                entity2.mData.copy(value);
                            }
                        }
                    }
                    entity = entity2;
                } else {
                    entity = new Entity();
                    synchronized (entity) {
                        entity.mFocus = focus;
                        entity.mName = dataName;
                        entity.mData = Data.clone(value);
                        entity.mDataViewCallbacks.put(dataView, callback);
                    }
                    synchronized (this.mCenters) {
                        this.mCenters.put(dataName, entity);
                    }
                }
                callback.onLocalAttach();
                ret = true;
                if (supportRemote && isRemoteAttached()) {
                    try {
                        if (this.mRemote.registerDataSyncCallback(entity.mName, entity.toString(), entity, entity.mData, focus)) {
                            callback.onRemoteAttach(this.mRemote);
                        }
                    } catch (DeadObjectException e) {
                        e.printStackTrace();
                        doDeadObjectCallback(this.mRemote, "registerContentSyncCallback");
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
            return ret;
        }

        public boolean unregisterDataView(String dataName, String callbackName) {
            boolean ret = false;
            if (dataName != null) {
                Entity entity = (Entity) this.mCenters.get(dataName);
                if (entity != null) {
                    synchronized (entity.mDataViewCallbacks) {
                        DataViewCallback callback = (DataViewCallback) entity.mDataViewCallbacks.get(callbackName);
                        if (callback != null) {
                            entity.mDataViewCallbacks.remove(callbackName);
                            callback.onLocalDetach();
                        }
                        ret = true;
                    }
                }
            }
            return ret;
        }

        public boolean registerDataSyncCallback(String dataName, String callbackName, IDataSyncCallback callback, Data value, boolean focus) throws RemoteException {
            if (callback == null || dataName == null || callbackName == null) {
                return false;
            }
            Entity entity = (Entity) this.mCenters.get(dataName);
            if (entity != null) {
                synchronized (entity) {
                    entity.mName = dataName;
                }
                synchronized (entity) {
                    if ((entity.mData == null && value != null) || !(entity.mData == null || entity.mData.equals(value))) {
                        if (focus) {
                            if (entity.mData == null) {
                                if (value != null) {
                                    entity.mData = value;
                                    Binders.syncToDataViews(entity, null, true, null, true, null, null);
                                    Binders.syncToRemoteContainer(entity, null, true, null, true, entity.mData, null);
                                }
                            } else if (!entity.mData.equals(value)) {
                                if (value != null) {
                                    entity.mData.copy(value);
                                } else {
                                    entity.mData = null;
                                }
                                Binders.syncToDataViews(entity, null, true, null, true, null, null);
                                Binders.syncToRemoteContainer(entity, null, true, null, true, entity.mData, null);
                            }
                        } else {
                            callback.onSync(dataName, null, true, entity.mData, null, null, true);
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
                    Binders.logd("注册远程回调到本地", "dataName=" + dataName, "callbackName=" + callbackName);
                }
                synchronized (this.mCenters) {
                    this.mCenters.put(dataName, entity);
                }
            }
            return true;
        }

        public boolean unregisterDataSyncCallback(String dataName, String callbackName) throws RemoteException {
            if (!(dataName == null || callbackName == null)) {
                Entity entity = (Entity) this.mCenters.get(dataName);
                if (entity != null) {
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

        public String lock(String dataName, ILockerSource locker) throws RemoteException {
            String lockerName = null;
            if (!(dataName == null || locker == null)) {
                final Entity entity = (Entity) this.mCenters.get(dataName);
                if (!(entity == null || entity.mData == null)) {
                    boolean empty;
                    synchronized (entity.mLocalLockers) {
                        empty = entity.mLocalLockers.isEmpty();
                        lockerName = locker.toString();
                        entity.mLocalLockers.put(lockerName, locker);
                    }
                    if (empty && isRemoteAttached()) {
                        try {
                            entity.mRemoteLocker = this.mRemote.lock(dataName, new ILockerSource.Stub() {
                                public String lockerName() throws RemoteException {
                                    String name = null;
                                    Entry<String, ILockerSource> entry = null;
                                    synchronized (entity.mLocalLockers) {
                                        if (!entity.mLocalLockers.isEmpty()) {
                                            entry = (Entry) new LinkedList(entity.mLocalLockers.entrySet()).getLast();
                                        }
                                    }
                                    if (entry.getValue() != null) {
                                        try {
                                            name = ((ILockerSource) entry.getValue()).lockerName();
                                            if (name == null) {
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
                            doDeadObjectCallback(this.mRemote, dataName);
                        }
                    }
                }
            }
            return lockerName;
        }

        public boolean unlock(String dataName, String locker) throws RemoteException {
            boolean ret = false;
            if (!(dataName == null || locker == null)) {
                Entity entity = (Entity) this.mCenters.get(dataName);
                if (!(entity == null || entity.mData == null)) {
                    boolean empty;
                    synchronized (entity.mLocalLockers) {
                        entity.mLocalLockers.remove(locker);
                        if (entity.mLocalLockers.isEmpty()) {
                            empty = true;
                        } else {
                            empty = entity.mLocalLockers.isEmpty();
                            ret = true;
                        }
                    }
                    if (empty && isRemoteAttached() && entity.mRemoteLocker != null) {
                        try {
                            if (this.mRemote.unlock(dataName, entity.mRemoteLocker)) {
                                entity.mRemoteLocker = null;
                            }
                        } catch (DeadObjectException e) {
                            e.printStackTrace();
                            doDeadObjectCallback(this.mRemote, dataName);
                        }
                    }
                }
            }
            return ret;
        }

        public boolean unlockAll(String dataName) throws RemoteException {
            boolean ret = false;
            if (dataName != null) {
                Entity entity = (Entity) this.mCenters.get(dataName);
                if (!(entity == null || entity.mData == null)) {
                    boolean empty;
                    synchronized (entity.mLocalLockers) {
                        if (entity.mLocalLockers.isEmpty()) {
                            empty = true;
                        } else {
                            entity.mLocalLockers.clear();
                            ret = true;
                            empty = false;
                        }
                    }
                    if (!(empty || !isRemoteAttached() || entity.mRemoteLocker == null)) {
                        try {
                            if (this.mRemote.unlock(dataName, entity.mRemoteLocker)) {
                                entity.mRemoteLocker = null;
                            }
                        } catch (DeadObjectException e) {
                            e.printStackTrace();
                            doDeadObjectCallback(this.mRemote, dataName);
                        }
                    }
                }
            }
            return ret;
        }

        public void clearAttach() {
            synchronized (this.mRemoteAttachmentListener) {
                for (OnRemoteAttachListener listener : this.mRemoteAttachmentListener) {
                    listener.onDetached(this.mRemote);
                }
            }
            this.mRemote = null;
        }

        public IRemoter getAttach() {
            return this.mRemote;
        }

        public boolean isRemoteAttached() {
            return (this.mRemote == null || this.mRemote.equals(this)) ? false : true;
        }

        public void syncToRemote(String dataName, Data value) {
            if (dataName != null) {
                Entity entity = (Entity) this.mCenters.get(dataName);
                if (entity != null && entity.mData != null) {
                    entity.mData.copy(value);
                }
            }
        }

        public boolean addOnRemoteAttachListener(OnRemoteAttachListener listener) {
            boolean ret = false;
            if (listener != null) {
                synchronized (this.mRemoteAttachmentListener) {
                    if (!this.mRemoteAttachmentListener.contains(listener)) {
                        ret = this.mRemoteAttachmentListener.add(listener);
                        if (isRemoteAttached()) {
                            synchronized (this.mCenters) {
                                for (Entity entity : this.mCenters.values()) {
                                    if (entity != null) {
                                        listener.onAttached(this.mRemote);
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
            if (listener == null) {
                return false;
            }
            boolean remove;
            synchronized (this.mRemoteAttachmentListener) {
                remove = this.mRemoteAttachmentListener.remove(listener);
            }
            return remove;
        }

        public synchronized boolean attachRemoter(IRemoter remote, boolean coverExist) {
            boolean z;
            if (remote != null) {
                if (!remote.equals(this)) {
                    if (isRemoteAttached()) {
                        if (coverExist) {
                            detachRemoter();
                            this.mRemote = null;
                        } else {
                            z = true;
                        }
                    }
                    synchronized (this.mCenters) {
                        try {
                            for (Entity entity : this.mCenters.values()) {
                                if (!(entity == null || entity.mData == null)) {
                                    if (remote.registerDataSyncCallback(entity.mData.name(), entity.toString(), entity, entity.mData, entity.mFocus)) {
                                        synchronized (entity.mDataViewCallbacks) {
                                            for (DataViewCallback callback : entity.mDataViewCallbacks.values()) {
                                                if (callback != null) {
                                                    callback.onRemoteAttach(remote);
                                                }
                                            }
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                            }
                            this.mRemote = remote;
                        } catch (DeadObjectException e) {
                            e.printStackTrace();
                            doDeadObjectCallback(remote, "attachRemoter");
                        } catch (RemoteException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (this.mRemote != null) {
                        synchronized (this.mRemoteAttachmentListener) {
                            for (OnRemoteAttachListener listener : this.mRemoteAttachmentListener) {
                                listener.onAttached(this.mRemote);
                            }
                        }
                        z = true;
                    }
                }
            }
            z = false;
            return z;
        }

        public boolean attachRemoter(IBinder service) {
            return attachRemoter(Stub.asInterface(service), false);
        }

        public boolean attachRemoter(IRemoter remote) {
            return attachRemoter(remote, false);
        }

        public synchronized void detachRemoter() {
            if (isRemoteAttached()) {
                try {
                    synchronized (this.mCenters) {
                        for (Entity entity : this.mCenters.values()) {
                            if (!(entity == null || entity.mData == null || !this.mRemote.unregisterDataSyncCallback(entity.mData.name(), entity.toString()))) {
                                synchronized (entity.mDataViewCallbacks) {
                                    for (DataViewCallback callback : entity.mDataViewCallbacks.values()) {
                                        if (callback != null) {
                                            callback.onRemoteDetach(this.mRemote);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (DeadObjectException e) {
                    e.printStackTrace();
                    doDeadObjectCallback(this.mRemote, "detachRemoter");
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                }
            }
            synchronized (this.mRemoteAttachmentListener) {
                for (OnRemoteAttachListener listener : this.mRemoteAttachmentListener) {
                    listener.onDetached(this.mRemote);
                }
            }
            this.mRemote = null;
        }

        public void notifyDataChanged(String dataName, String dataView, boolean isLocalInstance, boolean onlySelf, boolean lastValueEnabled, Data toCompareValue, Data param) throws RemoteException {
            if (dataName != null) {
                if (isRemoteAttached()) {
                    try {
                        this.mRemote.notifyDataChanged(dataName, dataView, false, onlySelf, lastValueEnabled, toCompareValue, param);
                        return;
                    } catch (DeadObjectException e) {
                        e.printStackTrace();
                        doDeadObjectCallback(this.mRemote, dataName);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        return;
                    }
                }
                Entity entity = (Entity) this.mCenters.get(dataName);
                if (entity != null) {
                    Binders.syncToDataViews(entity, dataView, onlySelf, null, true, toCompareValue, param);
                    Binders.syncToRemoteContainer(entity, dataView, onlySelf, entity.mName, true, toCompareValue, param);
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

    protected static class Entity extends DataSyncCallback {
        public Data mData = null;
        public Map<String, IDataSyncCallback> mDataSyncCallbacks = new HashMap();
        public Map<String, DataViewCallback> mDataViewCallbacks = new HashMap();
        protected boolean mFocus = false;
        public LinkedHashMap<String, ILockerSource> mLocalLockers = new LinkedHashMap();
        public String mName = null;
        public String mRemoteLocker = null;

        protected Entity() {
        }

        public boolean onSync(String dataName, String dataView, boolean yesSelfNoOther, Data curr, Data toCompareValue, Data param, boolean execObserver) throws RemoteException {
            Slog.d("sync data from remote:", "data:" + dataName, "view:" + dataView, "yesSelfNoOther:" + yesSelfNoOther);
            if (curr == null || curr.mValue == null) {
                if (this.mData != null) {
                    this.mData.copy(curr);
                }
                return false;
            }
            this.mData.mValue = curr.mValue;
            Binders.syncToDataViews(this, dataView, yesSelfNoOther, null, execObserver, toCompareValue, param);
            return false;
        }
    }

    public static Container getContainer(String moduleName, boolean createIfNotExist) {
        if (moduleName == null) {
            return null;
        }
        Container container;
        synchronized (sContainer) {
            container = (Container) sContainer.get(moduleName);
        }
        if (container != null || !createIfNotExist) {
            return container;
        }
        container = new Container();
        synchronized (sContainer) {
            sContainer.put(moduleName, container);
        }
        return container;
    }

    public static Container getContainer(String moduleName) {
        return getContainer(moduleName, true);
    }

    public static Container getContainer(boolean createIfNotExist) {
        return getContainer(BuildConfig.FLAVOR, createIfNotExist);
    }

    public static Container getContainer() {
        return getContainer(BuildConfig.FLAVOR, true);
    }

    public static boolean attach(Context context, String moduleName, String packName, String className, String receiverAction) {
        Container container = getContainer(moduleName);
        if (container == null) {
            return false;
        }
        container.setAttachServiceIntent(packName, className);
        container.setAttachServiceReadyFilter(receiverAction);
        return container.attachService(context);
    }

    public static boolean attach(Context context, String moduleName, ComponentName componentName, String receiverAction) {
        Container container = getContainer(moduleName);
        if (container == null) {
            return false;
        }
        container.setAttachServiceIntent(componentName);
        container.setAttachServiceReadyFilter(receiverAction);
        return container.attachService(context);
    }

    public static boolean attach(Context context, String moduleName, Intent serviceIntent, String receiverAction) {
        Container container = getContainer(moduleName);
        if (container == null) {
            return false;
        }
        container.setAttachServiceIntent(serviceIntent);
        container.setAttachServiceReadyFilter(receiverAction);
        return container.attachService(context);
    }

    public static void detach(String moduleName) {
        Container container = getContainer(moduleName);
        if (container != null) {
            container.detachService();
        }
    }

    public static boolean attach(Context context, String packName, String className, String receiverAction) {
        return attach(context, BuildConfig.FLAVOR, packName, className, receiverAction);
    }

    public static boolean attach(Context context, ComponentName componentName, String receiverAction) {
        return attach(context, BuildConfig.FLAVOR, componentName, receiverAction);
    }

    public static boolean attach(Context context, Intent serviceIntent, String receiverAction) {
        return attach(context, BuildConfig.FLAVOR, serviceIntent, receiverAction);
    }

    public static void detach() {
        detach(BuildConfig.FLAVOR);
    }

    protected static Intent getServiceIntent(Context context, Class<?> cls) {
        if (context == null || cls == null) {
            return null;
        }
        return new Intent(context, cls).setFlags(32);
    }

    protected static Intent getServiceIntent(ComponentName componentName) {
        if (componentName != null) {
            return new Intent().setComponent(componentName).setFlags(32);
        }
        return null;
    }

    public static Intent startService(Context context, Class<?> cls) {
        Intent intent = getServiceIntent(context, cls);
        if (intent != null) {
            try {
                context.startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return intent;
    }

    public static Intent startService(Context context, Intent intent) {
        if (intent != null) {
            try {
                context.startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return intent;
    }

    public static Intent startService(Context context, ComponentName componentName) {
        Intent intent = getServiceIntent(componentName);
        if (intent != null) {
            try {
                context.startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return intent;
    }

    public static Intent startService(Context context, String pkg, String cls) {
        return (pkg == null || cls == null) ? null : startService(context, new ComponentName(pkg, cls));
    }

    public static boolean stopService(Context context, Intent intent) {
        if (!(context == null || intent == null)) {
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
        if (!(context == null || intent == null)) {
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
        if (!(context == null || intent == null)) {
            try {
                context.stopService(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean bindService(Context context, Class<?> cls, ServiceConnection connection) {
        boolean ret = false;
        if (!(context == null || cls == null || connection == null)) {
            try {
                ret = context.bindService(new Intent(context, cls).setFlags(32), connection, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static boolean bindService(Context context, ComponentName componentName, ServiceConnection connection) {
        boolean ret = false;
        if (!(componentName == null || context == null)) {
            try {
                ret = context.bindService(new Intent().setComponent(componentName).setFlags(32), connection, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static boolean bindService(Context context, String pkg, String cls, ServiceConnection connection) {
        return (pkg == null || cls == null) ? false : bindService(context, new ComponentName(pkg, cls), connection);
    }

    public static boolean unbindService(Context context, ServiceConnection connection) {
        if (context == null) {
            return false;
        }
        try {
            context.unbindService(connection);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void writeArrayParcel(Parcel dest, Object value, int flags) {
        if (value != null) {
            int N = Array.getLength(value);
            dest.writeInt(N);
            dest.writeString(value.getClass().getComponentType().getName());
            for (int i = 0; i < N; i++) {
                Object item = Array.get(value, i);
                if (item instanceof Packet) {
                    dest.writeInt(1);
                    ((Packet) item).writeToParcel(dest, flags);
                } else {
                    dest.writeInt(0);
                }
            }
            return;
        }
        dest.writeInt(-1);
    }

    public static Object createObjFromParcel(Parcel parcel) {
        Object obj = null;
        String className = parcel.readString();
        if (className != null) {
            try {
                Class clas = Class.forName(className);
                Constructor constructor = null;
                try {
                    constructor = clas.getDeclaredConstructor(new Class[]{Parcel.class});
                    constructor.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    try {
                        constructor = clas.getConstructor(new Class[]{Parcel.class});
                    } catch (NoSuchMethodException e1) {
                        e1.printStackTrace();
                    }
                }
                if (constructor != null) {
                    obj = constructor.newInstance(new Object[]{parcel});
                }
            } catch (ClassNotFoundException e2) {
                e2.printStackTrace();
            } catch (InstantiationException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            } catch (IllegalAccessException e5) {
                e5.printStackTrace();
            }
        }
        return obj;
    }

    public static Object createArrayFromParcel(Parcel parcel) {
        Object obj = null;
        int len = parcel.readInt();
        if (len > 0) {
            try {
                obj = Array.newInstance(Class.forName(parcel.readString()), len);
                for (int i = 0; i < len; i++) {
                    if (parcel.readInt() == 1) {
                        Array.set(obj, i, createObjFromParcel(parcel));
                    } else {
                        Array.set(obj, i, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    protected static void syncToDataViews(Entity entity, String specialDataView, boolean yesSelfNoOther, String exceptDataView, boolean execObserver, Data toCompareValue, Data param) {
        Slog.d("sync to views(start) " + entity);
        if (entity != null) {
            String name;
            Data data;
            Map<String, DataViewCallback> dataViewCallbacks;
            synchronized (entity) {
                name = entity.mName;
                data = entity.mData;
                dataViewCallbacks = entity.mDataViewCallbacks;
            }
            if (data != null && (toCompareValue == null || !data.equals(toCompareValue))) {
                synchronized (entity) {
                    logd("开始同步数据到View:" + name, "DataView=" + dataViewCallbacks);
                    synchronized (dataViewCallbacks) {
                        if (specialDataView == null) {
                            for (Entry<String, DataViewCallback> entry : dataViewCallbacks.entrySet()) {
                                if (!(entry == null || entry.getValue() == null || ((String) entry.getKey()).equals(exceptDataView))) {
                                    ((DataViewCallback) entry.getValue()).onChanged(name, specialDataView, yesSelfNoOther, data, param, execObserver);
                                }
                            }
                        } else if (yesSelfNoOther) {
                            DataViewCallback callback = (DataViewCallback) dataViewCallbacks.get(specialDataView);
                            if (callback != null) {
                                callback.onChanged(name, specialDataView, yesSelfNoOther, data, param, execObserver);
                            }
                        } else {
                            for (Entry<String, DataViewCallback> entry2 : dataViewCallbacks.entrySet()) {
                                if (!(entry2 == null || entry2.getValue() == null)) {
                                    if (!specialDataView.equals(entry2.getKey())) {
                                        ((DataViewCallback) entry2.getValue()).onChanged(name, specialDataView, yesSelfNoOther, data, param, execObserver);
                                    }
                                }
                            }
                        }
                    }
                    logd("同步数据到Views完成");
                }
            }
        } else {
            logd("对象相等, 不执行监听");
        }
        Slog.d("sync to views(finish)");
    }

    protected static void syncToRemoteContainer(Entity entity, String specialDataView, boolean yesSelfNoOther, String exceptDataView, boolean execObserver, Data toCompareValue, Data param) {
        if (entity != null) {
            String name;
            Data data;
            Map<String, IDataSyncCallback> dataSyncCallbacks;
            synchronized (entity) {
                name = entity.mName;
                data = entity.mData;
                dataSyncCallbacks = entity.mDataSyncCallbacks;
            }
            Slog.d("sync to remote(start) : " + name);
            synchronized (dataSyncCallbacks) {
                List<String> removes = new ArrayList();
                for (Entry<String, IDataSyncCallback> entry : dataSyncCallbacks.entrySet()) {
                    if (!(entry == null || entry.getValue() == null || ((String) entry.getKey()).equals(exceptDataView))) {
                        try {
                            ((IDataSyncCallback) entry.getValue()).onSync(name, specialDataView, yesSelfNoOther, data, toCompareValue, param, execObserver);
                        } catch (DeadObjectException e) {
                            e.printStackTrace();
                            removes.add(entry.getKey());
                        } catch (RemoteException e2) {
                            e2.printStackTrace();
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

    public static void setLog(Type type) {
        if (type != null) {
            sSlog.addType(type);
        }
    }

    public static void logd(String... msg) {
        if (sSlog != null) {
            sSlog._d(1, 0, msg);
        }
    }
}
