package com.dislock.etcd.etcd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.grpc.*;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;

import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class EtcdNameResolverProvider extends NameResolverProvider {

    private static final String SCHEME = "etcd3";

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 1;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        if (SCHEME.equals(targetUri.getScheme())) {
            String targetPath = Preconditions.checkNotNull(targetUri.getAuthority(), "targetPath must be not null");
            return new EtcdNameResolver(targetPath);
        }
        return null;
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }

    private class EtcdNameResolver extends NameResolver {

        private final static int DEFAULT_ETCD_PORT = 2379;
        private final String authority;
        private final String host;
        private final SharedResourceHolder.Resource<ExecutorService> executorResource;

        @GuardedBy("this")
        private boolean shutdown;
        @GuardedBy("this")
        private boolean resolving;
        @GuardedBy("this")
        private Listener listener;

        @GuardedBy("this")
        private ExecutorService executor;

        EtcdNameResolver(String target) {
            URI nameUri = URI.create("//" + target);
            this.authority = nameUri.getAuthority();
            this.host = target;
            this.executorResource = GrpcUtil.SHARED_CHANNEL_EXECUTOR;
            executor = SharedResourceHolder.get(executorResource);
        }

        @Override
        public String getServiceAuthority() {
            return authority;
        }

        @Override
        public void start(Listener listener) {
            Preconditions.checkState(this.listener == null, "already started");
            this.listener = Preconditions.checkNotNull(listener, "listener");
            resolve();
        }

        @Override
        public final synchronized void refresh() {
            Preconditions.checkState(listener != null, "not started");
            resolve();
        }

        @Override
        public final synchronized void shutdown() {
            if (shutdown) {
                return;
            }
            shutdown = true;
            if (executor != null) {
                executor = SharedResourceHolder.release(executorResource, executor);
            }
        }

        @GuardedBy("this")
        private void resolve() {
            if (resolving || shutdown) {
                return;
            }
            executor.execute(resolutionRunnable);
        }

        private final Runnable resolutionRunnable = new Runnable() {
            @Override
            public void run() {
                InetSocketAddress[] inetSocketAddresses;
                Listener savedListener;
                synchronized (EtcdNameResolver.this) {
                    if (shutdown) {
                        return;
                    }
                    savedListener = listener;
                    resolving = true;
                }
                try {
                    try {
                        inetSocketAddresses = getAllByName(host);
                    } catch (Exception e) {
                        savedListener.onError(Status.UNAVAILABLE.withCause(e));
                        return;
                    }
                    List<ResolvedServerInfo> servers =
                            new ArrayList<>(inetSocketAddresses.length);
                    for (InetSocketAddress inetSocketAddress : inetSocketAddresses) {
                        servers.add(new ResolvedServerInfo(
                                new InetSocketAddress(inetSocketAddress.getHostName(), inetSocketAddress.getPort()),
                                Attributes.EMPTY));
                    }
                    //随机排序
                    Collections.shuffle(servers);
                    savedListener.onUpdate(
                            Collections.singletonList(servers), Attributes.EMPTY);
                } finally {
                    synchronized (EtcdNameResolver.this) {
                        resolving = false;
                    }
                }
            }
        };

        @VisibleForTesting
        InetSocketAddress[] getAllByName(String host) {
            String[] hostArray = host.split(",");
            InetSocketAddress[] inetAddresses = new InetSocketAddress[hostArray.length];
            for (int i = 0; i < inetAddresses.length; i++) {
                String[] temp = hostArray[i].split(":");
                int port = DEFAULT_ETCD_PORT;
                if (temp.length > 1) {
                    try {
                        port = Integer.parseInt(temp[1].trim());
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("fail to format port for curr host " + hostArray[i]);
                    }
                }
                inetAddresses[i] = InetSocketAddress.createUnresolved(temp[0].trim(), port);
            }
            return inetAddresses;
        }

    }
}
