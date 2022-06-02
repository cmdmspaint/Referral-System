package com.ssw.referral.controller;

import com.ssw.referral.common.CommonException;
import com.ssw.referral.common.EmBusinessError;
import com.ssw.referral.common.MessageRes;
import com.ssw.referral.model.CategoryModel;
import com.ssw.referral.model.ShopModel;
import com.ssw.referral.service.CategoryService;
import com.ssw.referral.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("/shop")
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 推荐服务
     * @param longitude  经度
     * @param latitude   纬度
     * @return
     * @throws CommonException
     */
    @RequestMapping("/recommend")
    @ResponseBody
    public MessageRes recommend(@RequestParam(name = "longitude") BigDecimal longitude,
                                @RequestParam(name = "latitude")BigDecimal latitude) throws CommonException {
        if (longitude == null || latitude == null){
            throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        List<ShopModel> shopModelList = shopService.recommend(longitude,latitude);
        return MessageRes.create(shopModelList);
    }

    //搜索服务
    @RequestMapping("/search")
    @ResponseBody
    public MessageRes search(@RequestParam(name = "longitude")BigDecimal longitude,
                             @RequestParam(name = "latitude")BigDecimal latitude,
                             @RequestParam(name = "keyword")String keyword,
                             @RequestParam(name = "orderby", required = false)Integer orderby,
                             @RequestParam(name = "categoryId", required = false)Integer categoryId,
                             @RequestParam(name = "tags", required = false)String tags) throws CommonException, IOException {
        if (keyword.isEmpty() || longitude == null || latitude == null){
            throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //V1.0
        List<ShopModel> shopModelList = shopService.search(longitude,latitude,keyword,orderby,categoryId,tags);

        //V2.0
      /*  Map<String,Object> result = shopService.searchES(longitude,latitude,keyword,orderby,categoryId,tags);
        List<ShopModel> shopModelList = (List<ShopModel>) result.get("shop");
        List<CategoryModel> categoryModelList = categoryService.selectAll();
        List<Map<String, Object>> tagsAggregation = (List<Map<String, Object>>) result.get("tags");*/

        List<CategoryModel> categoryModelList = categoryService.selectAll();
        List<Map<String, Object>> tagsAggregation = shopService.searchGroupByTags(keyword,categoryId,tags);
        //为了以后扩展筛选条件，故不和推荐一样直接返回list的model
        Map<String,Object> resMap = new HashMap<>();
        resMap.put("shop",shopModelList);
        resMap.put("category",categoryModelList);
        resMap.put("tags",tagsAggregation);
        return MessageRes.create(resMap);
    }

}