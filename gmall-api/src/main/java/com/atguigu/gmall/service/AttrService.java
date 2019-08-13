package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.PmsBaseAttrInfo;

import java.util.HashSet;
import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrInfo> getAttrValueByValueIds(HashSet<String> valueIdSet);
}
