package com.ssw.referral.common;

public class MessageErr {

    // 错误码
    private Integer errCode;

    // 错误描述
    private String errMsg;

    public MessageErr(Integer errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }


    public MessageErr(EmBusinessError emBusinessError) {
        this.errCode = emBusinessError.getErrCode();
        this.errMsg = emBusinessError.getErrMsg();
    }

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
