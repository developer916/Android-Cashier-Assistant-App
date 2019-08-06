package com.huione.casher_assistant.message;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.huione.casher_assistant.R;

public class MessageDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton backBtn;

    public TextView titleTextView, contentTextView, createdAtTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        String title = getIntent().getExtras().getString("title", "");
        String content = getIntent().getExtras().getString("content", "");
        String created_at = getIntent().getExtras().getString("created_at", "");
        titleTextView = findViewById(R.id.title);
        contentTextView = findViewById(R.id.content);
        createdAtTextView = findViewById(R.id.created_at);
        backBtn = findViewById(R.id.back_button);

        titleTextView.setText(title);

//        contentTextView.setText(content);
        SpannableString mSpannableString = new SpannableString(content);
        mSpannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#333333")), content.indexOf("$"), content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //size  为0 即采用原始的正常的 size大小
        mSpannableString.setSpan(new TextAppearanceSpan(null, 0, 60, null, null), content.indexOf("$"), content.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        contentTextView.setText(mSpannableString);

        createdAtTextView.setText(created_at);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.back_button:
                super.onBackPressed();
                break;
            default:
                break;
        }

    }
}
