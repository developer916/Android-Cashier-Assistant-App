package com.huione.casher_assistant.response;
import java.io.Serializable;

public class GetErrorResponse extends BaseResponse implements Serializable {

    /**
     * code : 2
     * data : {"time":"1561604816","merch_id":"64519"}
     * merch_name : hh
     * msg : 请求超时
     */

    private DataBean data;
    private String merch_name;
    private String msg;


    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMerch_name() {
        return merch_name;
    }

    public void setMerch_name(String merch_name) {
        this.merch_name = merch_name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class DataBean {
        /**
         * time : 1561604816
         * merch_id : 64519
         */

        private String time;
        private String merch_id;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getMerch_id() {
            return merch_id;
        }

        public void setMerch_id(String merch_id) {
            this.merch_id = merch_id;
        }
    }
}
