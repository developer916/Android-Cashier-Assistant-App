package com.huione.casher_assistant.security_setting;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.goldenpie.devs.pincodeview.PinCodeView;
import com.goldenpie.devs.pincodeview.core.Listeners;
import com.huione.casher_assistant.R;
import com.huione.casher_assistant.database.DatabaseHelper;
import com.huione.casher_assistant.message.MessageActivity;

import org.jetbrains.annotations.Nullable;

public class SecuritySettingActivity extends AppCompatActivity implements View.OnClickListener, Listeners.PinEnteredListener {

    private ImageButton backBtn;
    private TextView actionBarTitle, descriptionTextView;
    private View progressLayoutView, progressView;
    private PinCodeView pinCodeView;
    private DatabaseHelper db;
    private String actionType = "enter", password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_setting);
        pinCodeView = findViewById(R.id.pin_code);

        db = new DatabaseHelper(this);
        progressLayoutView = (FrameLayout) findViewById(R.id.progress_bar_layout);
        progressView = (ProgressBar) progressLayoutView.findViewById(R.id.progress_bar);
        Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        actionBarTitle = app_bar.findViewById(R.id.title);
        backBtn = findViewById(R.id.back_button);
        descriptionTextView = findViewById(R.id.description);
        pinCodeView.setPinEnteredListener(this);
        backBtn.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            actionType = bundle.getString("action_type", "enter");
        }
        password = db.getUserSettingValueByKey(getResources().getString(R.string.password_field), "");
        if (password.equals("")) {
            actionType = "new";
            backBtn.setVisibility(View.GONE);
        }else{
            actionBarTitle.setText(getString(R.string.left_security_login));
        }
        if(actionType.equals("old")){
            actionBarTitle.setText(getString(R.string.left_security_setting));
        }
        setupPage();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pinCodeView.setFocusable(true);
                pinCodeView.requestFocus();
                InputMethodManager inputManager = (InputMethodManager) pinCodeView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(pinCodeView, 0);
            }
        }, 100);
    }

    public void setupPage() {
        switch (actionType) {
            case "enter":
                backBtn.setVisibility(View.GONE);
                descriptionTextView.setText(R.string.security_setting_description1);
                break;
            case "old":
                descriptionTextView.setText(R.string.security_setting_description2);
                break;
            case "new":
                descriptionTextView.setText(R.string.security_setting_description3);
                break;
            default:
                descriptionTextView.setText(R.string.error_failed);
                pinCodeView.setPinEnteredListener(null);
                break;
        }

    }

    @Override
    public void onPinEntered(@Nullable String s) {
        if (actionType.equals("enter")) {
            if (!s.equals(password)) {
                pinCodeView.reset();
            } else {
                Intent messageIntent = new Intent(getApplicationContext(), MessageActivity.class);
                messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(messageIntent);
            }
        } else if (actionType.equals("old")) {
            if (!s.equals(password)) {
                pinCodeView.reset();
            } else {
                pinCodeView.clear();
                Intent newPasswordIntent = new Intent(getApplicationContext(), SecuritySettingActivity.class);
                newPasswordIntent.putExtra("action_type", "new");
                startActivity(newPasswordIntent);
            }

        } else if (actionType.equals("new")) {
            if (db.insertOrUpdateUserSetting(getResources().getString(R.string.password_field), s) != null) {
                Intent messageIntent = new Intent(getApplicationContext(), MessageActivity.class);
                messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(messageIntent);
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_saving_failed, Toast.LENGTH_LONG).show();
                pinCodeView.clear();
            }
        }
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

    /**
     * Show/Hide the progress UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressLayoutView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressLayoutView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
