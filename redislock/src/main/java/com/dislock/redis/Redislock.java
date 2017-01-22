package com.dislock.redis;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * Created by yeyc on 2017/1/19.
 */
public class Redislock {
    public static void main(String[] args) {

        Config config = new Config();
        config.useSingleServer().setAddress("192.168.23.133:6379");
        RedissonClient redisson =  Redisson.create(config);

        RLock rLock = redisson.getLock("test");

        SimpleExecutor simpleExecutor = new SimpleExecutor(new RedisDemo(rLock));

        simpleExecutor.execute(25,60);
    }
}
