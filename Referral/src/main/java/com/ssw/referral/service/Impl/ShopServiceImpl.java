package com.ssw.referral.service.Impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ssw.referral.common.CommonException;
import com.ssw.referral.common.EmBusinessError;
import com.ssw.referral.dao.ShopModelMapper;
import com.ssw.referral.model.CategoryModel;
import com.ssw.referral.model.SellerModel;
import com.ssw.referral.model.ShopModel;
import com.ssw.referral.service.CategoryService;
import com.ssw.referral.service.SellerService;
import com.ssw.referral.service.ShopService;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class ShopServiceImpl implements ShopService {


    @Autowired
    private ShopModelMapper shopModelMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RestHighLevelClient highLevelClient;

    @Autowired
    private SellerService sellerService;

    @Override
    @Transactional
    public ShopModel create(ShopModel shopModel) throws CommonException {
        shopModel.setCreatedAt(new Date());
        shopModel.setUpdatedAt(new Date());

        SellerModel sellerModel = sellerService.get(shopModel.getSellerId());
        //校验商家是否存在
        if (sellerModel == null){
            throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商户不存在");
        }
        //校验商家是否被禁用
        if (sellerModel.getDisabledFlag() == 1){
            throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商户已禁用");
        }
        //校验类目是否存在
        CategoryModel categoryModel = categoryService.get(shopModel.getCategoryId());
        if (categoryModel == null){
            throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"类目不存在");
        }
        shopModelMapper.insertSelective(shopModel);

        return get(shopModel.getId());
    }

    @Override
    public ShopModel get(Integer id) {
        ShopModel shopModel = shopModelMapper.selectByPrimaryKey(id);
        if (shopModel == null){
            return null;
        }
        //将商品与商家、品类关联（一对二）
        shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
        shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        return shopModel;
    }

    @Override
    public List<ShopModel> selectAll() {
        List<ShopModel> shopModelList = shopModelMapper.selectAll();
        shopModelList.forEach(shopModel -> {
            //将商品与商家、品类关联（一对二）
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });

        return shopModelList;
    }

    @Override
    public Integer countAllShop() {
        return shopModelMapper.countAllShop();
    }

    @Override
    public List<ShopModel> recommend(BigDecimal longitude, BigDecimal latitdue) {
        List<ShopModel> shopModelList =  shopModelMapper.recommend(longitude,latitdue);
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });
        return shopModelList;
    }

    @Override
    public List<ShopModel> search(BigDecimal longitude, BigDecimal latitude, String keyword, Integer orderby, Integer categoryId, String tags) {
        List<ShopModel> shopModelList = shopModelMapper.search(longitude,latitude,keyword,orderby,categoryId,tags);
        //将商品遍历与商家、品类关联（一对二）
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });

        return shopModelList;
    }

    @Override
    public Map<String, Object> searchES(BigDecimal longitude, BigDecimal latitude, String keyword, Integer orderby, Integer categoryId, String tags) throws IOException {
        Map<String,Object> result = new HashMap<>();

/*        SearchRequest searchRequest = new SearchRequest("shop");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("name",keyword));
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);

        List<Integer> shopIdsList = new ArrayList<>();
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit:hits){
            shopIdsList.add(new Integer(hit.getSourceAsMap().get("id").toString()));
        }
        List<ShopModel> shopModelList = shopIdsList.stream().map(id->{
            return get(id);
        }).collect(Collectors.toList());
        result.put("shop",shopModelList);*/
        Request request = new Request("GET","/shop/_search");
        //构建请求
        JSONObject jsonRequestObj = new JSONObject();

        //构建source部分
        jsonRequestObj.put("_source","*");

        //构建自定义距离字段
        jsonRequestObj.put("script_fields",new JSONObject());
        jsonRequestObj.getJSONObject("script_fields").put("distance",new JSONObject());
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").put("script",new JSONObject());
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("source","haversin(lat, lon, doc['location'].lat, doc['location'].lon)");
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("lang","expression");
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("params",new JSONObject());
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .getJSONObject("params").put("lat",latitude);
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .getJSONObject("params").put("lon",longitude);

        //构建query
        Map<String,Object> cixingMap = analyzeCategoryKeyword(keyword);
        //召回属于伤筋动骨操作，尽量宽松一些避免返回空，然后使用排序展示给用户，优先使用排序再使用召回
        //影响召回的策略
        boolean isAffectFilter = false;
        //影响排序
        boolean isAffectOrder =  true;


        jsonRequestObj.put("query",new JSONObject());

        //构建function score
        jsonRequestObj.getJSONObject("query").put("function_score",new JSONObject());

        //构建function score内的query
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("query",new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").put("bool",new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool").put("must",new JSONArray());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").add(new JSONObject());

        //构建match query

        //must数组内jsonarry的偏移量
        //使queryIndex++就可以不断拓展筛选条件
        int queryIndex = 0;

        if(cixingMap.keySet().size() > 0 && isAffectFilter){
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("bool",new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").put("should", new JSONArray());
            int filterQueryIndex = 0;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .put("match",new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .getJSONObject("match").put("name",new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .getJSONObject("match").getJSONObject("name").put("query",keyword);
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .getJSONObject("match").getJSONObject("name").put("boost",0.1);
            for(String key : cixingMap.keySet()) {
                filterQueryIndex++;
                Integer cixingCategoryId = (Integer) cixingMap.get(key);
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").add(new JSONObject());
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .put("term", new JSONObject());
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .getJSONObject("term").put("category_id", new JSONObject());
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .getJSONObject("term").getJSONObject("category_id").put("value", cixingCategoryId);
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .getJSONObject("term").getJSONObject("category_id").put("boost", 0);
            }
        }else{
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("match",new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").put("name", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").getJSONObject("name").put("query",keyword);
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").getJSONObject("name").put("boost",0.1);

        }

        //第二个偏移量
        queryIndex++;
        //构建第二个query的条件
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").add(new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").getJSONObject(queryIndex).put("term",new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("seller_disabled_flag",0);

        //标签搜索
        if(tags != null){
            queryIndex++;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("term",new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("tags",tags);
        }
        //分类搜索
        if(categoryId != null){
            queryIndex++;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("term",new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("category_id",categoryId);
        }


        //构建functions部分
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("functions",new JSONArray());

        int functionIndex = 0;
        if(orderby == null) {
            //if中的为默认排序
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("gauss", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss").put("location", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("origin", latitude.toString() + "," + longitude.toString());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("scale", "100km");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("offset", "0km");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("decay", "0.5");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 9);

            functionIndex++;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                    .put("field", "remark_score");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 0.2);

            functionIndex++;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                    .put("field", "seller_remark_score");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 0.1);


            if(cixingMap.keySet().size() > 0 && isAffectOrder){
                for(String key : cixingMap.keySet()){
                    functionIndex++;
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("filter",new JSONObject());
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("filter")
                            .put("term",new JSONObject());
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("filter")
                            .getJSONObject("term").put("category_id",cixingMap.get(key));
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight",3);

                }

            }
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("score_mode","sum");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("boost_mode","sum");
        }else{
            //else中为低价排序
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor",new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                    .put("field","price_per_man");

            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("score_mode","sum");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("boost_mode","replace");
        }

        //排序字段
        jsonRequestObj.put("sort",new JSONArray());
        jsonRequestObj.getJSONArray("sort").add(new JSONObject());
        jsonRequestObj.getJSONArray("sort").getJSONObject(0).put("_score",new JSONObject());
        if(orderby == null){
            jsonRequestObj.getJSONArray("sort").getJSONObject(0).getJSONObject("_score").put("order","desc");
        }else{
            jsonRequestObj.getJSONArray("sort").getJSONObject(0).getJSONObject("_score").put("order","asc");
        }

        //聚合字段
        jsonRequestObj.put("aggs",new JSONObject());
        jsonRequestObj.getJSONObject("aggs").put("group_by_tags",new JSONObject());
        jsonRequestObj.getJSONObject("aggs").getJSONObject("group_by_tags").put("terms",new JSONObject());
        jsonRequestObj.getJSONObject("aggs").getJSONObject("group_by_tags").getJSONObject("terms").put("field","tags");



        String reqJson = jsonRequestObj.toJSONString();


        //System.out.println(reqJson);
        request.setJsonEntity(reqJson);
        Response response = highLevelClient.getLowLevelClient().performRequest(request);
        String responseStr = EntityUtils.toString(response.getEntity());
        //System.out.println(responseStr);
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        //取返回结果中的hits字段
        JSONArray jsonArr = jsonObject.getJSONObject("hits").getJSONArray("hits");
        List<ShopModel> shopModelList = new ArrayList<>();
        for(int i = 0; i < jsonArr.size(); i++){
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            Integer id = new Integer(jsonObj.get("_id").toString());
            BigDecimal distance = new BigDecimal(jsonObj.getJSONObject("fields").getJSONArray("distance").get(0).toString());
            ShopModel shopModel = get(id);
            shopModel.setDistance(distance.multiply(new BigDecimal(1000).setScale(0,BigDecimal.ROUND_CEILING)).intValue());
            shopModelList.add(shopModel);
        }
        List<Map> tagsList = new ArrayList<>();
        JSONArray tagsJsonArray = jsonObject.getJSONObject("aggregations").getJSONObject("group_by_tags").getJSONArray("buckets");
        for(int i = 0; i < tagsJsonArray.size();i++){
            JSONObject jsonObj = tagsJsonArray.getJSONObject(i);
            Map<String,Object> tagMap = new HashMap<>();
            tagMap.put("tags",jsonObj.getString("key"));
            tagMap.put("num",jsonObj.getInteger("doc_count"));
            tagsList.add(tagMap);
        }
        result.put("tags",tagsList);

        result.put("shop",shopModelList);
        return result;

    }

    //构造分词函数识别器
    private Map<String,Object> analyzeCategoryKeyword(String keyword) throws IOException {
        Map<String,Object> res = new HashMap<>();

        Request request = new Request("GET","/shop/_analyze");
        request.setJsonEntity("{" + "  \"field\": \"name\"," + "  \"text\":\""+keyword+"\"\n" + "}");
        Response response = highLevelClient.getLowLevelClient().performRequest(request);
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        JSONArray jsonArray = jsonObject.getJSONArray("tokens");
        for(int i = 0; i < jsonArray.size(); i++){
            String token = jsonArray.getJSONObject(i).getString("token");
            Integer categoryId = getCategoryIdByToken(token);
            if(categoryId != null){
                res.put(token,categoryId);
            }
        }
        return res;
    }
    private Map<Integer,List<String>> categoryWorkMap = new HashMap<>();

    //在spring容器初始化的时候执行该方法
    @PostConstruct
    public void init(){
        // 根据对应的categoryId 区分类目 然后设置对应的关键词匹配
        // 1美食 2酒店 输入关键词即可 查找对应的类目信息
        categoryWorkMap.put(1,new ArrayList<>());
        categoryWorkMap.put(2,new ArrayList<>());

        categoryWorkMap.get(1).add("吃饭");
        categoryWorkMap.get(1).add("下午茶");

        categoryWorkMap.get(2).add("休息");
        categoryWorkMap.get(2).add("睡觉");
        categoryWorkMap.get(2).add("住宿");

    }

    private Integer getCategoryIdByToken(String token){
        for(Integer key : categoryWorkMap.keySet()){
            List<String> tokenList = categoryWorkMap.get(key);
            if(tokenList.contains(token)){
                return key;
            }
        }
        return null;
    }


    @Override
    public List<Map<String, Object>> searchGroupByTags(String keyword, Integer categoryId, String tags) {
        return null;
    }
}
