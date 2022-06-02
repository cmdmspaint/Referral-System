package com.ssw.referral.controller;

import com.ssw.referral.common.MessageRes;
import com.ssw.referral.model.CategoryModel;
import com.ssw.referral.service.CategoryService;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 *   客户端品类服务
 */
@Controller("/category")
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //查询所有品类用于展示
    @ResponseBody
    @RequestMapping("/list")
    public MessageRes list(){
        List<CategoryModel> categoryModelList = categoryService.selectAll();
        return MessageRes.create(categoryModelList);
    }
}
