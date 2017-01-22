package com.dislock.redis;

import org.redisson.api.RLock;

/**
 * Created by yeyc on 2017/1/22.
 */
public class RedisDemo implements Runnable{

    private RLock rLock;
    public RedisDemo(RLock rLock){
        this.rLock = rLock;
    }

    @Override
    public void run() {
        rLock.lock();
        try {
            int j=0;
            for (int i = 0; i < 10; i++) {
               j++;
            }
        }finally {
            rLock.unlock();
        }
    }
}