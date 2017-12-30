package com.zeu.frame.bind.listener;
import com.zeu.frame.bind.comm.IRemoter;

/**
 * Created by zeu on 2017/8/3.
 */

public interface OnRemoteAttachListener {
    void onAttached(IRemoter remoter);
    void onDetached(IRemoter remoter);
}
