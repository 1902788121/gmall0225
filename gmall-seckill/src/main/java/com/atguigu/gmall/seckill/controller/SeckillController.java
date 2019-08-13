package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("seckill")
    @ResponseBody
    public String seckill() {
        RSemaphore semaphore = redissonClient.getSemaphore("sku:1:stock");
        boolean b = semaphore.tryAcquire();// 库存减一

        if (b) {
            Jedis jedis = null;
            try {
                jedis = redisUtil.getJedis();
                String s = jedis.get("sku:1:stock");
                System.out.println(Thread.currentThread().getName() + "抢购成功,目前库存数量:" + s);
            } finally {
                jedis.close();
            }
            return "抢购成功";
        } else {
            return "抢购失败";
        }

    }

    @RequestMapping("kill")
    @ResponseBody
    public String kill() {
        String memberId = "1";
        System.out.println(Thread.currentThread().getName() + "开始抢购。。");

        // 抢库存
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            // 加入一个setexnx限制，限制单个用户的抢购频率
            String stock = jedis.get("sku:1:stock");
            if (Long.parseLong(stock) > 0) {
                jedis.watch("sku:1:stock");
                Transaction multi = jedis.multi();
                multi.incrBy("sku:1:stock", -1);
                List<Object> exec = multi.exec();
                if (exec != null && exec.size() > 0) {
                    Long i = (Long) exec.get(0);
                    System.out.println(Thread.currentThread().getName() + "抢购成功。目前库存数量：" + i);
                    return "抢购成功";
                } else {
                    System.out.println(Thread.currentThread().getName() + "抢购失败");
                    return "抢购失败";
                }
            } else {
                System.out.println(Thread.currentThread().getName() + "目前库存数量为0，抢购结束");
                return "活动结束";
            }
        } finally {
            jedis.close();
        }

    }
}
