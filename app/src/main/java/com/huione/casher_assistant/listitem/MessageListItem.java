package com.huione.casher_assistant.listitem;

public class MessageListItem {
    private String uid;
    private String title;
    private String content;
    private String biz;
    private String tel;
    private String amount;
    private String created_at;

    public MessageListItem() {
        this.uid = "";
        this.title = "";
        this.content = "";
        this.biz = "";
        this.tel = "";
        this.amount = "";
        this.created_at = "";
    }

    public MessageListItem(String uid, String title, String content,String biz, String tel, String amount, String created_at) {
        this.uid = uid;
        this.title = title;
        this.content = content;
        this.biz = biz;
        this.tel = tel;
        this.amount = amount;
        this.created_at = created_at;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBiz() {
        return biz;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
