package com.zeu.frame.bind.observer;

import com.zeu.frame.bind.Data;

/**
 * Created by zeyu on 2016/12/14.
 */

public interface BooleanObserver {
    boolean onChanged(boolean curr, boolean last, Data param);
}
