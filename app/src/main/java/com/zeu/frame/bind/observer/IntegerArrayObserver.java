package com.zeu.frame.bind.observer;

import com.zeu.frame.bind.Data;

/**
 * Created by zeyu on 2016/12/14.
 */

public interface IntegerArrayObserver {
    boolean onChanged(int[] curr, int[] last, Data param);
}
