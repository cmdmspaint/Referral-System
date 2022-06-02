package com.ssw.referral.common;

public class CommonException extends Exception{
    private MessageErr messageErr;


    public CommonException(EmBusinessError emBusinessError){
        super();
        this.messageErr = new MessageErr(emBusinessError);
    }

    public CommonException(EmBusinessError emBusinessError,String errMsg){
        super();
        this.messageErr = new MessageErr(emBusinessError);
        this.messageErr.setErrMsg(errMsg);
    }

    public MessageErr getMessageErr() {
        return messageErr;
    }

    public void setMessageErr(MessageErr messageErr) {
        this.messageErr = messageErr;
    }
}
