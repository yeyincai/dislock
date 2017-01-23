package com.dislock.etcd;



/**
 * Created by yeyc on 2017/1/22.
 */
public class JmhArgument {
    static final DislockClient dislockClient = DislockClient.build("etcd3://127.0.0.1:2379");

    static final Dislock dislock = dislockClient.getLock("ghgghgfg");

}
