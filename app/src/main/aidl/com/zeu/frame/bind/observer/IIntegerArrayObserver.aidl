// IRboolean.aidl
package com.zeu.frame.bind.observer;
import com.zeu.frame.bind.Data;
// Declare any non-default types here with import statements

interface IIntegerArrayObserver {
//所有的AIDL的回调函数中, 不能直接更新View, 否则可能会出现非主线程更新View的异常, 在解析AIDL协议的时候,并非在主线程中触发回调
    boolean onChanged(in int[] curr, in int[] last, in Data param);
}
