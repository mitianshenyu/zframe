package com.zeu.frame.bind.observer;

import com.zeu.frame.bind.Data;

/**
 * Created by zeyu on 2016/12/14.
 */

public interface CharacterObserver {
    boolean onChanged(char curr, char last, Data param);
}
