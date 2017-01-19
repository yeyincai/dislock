package com.dislock.etcd;

import java.util.concurrent.TimeUnit;


public class DislockBuilder {


    private  class DislockImpl implements Dislock{
        @Override
        public void lock() {
        }

        @Override
        public boolean tryLock() {
            return false;
        }

        @Override
        public boolean tryLock(long timeout, TimeUnit unit) {
            return false;
        }

        @Override
        public void unlock() {
        }
    }
}
