package com.dislock.etcd;

import com.dislock.etcd.etcd.EtcdClient;
import com.dislock.etcd.etcd.EtcdOperation;
import com.dislock.etcd.etcd.EtcdWatch;
import com.dislock.etcd.etcd.LockListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class DislockImpl implements Dislock,LockListener{

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final String key;
    private final EtcdOperation etcdOperation;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notify = lock.newCondition();


    DislockImpl(String key,EtcdClient etcdClient){
        this.key = key;
        this.etcdOperation = new EtcdOperation(etcdClient);
        EtcdWatch etcdWatch = new EtcdWatch(key, etcdClient, this);
        etcdWatch.watch();
    }

    @Override
    public void lock() {
        final Lock LockTemp = this.lock;
        //防止客户端太多竞争者，冲垮etcd
        LockTemp.lock();
        try {
            //如果没有获取到锁，等其它人释放
            if(etcdOperation.acquire(key)){
                try {
                    //阻塞等待有人释放锁
                    notify.await();
                } catch (InterruptedException e) {
                    logger.error("key={}等待获取锁失败",key,e);
                }
                //继续获取
                lock();
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void unlock() {
        final Lock LockTemp = this.lock;
        LockTemp.lock();
        try {
            //释放锁
            if(!etcdOperation.release(key)){
                logger.error("key={},释放锁失败" ,key );
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void releasedNotify() {
        final Lock LockTemp = this.lock;
        LockTemp.lock();
        try {
            notify.signalAll();
        }finally {
            lock.unlock();
        }
    }
}