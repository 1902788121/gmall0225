package com.atguigu.gmall.order.orderTask;

import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ExpireOrder {

    @Autowired
    OrderService orderService;

    @Scheduled(cron = "0/5 * * * * ?")
    public void work() throws InterruptedException {
        System.out.println("扫描过期订单");
        // orderService.findExpireOrder();
        System.out.println("删除过期订单");
    }


}
