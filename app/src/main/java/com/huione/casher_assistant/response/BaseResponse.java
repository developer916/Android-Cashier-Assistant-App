package com.huione.casher_assistant.response;

import java.io.Serializable;

public class BaseResponse implements Serializable {
   int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
