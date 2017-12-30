package com.zeu.frame.bind.observer;

import com.zeu.frame.bind.Data;
import com.zeu.frame.bind.Packet;

/**
 * Created by zeu on 2017/6/7.
 */

public interface PacketArrayObserver {
    boolean onChanged(Packet[] curr, Packet[] last, Data param);
}
