// IRemote.aidl
package com.zeu.frame.bind.comm;
// Declare any non-default types here with import statements
import com.zeu.frame.bind.Data;

import com.zeu.frame.bind.observer.IDataChangedObserver;
import com.zeu.frame.bind.observer.IPacketArrayObserver;

import com.zeu.frame.bind.callback.IDataSyncCallback;
import com.zeu.frame.bind.comm.ILockerSource;

interface IRemoter {
    //框架基本结构:一个远程数据维护多个视图, 而每个视图在改变数据的时候, 会同步其他数据, 在链接的时候确保数据的一致性
    //每个对象都维护一个同步回调函数, 在连接上服务的时候, 注册远程, 注册远程前同步内容, 并执行对应的监听
    //所有的AIDL的回调函数中, 不能直接更新View, 否则可能会出现非主线程更新View的异常, 在解析AIDL协议的时候,并非在主线程中触发回调
    //注册远程同步回调, 当远程连接上的时候, 如果语句改变, 会回调所有的监听
    boolean registerDataSyncCallback(String dataName, String callbackName, IDataSyncCallback callback, in Data value, boolean focus);
    boolean unregisterDataSyncCallback(String dataName, String callbackName);

    //链接的时候, 将本地的LockerCallback同步到远方, 没有链接的时候,同步到本地, 已经链接, 则同步到本地和远方
    String lock(String dataName, ILockerSource locker);
    boolean unlock(String dataName, String locker);

    //通知specifiedInstance指定远程对象, value==null, 则忽略值, 执行监听, 否则先比对是否相等, 如果不想等则执行监听
    void notifyDataChanged(String dataName, String variableView, boolean isLocalInstance, boolean selfOrOther, boolean noLastValue, in Data toCompareValue, in Data param);

    Data getData(String dataName, in Data defValue/*parcelable类型必须放最后*/);
    boolean setData(String dataName, String variableView, IDataChangedObserver observer, boolean exeAddedObserver, boolean noLastValue, in Data value/*parcelable类型必须放最后*/, in Data param);
}