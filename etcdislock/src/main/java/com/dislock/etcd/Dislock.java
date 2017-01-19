package com.dislock.etcd;


import java.util.concurrent.TimeUnit;

public interface Dislock {

    void lock();

    boolean tryLock();

    boolean tryLock(long timeout, TimeUnit unit);

    void unlock();

}
