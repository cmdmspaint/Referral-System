package com.ssw.referral.common;


import com.ssw.referral.controller.admin.AdminController;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


@Aspect
@Configuration
public class ControllerAspect {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HttpServletResponse httpServletResponse;

    /**
     * 环绕式切面编程
     * execution 监管返回值
     * * com.ssw.referral.controller.admin.*.*(..) 该包下所有的方法参数
     * 并且被打上&& @@annotation(org.springframework.web.bind.annotation.RequestMapping) RequestMapping标签
     * 的方法才会被监管
     * @return
     */
    @Around("execution(* com.ssw.referral.controller.admin.*.*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object adminControllerBeforeValidation(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method =((MethodSignature)joinPoint.getSignature()).getMethod();
        AdminPermission adminPermission = method.getAnnotation(AdminPermission.class);
        if (adminPermission == null){
            // 公共方法
            Object resultObject = joinPoint.proceed();
            return resultObject;
        }
        // 判断当前管理员是否登录
       String email = (String) httpServletRequest.getSession().getAttribute(AdminController.CURRENT_ADMIN_SESSION);
        if (email == null){
            if (adminPermission.produceType().equals("text/html")){
                httpServletResponse.sendRedirect("/admin/admin/loginpage");
                return null;
            }else {
               MessageErr messageErr= new MessageErr(EmBusinessError.ADMIN_SHOULD_LOGIN);
                return MessageRes.create(messageErr,"Fail!!!");
            }
        }else{
            Object resultObject = joinPoint.proceed();
            return resultObject;
        }
    }
}
