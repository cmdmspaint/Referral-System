package com.ssw.referral.service;



import com.ssw.referral.common.CommonException;
import com.ssw.referral.model.CategoryModel;

import java.util.List;


public interface CategoryService {

    //创建品类
    CategoryModel create(CategoryModel categoryModel) throws CommonException;

    //根据品类id获取品类
    CategoryModel get(Integer id);

    //后台品类查询管理
    List<CategoryModel> selectAll();

    //统计品类数量
    Integer countAllCategory();
}
