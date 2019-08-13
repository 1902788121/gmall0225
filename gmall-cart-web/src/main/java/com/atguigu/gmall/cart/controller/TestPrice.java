package com.atguigu.gmall.cart.controller;

import java.math.BigDecimal;

public class TestPrice {

    public static void main(String[] args) {

        // 数字的初始化
        BigDecimal b1= new BigDecimal("0.01");
        BigDecimal b2 = new BigDecimal(0.01f);
        BigDecimal b3 = new BigDecimal(0.01d);
        System.out.println(b1);
        System.out.println(b2);
        System.out.println(b3);

        // 数字的比较
        int i = b2.compareTo(b3);// -1 0 1
        System.out.println(i);

        // 加减乘除
        BigDecimal b4 = new BigDecimal("6");
        BigDecimal b5 = new BigDecimal("7");
        System.out.println(b4.add(b5));
        System.out.println(b4.subtract(b5));
        System.out.println(b4.multiply(b5));


        // 约等于
        BigDecimal divide = b4.divide(b5, 15, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(divide);

        BigDecimal add = b2.add(b3);
        BigDecimal bigDecimal = add.setScale(3, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(bigDecimal);

    }

}
