package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.*;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckedBySkuId(String spuId, String skuId) {

        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsSkuSaleAttrValueMapper.selectSpuSaleAttrListCheckedBySkuId(spuId, skuId);

        return pmsProductSaleAttrs;
    }


    @Override
    public List<PmsSkuInfo> checkSkuBySpuId(String spuId) {
        List<PmsSkuInfo> PmsSkuInfos = pmsSkuSaleAttrValueMapper.selectCheckSkuBySpuId(spuId);
        return PmsSkuInfos;
    }

    @Override
    public String checkSkuByValueIdsTwo(String[] ids) {
        List<PmsSkuInfo> PmsSkuInfos = pmsSkuSaleAttrValueMapper.selectCheckSkuByValueIdsTwo(StringUtils.join(ids, ","));

        // 用销售属性值的组合当作key，用skuId当作value制作一个hash表
        HashMap<String, String> skuMap = new HashMap<String, String>();
        for (PmsSkuInfo pmsSkuInfo : PmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();

            String valueIds = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                valueIds = valueIds + "|" + pmsSkuSaleAttrValue.getSaleAttrValueId();
            }
            skuMap.put(valueIds, skuId);
        }

        String valueIds = "";
        for (String id : ids) {
            valueIds = valueIds + "|" + id;
        }
        String itemSkuId = skuMap.get(valueIds);
        return itemSkuId;

    }

    @Override
    public String checkSkuByValueIds(String[] ids) {
        // 检查当前spu的销售属性值的组合是否曾经添加过，做sku是否重复的检查
        String skuId = null;
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuSaleAttrValueMapper.selectCheckSkuByValueIds(StringUtils.join(ids, ","));
        List<String> skuIds = new ArrayList<>();
        if (pmsSkuSaleAttrValues != null && pmsSkuSaleAttrValues.size() > 0) {
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {
                skuIds.add(pmsSkuSaleAttrValue.getSkuId());
            }
        }

        // 用销售属性值的组合当作key，用skuId当作value制作一个hash表
        HashMap<String, String> skuMap = new HashMap<String, String>();
        for (String skuIdFromList : skuIds) {
            String valueIdHashStr = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {
                String skuIdFromDb = pmsSkuSaleAttrValue.getSkuId();
                if (skuIdFromDb.equals(skuIdFromList)) {
                    valueIdHashStr = valueIdHashStr + "|" + pmsSkuSaleAttrValue.getSaleAttrValueId();
                }
            }
            skuMap.put(valueIdHashStr, skuIdFromList);
        }
        System.out.println("你的数据库中的所有有关的属性值和sku对应的hash表：" + skuMap);

        String valueIds = "";
        for (String id : ids) {
            valueIds = valueIds + "|" + id;
        }

        String itemSkuId = skuMap.get(valueIds);

        if (itemSkuId != null && !itemSkuId.equals("")) {
            skuId = itemSkuId;
        }

        System.out.println("你的销售属性值的组合id:" + valueIds);


        return skuId;
    }

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String sku_id = pmsSkuInfo.getId();

        // 保存图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(sku_id);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        // 保存平台属性
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(sku_id);
            skuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        // 保存销售属性
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(sku_id);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);

        }


    }


    @Override
    public PmsSkuInfo getSkuByIdFromDb(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        pmsSkuInfo1.setSkuImageList(pmsSkuImages);
        return pmsSkuInfo1;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId, String ip) {
        System.out.println("ip:" + ip + Thread.currentThread().getName() + "进入商品:" + skuId + "详情的请求");
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        // 查询商品缓存
        Jedis jedis = redisUtil.getJedis();
        try {
            // 如果缓存存在直接返回数据
            String skuJsonStr = jedis.get("sku:" + skuId + ":info");
            if (StringUtils.isBlank(skuJsonStr)) {
                System.out.println("ip:" + ip + Thread.currentThread().getName() + "缓存中没有商品:" + skuId + "，准备请求mysql");

                // 如果缓存不存在，查询mysql
                String delCode = UUID.randomUUID().toString();
                String OK = jedis.set("sku:" + skuId + ":lock", delCode, "nx", "px", 3000);
                if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
                    System.out.println("ip:" + ip + Thread.currentThread().getName() + "获得:" + skuId + "分布式锁，开始请求mysql");

                    pmsSkuInfo = getSkuByIdFromDb(skuId);
                    if (pmsSkuInfo != null) {

                        // 查询结果返回数据给用户，同时同步redis缓存
                        jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));
                    }
                    // 删除分布式锁
                    System.out.println("ip:" + ip + Thread.currentThread().getName() + "请求mysql成功，归还" + skuId + "的分布式锁");
                    String v = jedis.get("sku:" + skuId + ":lock");
                    //String luaScript ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    //Object eval = jedis.eval(luaScript, Collections.singletonList("sku:" + skuId + ":lock"), Collections.singletonList(delCode));
                    if (StringUtils.isNotBlank(v) && v.equals(delCode)) {
                        jedis.del("sku:" + skuId + ":lock");
                    }
                } else {
                    System.out.println("ip:" + ip + Thread.currentThread().getName() + "没有获得:" + skuId + "分布式锁，开始自旋。。。。。");

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 等待三秒后自旋
                    return getSkuById(skuId, ip);
                }

            } else {
                pmsSkuInfo = JSON.parseObject(skuJsonStr, PmsSkuInfo.class);
                System.out.println("ip:" + ip + Thread.currentThread().getName() + "从缓存中获取商品:" + skuId + "的数据");

            }
        } finally {
            jedis.close();
        }
        System.out.println("ip:" + ip + Thread.currentThread().getName() + "请求结束");

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> select = skuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(select);
        }

        return pmsSkuInfos;
    }
}
