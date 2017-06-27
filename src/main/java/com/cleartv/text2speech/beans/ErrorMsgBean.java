package com.cleartv.text2speech.beans;

public class ErrorMsgBean {
    private MsgBean msg;
    private long time;
    private int errorCode;
    private String errorMsg;

    public ErrorMsgBean(MsgBean msg, long time, int code, String errorMsg) {
        this.msg = msg;
        this.time = time;
        this.errorCode = code;
        this.errorMsg = errorMsg;
    }
}