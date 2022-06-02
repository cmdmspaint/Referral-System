package com.ssw.referral.service.Impl;

import com.ssw.referral.common.CommonException;
import com.ssw.referral.common.EmBusinessError;
import com.ssw.referral.dao.SellerModelMapper;
import com.ssw.referral.model.SellerModel;
import com.ssw.referral.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class SellerServiceImpl implements SellerService {

    @Autowired
    SellerModelMapper shellerMapper;

    @Override
    @Transactional
    public SellerModel create(SellerModel sellerModel) {
        sellerModel.setCreatedAt(new Date());
        sellerModel.setUpdatedAt(new Date());
        sellerModel.setRemarkScore(new BigDecimal(0));
        sellerModel.setDisabledFlag(0);
        shellerMapper.insertSelective(sellerModel);
        return get(sellerModel.getId());
    }

    @Override
    public SellerModel get(Integer id) {
        return shellerMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<SellerModel> selectAll() {
        return shellerMapper.selectAll();
    }

    @Override
    public SellerModel changeStatus(Integer id, Integer disabledFlag) throws CommonException {
        SellerModel sellerModel = get(id);
        if(sellerModel == null){
            throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        sellerModel.setDisabledFlag(disabledFlag);
        shellerMapper.updateByPrimaryKeySelective(sellerModel);
        return sellerModel;
    }
}
