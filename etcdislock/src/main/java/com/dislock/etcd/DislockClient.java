package com.dislock.etcd;


import com.dislock.etcd.etcd.EtcdClient;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class DislockClient {

    private EtcdClient etcdClient;

    private DislockClient(String etcdUrl){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(etcdUrl),"etcdUrl is not null or empty");
        this.etcdClient = new EtcdClient(etcdUrl);
    }

    public static DislockClient build(String etcdUrl){
        return  new DislockClient(etcdUrl);
    }

    public Dislock getLock(String key){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key),"key is not null  or empty");
        return new DislockImpl(key,etcdClient);
    }

}
