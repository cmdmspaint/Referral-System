package com.ssw.referral.controller;


import com.ssw.referral.common.*;
import com.ssw.referral.model.UserModel;
import com.ssw.referral.request.LoginReq;
import com.ssw.referral.request.RegisterReq;
import com.ssw.referral.service.CategoryService;
import com.ssw.referral.service.SellerService;
import com.ssw.referral.service.ShopService;
import com.ssw.referral.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Controller("/user")
@RequestMapping("/user")
public class UserController {

    public static final String CURRENT_USER_SESSION = "currentUserSession";

    @Autowired
    private HttpServletRequest httpServletRequest; // 基于threadlocal实现的session机制

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private SellerService sellerService;

    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        return "hello test!";
    }

    @RequestMapping("/index")
    public ModelAndView index(){
        String userName ="testcc";
        ModelAndView modelAndView = new ModelAndView("/static/index.html");
        modelAndView.addObject("name",userName);
        return modelAndView;
    }

    @RequestMapping("/get")
    @ResponseBody
    public MessageRes getUser(@RequestParam(name="id")Integer id) throws CommonException {
        UserModel userModel = userService.getUser(id);
        if(userModel == null){
            //return MessageRes.create(new MessageErr(EmBusinessError.NO_OBJECT_FOUND),"Fail!!!");
            throw new CommonException(EmBusinessError.NO_OBJECT_FOUND);
        }else{
            return MessageRes.create(userModel);
        }
    }

    @RequestMapping("/register")
    @ResponseBody
    public MessageRes register(@Valid @RequestBody RegisterReq registerReq, BindingResult bindingResult) throws UnsupportedEncodingException, CommonException, NoSuchAlgorithmException {

        if (bindingResult.hasErrors()){
            throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtil.processErrorString(bindingResult));
        }

        UserModel registerUser =new UserModel();
        registerUser.setTelphone(registerReq.getTelphone());
        registerUser.setPassword(registerReq.getPassword());
        registerUser.setNickName(registerReq.getNickName());
        registerUser.setGender(registerReq.getGender());

        UserModel resUserModel = userService.register(registerUser);

        return MessageRes.create(resUserModel);
    }

    @RequestMapping("/login")
    @ResponseBody
    public MessageRes login(@RequestBody @Valid LoginReq loginReq,BindingResult bindingResult) throws CommonException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if(bindingResult.hasErrors()){
            throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR,CommonUtil.processErrorString(bindingResult));
        }
        UserModel userModel = userService.login(loginReq.getTelphone(),loginReq.getPassword());
        httpServletRequest.getSession().setAttribute(CURRENT_USER_SESSION,userModel);

        return MessageRes.create(userModel);
    }

    @RequestMapping("/logout")
    @ResponseBody
    public MessageRes logout() {
       httpServletRequest.getSession().invalidate();
       return MessageRes.create(null);
    }

    // 获取当前信息
    @RequestMapping("/getcurrentuser")
    @ResponseBody
    public MessageRes getCurrentUser(){
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute(CURRENT_USER_SESSION);
        return MessageRes.create(userModel);
    }
}
