// IParcelObserver.aidl
package com.zeu.frame.bind.comm;
// Declare any non-default types here with import statements

interface ILockerSource {
   //所有的AIDL的回调函数中, 不能直接更新View, 否则可能会出现非主线程更新View的异常, 在解析AIDL协议的时候,并非在主线程中触发回调
    String lockerName(); //true is added
}
