package com.dislock.etcd;

/**
 * Created by yeyc on 2017/1/22.
 */
public class EtcdCurrentTest {
    public static void main(String[] args) {
        SimpleExecutor simpleExecutor = new SimpleExecutor(new EtcdDemo(JmhArgument.dislock));
        simpleExecutor.execute(25,60);
    }
}
