package com.zeu.frame.bind.callback;

import com.zeu.frame.bind.Data;
import com.zeu.frame.bind.comm.IRemoter;

/**
 * Created by zeu on 2017/7/11.
 */
public interface DataViewCallback {
    boolean onChanged(String dataName, String dataView, boolean yesSelfNoOther, Data curr, Data param, boolean execObserver);
    void onLocalAttach();
    void onLocalDetach();
    void onRemoteAttach(IRemoter remoter);
    void onRemoteDetach(IRemoter remoter);
}
