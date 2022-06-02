package com.ssw.referral.service.Impl;

import com.ssw.referral.common.CommonException;
import com.ssw.referral.common.CommonUtil;
import com.ssw.referral.common.EmBusinessError;
import com.ssw.referral.dao.UserModelMapper;
import com.ssw.referral.model.UserModel;
import com.ssw.referral.service.UserService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserModelMapper userModelMapper;

    @Override
    public UserModel getUser(Integer id) {
        return userModelMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public UserModel register(UserModel registerUser) throws CommonException, UnsupportedEncodingException, NoSuchAlgorithmException {
        registerUser.setPassword(CommonUtil.encodeByMd5(registerUser.getPassword()));
        registerUser.setCreateAt(new Date());
        registerUser.setUpdatedAt(new Date());

        try {
            userModelMapper.insertSelective(registerUser);
        }catch (DuplicateKeyException ex){
            throw new CommonException(EmBusinessError.REGISTER_DUP_FAIL);
        }

        return getUser(registerUser.getId());
    }

    @Override
    public UserModel login(String telphone, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, CommonException {
        UserModel userModel = userModelMapper.selectByTelphoneAndPassword(telphone, CommonUtil.encodeByMd5(password));
        if (userModel == null){
            throw new CommonException(EmBusinessError.LOGIN_FAIL);
        }
        return userModel;
    }

    @Override
    public Integer countAllUser() {
        return userModelMapper.countAllUser();
    }


}
