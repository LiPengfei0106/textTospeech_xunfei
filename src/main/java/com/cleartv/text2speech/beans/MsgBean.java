package com.cleartv.text2speech.beans;

public class MsgBean {
    private String id;
    private String text;

    public MsgBean(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

}