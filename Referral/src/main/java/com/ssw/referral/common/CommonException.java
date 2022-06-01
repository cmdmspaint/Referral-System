package com.ssw.referral.common;

public class CommonException extends Exception{
    private MessageErr messageErr;


    public CommonException(EmBusinessError emBusinessError){
        super();
        this.messageErr = new MessageErr(emBusinessError);
    }

    public MessageErr getMessageErr() {
        return messageErr;
    }

    public void setMessageErr(MessageErr messageErr) {
        this.messageErr = messageErr;
    }
}
