package com.ssw.referral.service;

import com.ssw.referral.common.CommonException;
import com.ssw.referral.model.UserModel;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * 使用接口的好处
 * 方便替换 只要方法实现了UserService接口 可以有service1 service2
 * 多态+接口抽象的思想
 */
public interface UserService {

    UserModel getUser(Integer id);

    UserModel register(UserModel registerUser) throws CommonException, UnsupportedEncodingException, NoSuchAlgorithmException;

    UserModel login(String telphone,String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, CommonException;

    Integer countAllUser();

}
