package com.ssw.referral.controller.admin;

import com.ssw.referral.common.*;
import com.ssw.referral.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Controller("/admin/admin")
@RequestMapping("/admin/admin")
public class AdminController {



    @Value("${admin.email}")
    private String email;

    @Value("${admin.encryptPassword}")
    private String encrptyPassword;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private UserService userService;

    public static final String CURRENT_ADMIN_SESSION = "currentAdminSession";

    @RequestMapping("/index")
    @AdminPermission
    public ModelAndView index(){
        ModelAndView modelAndView = new ModelAndView("/admin/admin/index");
        userService.countAllUser();
        modelAndView.addObject("userCount",userService.countAllUser());
        modelAndView.addObject("CONTROLLER_NAME","admin");
        modelAndView.addObject("ACTION_NAME","index");
        return modelAndView;
    }

    @RequestMapping("/loginpage")
    public ModelAndView loginpage(){
        ModelAndView modelAndView = new ModelAndView("/admin/admin/login");
        return modelAndView;
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(@RequestParam(name = "email")String email,
    @RequestParam(name="password")String password) throws CommonException, UnsupportedEncodingException, NoSuchAlgorithmException {
       if (email.isEmpty() || password.isEmpty()){
           throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户名密码不能为空");
       }
       if(email.equals(this.email) && CommonUtil.encodeByMd5(password).equals(this.encrptyPassword)){
            // 登录成功
            httpServletRequest.getSession().setAttribute(CURRENT_ADMIN_SESSION,email);
            return "redirect:/admin/admin/index";
       }else {
           // 登录失败
           throw new CommonException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户名密码错误");
       }
    }


}
