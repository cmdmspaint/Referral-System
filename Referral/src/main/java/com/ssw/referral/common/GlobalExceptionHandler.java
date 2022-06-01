package com.ssw.referral.common;


import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public MessageRes doError(HttpServletRequest servletRequest, HttpServletResponse servletResponse,Exception ex){
        MessageErr messageErr;

        if(ex instanceof CommonException){
            return MessageRes.create(((CommonException)ex).getMessageErr(),"Fail!!!");
        }else if(ex instanceof NoHandlerFoundException){
            messageErr = new MessageErr(EmBusinessError.NO_HANDLER_FOUND);
            return MessageRes.create(messageErr,"Fail!!!");
        }else if(ex instanceof ServletRequestBindingException){
            messageErr = new MessageErr(EmBusinessError.BIND_EXCEPTION_ERROR);
            return  MessageRes.create(messageErr,"Fail!!!");
        }else {
            messageErr = new MessageErr(EmBusinessError.UNKNOWN_ERROR);
            return MessageRes.create(messageErr,"Fail!!!");
        }
    }

}
