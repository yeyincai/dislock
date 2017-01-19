package com.dislock.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

/**
 * Created by yeyc on 2017/1/19.
 */
public class Redislock {
    public static void main(String[] args) {

        RedissonClient redisson =  Redisson.create();

        redisson.getLock("");
    }
}
