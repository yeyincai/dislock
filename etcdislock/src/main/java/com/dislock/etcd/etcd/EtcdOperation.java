package com.dislock.etcd.etcd;

import com.coreos.jetcd.api.*;
import com.google.protobuf.ByteString;


public class EtcdOperation {

    private EtcdClient etcdClient;

    private static final long LEASE_EXPIRED_SECONDS = 5;
    final static String PRE = "ETCD_DISLOCK_";
    private final static String DEFAULT_VALUE = "EXIST";


    public EtcdOperation(EtcdClient etcdClient){
        this.etcdClient = etcdClient;
    }


    /**
     * 获取锁,返回true的话说明获取到了锁，false的话说明没有获取到锁
     *
     * @param key 加锁的key
     * @return boolean
     */
    public boolean acquire(final String key) {
        //获取租约
        final LeaseGrantResponse leaseGrantResponse = buildLeaseGrantResponse();
        //创建key值
        final ByteString keyByteString = ByteString.copyFromUtf8(PRE.concat(key));
        final ByteString valueByteString = ByteString.copyFromUtf8(DEFAULT_VALUE);
        final PutRequest putRequest = buildPutRequest(keyByteString, valueByteString, leaseGrantResponse.getID());
        //通过比较某个key的值是否相等来判断是否有创建该key
        //如果比较失败，可以认为没有该节点，接着创建该节点
        final TxnRequest txnRequest = TxnRequest.newBuilder().addCompare(buildCompare(keyByteString, valueByteString))
                .addFailure(buildPutRequestByRequestOp(putRequest)).build();
        final TxnResponse txn = etcdClient.getKvBlockingStub().txn(txnRequest);
        return !txn.getSucceeded();
    }

    /**
     * 释放锁
     * @param key 加锁的key
     * @return boolean
     */
    public boolean release(final String key) {
        //删除key,采用比较器去删除，如果value相同，则删除该key
        final ByteString keyByteString = ByteString.copyFromUtf8(PRE.concat(key));
        final ByteString valueByteString = ByteString.copyFromUtf8(DEFAULT_VALUE);
        final DeleteRangeRequest deleteRangeRequest = buildDeleteRangeRequest(keyByteString);

        final TxnRequest txnRequest = TxnRequest.newBuilder().addCompare(buildCompare(keyByteString, valueByteString))
                .addSuccess(buildDeleteRangeRequestByRequestOp(deleteRangeRequest)).build();

        final TxnResponse txn = etcdClient.getKvBlockingStub().txn(txnRequest);
        return txn.getSucceeded();
    }


    private LeaseGrantResponse buildLeaseGrantResponse() {
        return etcdClient.getLeaseBlockingStub().leaseGrant(LeaseGrantRequest.newBuilder().setTTL(LEASE_EXPIRED_SECONDS).build());
    }

    private PutRequest buildPutRequest(final ByteString key, final ByteString value, long leaseId) {
        return PutRequest.newBuilder().setKey(key).setValue(value).setLease(leaseId).build();
    }

    private Compare buildCompare(final ByteString key, final ByteString value) {
        //创建value值的比较器
        return Compare.newBuilder().setKey(key).setTarget(Compare.CompareTarget.VALUE)
                .setResult(Compare.CompareResult.EQUAL).setValue(value).build();
    }

    private RequestOp buildPutRequestByRequestOp(final PutRequest putRequest) {
        return RequestOp.newBuilder().setRequestPut(putRequest).build();
    }

    private RequestOp buildDeleteRangeRequestByRequestOp(final DeleteRangeRequest deleteRangeRequest) {
        return RequestOp.newBuilder().setRequestDeleteRange(deleteRangeRequest).build();
    }

    private DeleteRangeRequest buildDeleteRangeRequest(final ByteString key) {
        return DeleteRangeRequest.newBuilder().setKey(key).build();
    }
}
