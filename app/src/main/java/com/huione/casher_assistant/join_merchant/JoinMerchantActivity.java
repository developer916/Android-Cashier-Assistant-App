package com.huione.casher_assistant.join_merchant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.huione.casher_assistant.R;
import com.huione.casher_assistant.my_qr_code.MyQrCodeActivity;

public class JoinMerchantActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nameEditText, phoneEditText;
    private ImageButton backBtn;
    private Button generateQRBtn;
    private View progressLayoutView, progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_merchant);
        Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        progressLayoutView = (FrameLayout) findViewById(R.id.progress_bar_layout);
        progressView = (ProgressBar) progressLayoutView.findViewById(R.id.progress_bar);
        backBtn = findViewById(R.id.back_button);
        nameEditText = findViewById(R.id.name);
        phoneEditText = findViewById(R.id.phone);
        generateQRBtn = findViewById(R.id.generate_qr_button);
        backBtn.setOnClickListener(this);
        generateQRBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.back_button:
                super.onBackPressed();
                break;
            case R.id.generate_qr_button:
                generateQRCode();
                break;
            default:
                break;
        }
    }

    public void generateQRCode() {
        generateQRBtn.setEnabled(false);
        if (checkValidation()) {
            String sName = nameEditText.getText().toString();
            String sPhone = phoneEditText.getText().toString();
            Intent myQrCodeIntent = new Intent(getApplicationContext(), MyQrCodeActivity.class);
            myQrCodeIntent.putExtra("name", sName);
            myQrCodeIntent.putExtra("phone", sPhone);
            startActivity(myQrCodeIntent);
            generateQRBtn.setEnabled(true);

        } else {
            generateQRBtn.setEnabled(true);

        }
    }

    public boolean checkValidation() {
        View focusView = null;
        boolean valid = true;
        nameEditText.setError(null);
        phoneEditText.setError(null);
        String sName = nameEditText.getText().toString();
        String sPhone = phoneEditText.getText().toString();
        if (TextUtils.isEmpty(sPhone)) {
            phoneEditText.setError(getString(R.string.error_field_required));
            focusView = phoneEditText;
            valid = false;
        } else if (!isPhoneNumberValid(sPhone)) {
            phoneEditText.setError(getString(R.string.error_invalid_phone));
            focusView = phoneEditText;
            valid = false;
        }
        if (TextUtils.isEmpty(sName)) {
            nameEditText.setError(getString(R.string.error_field_required));
            focusView = nameEditText;
            valid = false;

        }
        if (!valid) {
            focusView.requestFocus();
        }
        return valid;
    }

    boolean isPhoneNumberValid(String phonenumber) {
        return !TextUtils.isEmpty(phonenumber) && phonenumber.charAt(0) == '+';
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
