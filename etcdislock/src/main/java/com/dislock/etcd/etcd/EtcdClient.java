package com.dislock.etcd.etcd;

import com.coreos.jetcd.api.KVGrpc;
import com.coreos.jetcd.api.LeaseGrpc;
import com.coreos.jetcd.api.WatchGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;


public class EtcdClient {
    private KVGrpc.KVBlockingStub kvBlockingStub;
    private WatchGrpc.WatchStub watchStub;
    private LeaseGrpc.LeaseBlockingStub leaseBlockingStub;


    public EtcdClient(String url) {
        final ManagedChannel channel = channelBuild(url);
        this.kvBlockingStub = KVGrpc.newBlockingStub(channel);
        this.watchStub = WatchGrpc.newStub(channel);
        this.leaseBlockingStub = LeaseGrpc.newBlockingStub(channel);
    }


    private ManagedChannel channelBuild(String url) {
        return NettyChannelBuilder.forTarget(url).nameResolverFactory(new EtcdNameResolverProvider()).usePlaintext(true).build();
    }


    public KVGrpc.KVBlockingStub getKvBlockingStub() {
        return kvBlockingStub;
    }

    public WatchGrpc.WatchStub getWatchStub() {
        return watchStub;
    }

    public LeaseGrpc.LeaseBlockingStub getLeaseBlockingStub() {
        return leaseBlockingStub;
    }
}
