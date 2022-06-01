package com.ssw.referral.common;

import org.apache.logging.log4j.message.Message;

public class MessageRes {

    // 表明读请求的返回处理结果,"success"或"fail"
    private String status;

    // 若status=success时,表明对应的返回json类数据
    // 若status=fail时,则data内将使用通用的错误码对应的格式
    private Object data;

    // 定义一个通用的创建返回对象的方法
    public static MessageRes create(Object result){
        return MessageRes.create(result,"success");
    }
    public static MessageRes create(Object result,String status){
        MessageRes messageRes = new MessageRes();
        messageRes.setStatus(status);
        messageRes.setData(result);

        return messageRes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
