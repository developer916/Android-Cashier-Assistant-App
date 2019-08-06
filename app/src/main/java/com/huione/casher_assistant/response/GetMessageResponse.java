package com.huione.casher_assistant.response;
import java.io.Serializable;
import java.util.List;

/**
 * 获取交易消息
 */
public class GetMessageResponse extends BaseResponse implements Serializable {

    /**
     * data : {"list":[{"id":"4101528","msg_id":"4101494","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：高手 (717157676)，转账金额：$0.01","biz":"5","tel":"717157676","amount":"$0.01","created_at":"2019-06-27 16:38:27"},{"id":"4101518","msg_id":"4101484","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：时候 (968523785)，转账金额：$2.30","biz":"95","tel":"968523785","amount":"$2.30","created_at":"2019-06-27 15:58:32"},{"id":"4101516","msg_id":"4101482","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：时候 (968523785)，转账金额：$2.00","biz":"95","tel":"968523785","amount":"$2.00","created_at":"2019-06-27 15:57:41"},{"id":"4101514","msg_id":"4101480","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：时候 (968523785)，转账金额：$1.00","biz":"95","tel":"968523785","amount":"$1.00","created_at":"2019-06-27 15:56:56"},{"id":"4101506","msg_id":"4101472","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.70","biz":"95","tel":"968633993","amount":"$0.70","created_at":"2019-06-27 14:56:38"},{"id":"4101504","msg_id":"4101470","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.60","biz":"95","tel":"968633993","amount":"$0.60","created_at":"2019-06-27 14:54:33"},{"id":"4101502","msg_id":"4101468","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.50","biz":"95","tel":"968633993","amount":"$0.50","created_at":"2019-06-27 14:53:23"},{"id":"4101500","msg_id":"4101466","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.40","biz":"95","tel":"968633993","amount":"$0.40","created_at":"2019-06-27 14:52:48"},{"id":"4101498","msg_id":"4101464","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.30","biz":"95","tel":"968633993","amount":"$0.30","created_at":"2019-06-27 14:52:06"},{"id":"4101496","msg_id":"4101462","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.20","biz":"95","tel":"968633993","amount":"$0.20","created_at":"2019-06-27 14:51:41"}],"merch_name":"hh"}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * list : [{"id":"4101528","msg_id":"4101494","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：高手 (717157676)，转账金额：$0.01","biz":"5","tel":"717157676","amount":"$0.01","created_at":"2019-06-27 16:38:27"},{"id":"4101518","msg_id":"4101484","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：时候 (968523785)，转账金额：$2.30","biz":"95","tel":"968523785","amount":"$2.30","created_at":"2019-06-27 15:58:32"},{"id":"4101516","msg_id":"4101482","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：时候 (968523785)，转账金额：$2.00","biz":"95","tel":"968523785","amount":"$2.00","created_at":"2019-06-27 15:57:41"},{"id":"4101514","msg_id":"4101480","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：时候 (968523785)，转账金额：$1.00","biz":"95","tel":"968523785","amount":"$1.00","created_at":"2019-06-27 15:56:56"},{"id":"4101506","msg_id":"4101472","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.70","biz":"95","tel":"968633993","amount":"$0.70","created_at":"2019-06-27 14:56:38"},{"id":"4101504","msg_id":"4101470","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.60","biz":"95","tel":"968633993","amount":"$0.60","created_at":"2019-06-27 14:54:33"},{"id":"4101502","msg_id":"4101468","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.50","biz":"95","tel":"968633993","amount":"$0.50","created_at":"2019-06-27 14:53:23"},{"id":"4101500","msg_id":"4101466","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.40","biz":"95","tel":"968633993","amount":"$0.40","created_at":"2019-06-27 14:52:48"},{"id":"4101498","msg_id":"4101464","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.30","biz":"95","tel":"968633993","amount":"$0.30","created_at":"2019-06-27 14:52:06"},{"id":"4101496","msg_id":"4101462","title":"收到一笔转账","content":"尊敬的用户：您收到一笔转账。转账人：Q (968633993)，转账金额：$0.20","biz":"95","tel":"968633993","amount":"$0.20","created_at":"2019-06-27 14:51:41"}]
         * merch_name : hh
         */

        private String merch_name;
        private List<ListBean> list;

        public String getMerch_name() {
            return merch_name;
        }

        public void setMerch_name(String merch_name) {
            this.merch_name = merch_name;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {
            /**
             * id : 4101528
             * msg_id : 4101494
             * title : 收到一笔转账
             * content : 尊敬的用户：您收到一笔转账。转账人：高手 (717157676)，转账金额：$0.01
             * biz : 5
             * tel : 717157676
             * amount : $0.01
             * created_at : 2019-06-27 16:38:27
             */

            private String id;
            private String msg_id;
            private String title;
            private String content;
            private String biz;
            private String tel;
            private String amount;
            private String created_at;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getMsg_id() {
                return msg_id;
            }

            public void setMsg_id(String msg_id) {
                this.msg_id = msg_id;
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

            public String getCreated_at() {
                return created_at;
            }

            public void setCreated_at(String created_at) {
                this.created_at = created_at;
            }
        }

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
