package com.dislock.etcd.etcd;

import com.coreos.jetcd.api.*;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class EtcdWatch {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final EtcdClient etcdClient;
    private final EventBus eventBus;
    private final String key;
    private final LockListener listener;

    public EtcdWatch(String key,EtcdClient etcdClient,LockListener listener) {
        this.etcdClient = etcdClient;
        this.eventBus = new EventBus("dislock_".concat(key));
        this.eventBus.register(new WatchErrorHandler());

        this.key = key;
        this.listener = listener;
    }

    public void watch( ) {
        final ByteString byteStringKey  = ByteString.copyFromUtf8(key);

        final StreamObserver<WatchRequest> watch = etcdClient.getWatchStub().watch(new StreamObserver<WatchResponse>() {
            @Override
            public void onNext(WatchResponse value) {
                if (!value.getCanceled() && !value.getCreated()) {
                    List<Event> eventsList = value.getEventsList();
                    eventsList.forEach(event -> {
                        if (event.getType() == Event.EventType.DELETE) {
                            listener.releasedNotify();
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("watch lock err!", t);
                eventBus.post(new WatchErrorEvent());
            }

            @Override
            public void onCompleted() {
                logger.debug("watch lock completed!");
            }
        });

        WatchCreateRequest createRequest = WatchCreateRequest.newBuilder().setKey(byteStringKey).build();
        watch.onNext(WatchRequest.newBuilder().setCreateRequest(createRequest).build());
    }


    private class WatchErrorHandler {
        private static final long DELAY_TIME = 10L;
        @Subscribe
        @AllowConcurrentEvents
        void listener() throws InterruptedException {
            TimeUnit.SECONDS.sleep(DELAY_TIME);
            watch();
        }
    }

    private class WatchErrorEvent {
        WatchErrorEvent() {
        }
    }

}
