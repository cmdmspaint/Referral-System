package com.ssw.referral.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ssw.referral.common.*;
import com.ssw.referral.model.SellerModel;
import com.ssw.referral.request.PageQuery;
import com.ssw.referral.request.SellerCreateReq;
import com.ssw.referral.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@Controller("/admin/seller")
@RequestMapping("/admin/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    //商户列表
    @RequestMapping("/index")
    @AdminPermission
    public ModelAndView index(PageQuery pageQuery){ //
        //分页精髓所在
        PageHelper.startPage(pageQuery.getPage(),pageQuery.getSize());
        List<SellerModel> sellerModelList = sellerService.selectAll();

        PageInfo<SellerModel> sellerModelPageInfo = new PageInfo<>(sellerModelList);
        ModelAndView modelAndView = new ModelAndView("admin/seller/index.html");
        modelAndView.addObject("data",sellerModelPageInfo);
        modelAndView.addObject("CONTROLLER_NAME","seller");
        modelAndView.addObject("ACTION_NAME","index");
        return modelAndView;
    }

    //新建商家页
    @RequestMapping("/createpage")
    @AdminPermission
    public ModelAndView createPage(){
        ModelAndView modelAndView = new ModelAndView("admin/seller/create.html");
        modelAndView.addObject("CONTROLLER_NAME","seller");
        modelAndView.addObject("ACTION_NAME","create");
        return modelAndView;
    }

    @RequestMapping(value = "/create",method = RequestMethod.POST)
    @AdminPermission
    public String create(@Valid SellerCreateReq sellerCreateReq, BindingResult bindingResult) throws CommonException {
        if(bindingResult.hasErrors()){
            throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtil.processErrorString(bindingResult));
        }

        SellerModel sellerModel = new SellerModel();
        sellerModel.setName(sellerCreateReq.getName());
        sellerService.create(sellerModel);

        return "redirect:/admin/seller/index";

    }

    //禁用商家
    @RequestMapping(value="down",method = RequestMethod.POST)
    @AdminPermission
    @ResponseBody
    public MessageRes down(@RequestParam(value="id")Integer id) throws CommonException {
        SellerModel sellerModel = sellerService.changeStatus(id,1);
        return MessageRes.create(sellerModel);
    }

    //启用商家
    @RequestMapping(value="up",method = RequestMethod.POST)
    @AdminPermission
    @ResponseBody
    public MessageRes up(@RequestParam(value="id")Integer id) throws CommonException {
        SellerModel sellerModel = sellerService.changeStatus(id,0);
        return MessageRes.create(sellerModel);
    }
}

