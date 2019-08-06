package com.huione.casher_assistant.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huione.casher_assistant.R;
import com.huione.casher_assistant.lib.HelperFunctions;
import com.huione.casher_assistant.listitem.MessageListItem;
import com.jaychang.srv.SimpleCell;
import com.jaychang.srv.SimpleViewHolder;
import com.jaychang.srv.Updatable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListCell extends SimpleCell<MessageListItem, MessageListCell.ViewHolder> implements Updatable<MessageListItem> {

    public MessageListCell(MessageListItem messageItem) {
        super(messageItem);
    }

    public Pattern replaceRegPattern = Pattern.compile("\\$(\\d+)(,(\\d+))?(,(\\d+))?(,(\\d+))?(,(\\d+))?(,(\\d+))?(\\S*)");

    @Override
    protected int getLayoutRes() {
        return R.layout.message_list_row;
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(ViewGroup parent, View cellView) {
        return new ViewHolder(cellView);
    }

    @Override
    protected long getItemId() {
        return getItem().getUid().hashCode();
    }

    @Override
    protected void onBindViewHolder(ViewHolder holder, int position, Context context, Object payload) {
        MessageListItem message = getItem();
        holder.title.setText(R.string.message_list_row_title);
        String content = formatVoiceMessage(context, message.getTel(), message.getAmount(), message.getBiz());

        SpannableString mSpannableString = new SpannableString(content);
        mSpannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#333333")), content.indexOf("$"), content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //size  为0 即采用原始的正常的 size大小
        mSpannableString.setSpan(new TextAppearanceSpan(null, 0, 60, null, null), content.indexOf("$"), content.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        holder.content.setText(mSpannableString);
        holder.created_at.setText(message.getCreated_at());
    }

    @Override
    public boolean areContentsTheSame(MessageListItem newItem) {
        return getItem().getUid().equals(newItem.getUid());
    }

    @Override
    public Object getChangePayload(MessageListItem newItem) {
        return null;
    }

    public class ViewHolder extends SimpleViewHolder {
        public TextView title, content, created_at;

        public ViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title);
            content = (TextView) view.findViewById(R.id.content);
            created_at = (TextView) view.findViewById(R.id.created_at);
        }
    }

    public String formatVoiceMessage(Context context, String tel, String amount, String biz) {
//        return getString(R.string.voice_message_template, tel, HelperFunctions.filterContent(replaceRegPattern, amount));
        if (biz.equals("5") || biz.equals("95")) {
            //转账和二维码（都是汇旺的）
            return context.getString(R.string.voice_message_template, HelperFunctions.filterContent(replaceRegPattern, amount));
        } else if (biz.equals("7")) {
            //扫码收款（支付宝的）
            return context.getString(R.string.voice_message_alipay, HelperFunctions.filterContent(replaceRegPattern, amount));
        } else {
            return context.getString(R.string.voice_message_template, HelperFunctions.filterContent(replaceRegPattern, amount));
        }
    }

}