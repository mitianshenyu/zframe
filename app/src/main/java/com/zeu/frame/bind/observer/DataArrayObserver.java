package com.zeu.frame.bind.observer;

import com.zeu.frame.bind.Data;

/**
 * Created by zeu on 2017/6/7.
 */

public interface DataArrayObserver {
    boolean onChanged(Data[] curr, Data[] last, Data param);
}
