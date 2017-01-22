package com.dislock.etcd;

/**
 * Created by yeyc on 2017/1/22.
 */
public class EtcdDemo implements Runnable{

    private Dislock dislock;

    public EtcdDemo(Dislock dislock){
        this.dislock = dislock;
    }

    @Override
    public void run() {
        dislock.lock();
        try {
            int j=0;
            for (int i = 0; i < 10; i++) {
               j++;
            }
        }finally {
            dislock.unlock();
        }
    }
}