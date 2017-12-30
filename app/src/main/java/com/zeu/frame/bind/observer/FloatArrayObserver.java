package com.zeu.frame.bind.observer;
import com.zeu.frame.bind.Data;
/**
 * Created by zeyu on 2016/12/14.
 */

public interface FloatArrayObserver {
    boolean onChanged(float[] curr, float[] last, Data param);
}
