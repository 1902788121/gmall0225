package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.beans.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    UmsMember getUserById(String uid);

    UmsMember login(UmsMember umsMember);

    void putToken(String gmallToken,String memberId);

    List<UmsMemberReceiveAddress> getMemberAddressesById(String memberId);

    UmsMember addOauthUser(UmsMember umsMember);

    UmsMemberReceiveAddress getMemberAddressesByAddressId(String receiveAddressId);
}
