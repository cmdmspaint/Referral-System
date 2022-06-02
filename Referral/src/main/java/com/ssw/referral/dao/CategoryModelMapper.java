package com.ssw.referral.dao;



import com.ssw.referral.model.CategoryModel;

import java.util.List;

public interface CategoryModelMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(CategoryModel record);

    int insertSelective(CategoryModel record);

    List<CategoryModel> selectAll();

    Integer countAllCategory();

    CategoryModel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CategoryModel record);

    int updateByPrimaryKey(CategoryModel record);
}