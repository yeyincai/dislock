package com.dislock.etcd.etcd;

import com.coreos.jetcd.api.KVGrpc;
import com.coreos.jetcd.api.LeaseGrpc;
import com.coreos.jetcd.api.WatchGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

/**
 * Created by yeyc on 2017/1/19.
 */
public class EtcdClient {
    private  KVGrpc.KVBlockingStub kvBlockingStub;
    private  WatchGrpc.WatchBlockingStub watchBlockingStub;
    private  LeaseGrpc.LeaseBlockingStub leaseBlockingStub;


    public EtcdClient(String dnsName){
        final ManagedChannel channel = channelBuild(dnsName);
        this.kvBlockingStub = KVGrpc.newBlockingStub(channel);
        this.watchBlockingStub = WatchGrpc.newBlockingStub(channel);
        this.leaseBlockingStub = LeaseGrpc.newBlockingStub(channel);
    }


    private ManagedChannel channelBuild(String dnsName){
        //use grpc default DnsNameResolverProvider,so you  should be set etcd real multiple host:port  to   hosts
        return NettyChannelBuilder.forTarget(dnsName).usePlaintext(true).build();
    }


    public KVGrpc.KVBlockingStub getKvBlockingStub() {
        return kvBlockingStub;
    }

    public WatchGrpc.WatchBlockingStub getWatchBlockingStub() {
        return watchBlockingStub;
    }

    public LeaseGrpc.LeaseBlockingStub getLeaseBlockingStub() {
        return leaseBlockingStub;
    }
}
