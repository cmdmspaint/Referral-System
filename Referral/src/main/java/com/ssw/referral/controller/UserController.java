package com.ssw.referral.controller;


import com.ssw.referral.common.CommonException;
import com.ssw.referral.common.EmBusinessError;
import com.ssw.referral.common.MessageErr;
import com.ssw.referral.common.MessageRes;
import com.ssw.referral.model.UserModel;
import com.ssw.referral.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller("/user")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        return "hello test!";
    }

    @RequestMapping("/index")
    public ModelAndView index(){
        String userName ="testcc";
        ModelAndView modelAndView = new ModelAndView("/index.html");
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
}
