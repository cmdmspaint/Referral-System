package com.ssw.referral.service;

import com.ssw.referral.common.CommonException;
import com.ssw.referral.model.SellerModel;

import java.util.List;

public interface SellerService {

    SellerModel create(SellerModel sellerModel);
    SellerModel get(Integer id);
    List<SellerModel> selectAll();
    SellerModel changeStatus(Integer id,Integer disabledFlag) throws CommonException;

}
