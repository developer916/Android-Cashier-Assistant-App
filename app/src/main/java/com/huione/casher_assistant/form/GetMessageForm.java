package com.huione.casher_assistant.form;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.huione.casher_assistant.lib.QueryString;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class GetMessageForm implements Serializable {
    private String m;
    @SerializedName("merch_id")
    private String merchant_id;
    private String device_id;
    @SerializedName("page")
    private int page_number;
    @SerializedName("limit")
    private int page_limit;
    private String msg_id;
    private String time;
    private String sign;
    private transient SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private transient TimeZone tz = TimeZone.getTimeZone("GMT+07:00");
    private transient Boolean latest_form = false;
    private long between;//此时间是校准时间 时分秒

    public long getBetween() {
        return between;
    }

    public void setBetween(long between) {
        this.between = between;
    }


    public GetMessageForm() {
        m = null;
        merchant_id = null;
        device_id = null;
        page_number = 1;
        page_limit = 10;
        msg_id = null;
        time = null;
        sign = null;
    }

    public GetMessageForm(String m, String merchant_id, String device_id, int page_number, int page_limit, String time, String msg_id) {
        this.m = m;
        this.merchant_id = merchant_id;
        this.device_id = device_id;
        this.page_number = page_number;
        this.page_limit = page_limit;
        this.time = time;
        this.msg_id = msg_id;
    }

    public String md5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public boolean isValid() {
        return !(TextUtils.isEmpty(m) || TextUtils.isEmpty(device_id) || page_limit <= 0 || page_number <= 0 || TextUtils.isEmpty(time) || msg_id == null);
    }

    public void genearateTime() {
        Calendar cal = Calendar.getInstance(tz);
        Log.i("AAA", "@@getBetween=" + getBetween());
        this.time = dateFormat.format(cal.getTime().getTime() + getBetween() + 60000);
    }

    public boolean generateMd5Sign() {
        genearateTime();
        if (isValid()) {
            QueryString queryString = new QueryString();
            Map<String, String> parameters = new TreeMap<>();
            parameters.put("m", this.m);
            parameters.put("merch_id", this.merchant_id);
            parameters.put("device_id", this.device_id);
            parameters.put("page", Integer.toString(this.page_number));
            parameters.put("limit", Integer.toString(this.page_limit));
            parameters.put("msg_id", this.msg_id);
            parameters.put("time", this.time);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                queryString.add(entry.getKey(), entry.getValue());
            }
            sign = md5(queryString + this.device_id).toLowerCase();
            return true;
        } else {
            return false;
        }

    }

    public String getMethod() {
        return m;
    }

    public void setMethod(String m) {
        this.m = m;
    }

    public String getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(String merchant_id) {
        this.merchant_id = merchant_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public int getPage_number() {
        return page_number;
    }

    public void setPage_number(int page_number) {
        this.page_number = page_number;
    }

    public int getPage_limit() {
        return page_limit;
    }

    public void setPage_limit(int page_limit) {
        this.page_limit = page_limit;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSign() {
        return sign;
    }

    public Boolean getLatest_form() {
        return latest_form;
    }

    public void setLatest_form(Boolean latest_form) {
        this.latest_form = latest_form;
    }
}
