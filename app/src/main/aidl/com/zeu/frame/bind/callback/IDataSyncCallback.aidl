// IParcelObserver.aidl
package com.zeu.frame.bind.callback;
import com.zeu.frame.bind.Data;
// Declare any non-default types here with import statements

interface IDataSyncCallback {
   //所有的AIDL的回调函数中, 不能直接更新View, 否则可能会出现非主线程更新View的异常, 在解析AIDL协议的时候,并非在主线程中触发回调
    boolean onSync(String dataName, String dataView, boolean yesSelfNoOther, in Data curr, in Data toCompareValue, in Data param, boolean execObserver);
}
