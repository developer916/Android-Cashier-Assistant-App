package com.huione.casher_assistant.my_qr_code;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huione.casher_assistant.R;
import com.huione.casher_assistant.database.DatabaseHelper;
import com.huione.casher_assistant.form.GetMessageForm;
import com.huione.casher_assistant.lib.ApiClient;
import com.huione.casher_assistant.lib.QRCodeHelper;
import com.huione.casher_assistant.message.MessageActivity;
import com.huione.casher_assistant.response.BaseResponse;
import com.huione.casher_assistant.response.GetMessageResponse;
import com.huione.huionenew.utils.EasyAES;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyQrCodeActivity extends AppCompatActivity implements View.OnClickListener {
    public final Handler handler = new Handler();
    private final String CUSTOM_TAG = "custom_tag";
    long between;//客户端与服务器时间差
    private ImageButton backBtn;
    private ImageView qrCodeImageView;
    private Button saveQrCodeBtn;
    private TextView validTimeTextView;
    private View progressLayoutView, progressView;
    private String device_id = null;
    private TimeZone tz = TimeZone.getTimeZone("GMT+07:00");
    private Timer timer;
    private TimerTask timerTask;
    private int expiration_time = 0, failed_count = 0;
    private String name, phone;
    private DatabaseHelper db;
    private GetMessageForm getMessageForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_my_qr_code);
        isExternalStorageWritable();
        db = new DatabaseHelper(this);

        Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        progressLayoutView = (FrameLayout) findViewById(R.id.progress_bar_layout);
        progressView = (ProgressBar) progressLayoutView.findViewById(R.id.progress_bar);
        backBtn = findViewById(R.id.back_button);
        qrCodeImageView = findViewById(R.id.qr_code);
        saveQrCodeBtn = findViewById(R.id.save_qr_button);
        validTimeTextView = findViewById(R.id.valid_time);
        backBtn.setOnClickListener(this);
        saveQrCodeBtn.setOnClickListener(this);
        device_id = Secure.getString(getApplicationContext().getContentResolver(),
                Secure.ANDROID_ID);
        name = getIntent().getExtras().getString("name", null);
        phone = getIntent().getExtras().getString("phone", null);
        if (name == null || phone == null) {
            Toast.makeText(getApplicationContext(), R.string.error_failed, Toast.LENGTH_LONG).show();
            finish();
        } else {
            getMessageForm = new GetMessageForm("getmessage", "", device_id, 1, 10, null, "");
            getMessage();

        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.back_button:
                super.onBackPressed();
                break;
            case R.id.save_qr_button:
                saveQRCode();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stoptimertask();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        timer.schedule(timerTask, 0, 1000); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        expiration_time--;
                        if (expiration_time < 0) {
                            displayQRCode();
                            expiration_time = 30;
                        }
                        validTimeTextView.setText(String.valueOf(expiration_time));
                    }
                });
            }
        };
    }

    public void getMessage() {
        if (getMessageForm.generateMd5Sign()) {
            Call<GetMessageResponse> mService = ApiClient.getInstance().getApi().getMessage(
                    getMessageForm.getMethod(),
                    getMessageForm.getMerchant_id(),
                    getMessageForm.getDevice_id(),
                    getMessageForm.getPage_number(),
                    getMessageForm.getPage_limit(),
                    getMessageForm.getMsg_id(),
                    getMessageForm.getTime(),
                    getMessageForm.getSign()
            );
            mService.enqueue(new Callback<GetMessageResponse>() {
                @Override
                public void onResponse(Call<GetMessageResponse> call, final Response<GetMessageResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            BaseResponse mBaseResponse = response.body();
                            Log.i("Assistant", "mBaseResponse=" + new Gson().toJson(mBaseResponse));
                            if (mBaseResponse.getCode() == 1) {
                                Log.e(CUSTOM_TAG, "api_success");
                                Intent messageIntent = new Intent(MyQrCodeActivity.this, MessageActivity.class);
                                messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(messageIntent);
                            } else if (mBaseResponse.getCode() == 2) {
                                failed_count++;
                                GetMessageResponse mGetMessageResponse = response.body();
                                String time = (String) mGetMessageResponse.getData().getTime();
                                SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat mSimpleDateFormatResult = new SimpleDateFormat("yyyyMMddHHmmss");
                                Date resultData = null;
                                try {
                                    resultData = mSimpleDateFormatResult.parse(time);
                                    String serverDate = mSimpleDateFormat.format(resultData);
                                    Date localDate = new Date();
                                    String local = mSimpleDateFormat.format(localDate);
                                    Log.i("AAA", "serverDate=" + serverDate + ";local=" + local);

                                    between = resultData.getTime() - localDate.getTime();
                                    getMessageForm.setBetween(between);//设置时间差

                                    Log.i("AAA", "between=" + between);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (mGetMessageResponse.getData().getMerch_id().equals("false")) {
                                    Log.e(CUSTOM_TAG, "api_failed1");
                                    db.clearMessages();
                                    new android.os.Handler().postDelayed(
                                            new Runnable() {
                                                public void run() {
                                                    getMessage();
                                                }
                                            },
                                            3000);
                                } else {
                                    Log.e(CUSTOM_TAG, "api_failed2");
                                    getMessageForm.setMerchant_id(mGetMessageResponse.getData().getMerch_id());
                                    if (failed_count <= 1) {
                                        getMessage();

                                    } else {
                                        new android.os.Handler().postDelayed(
                                                new Runnable() {
                                                    public void run() {
                                                        if (failed_count > 0) {
                                                            getMessage();
                                                        }
                                                    }
                                                },
                                                3000);
                                    }
                                }
                            }
                        } else {
                            String s = response.errorBody().string();
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                Toast.makeText(getApplicationContext(), jsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), R.string.error_failure_wrong_format, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                Log.e("failed_json_response", s);
                            }
                        }

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), R.string.error_failure_wrong_format, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<GetMessageResponse> call, Throwable t) {
                    call.cancel();
                    Toast.makeText(getApplicationContext(), R.string.error_failure_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void saveQRCode() {
        Bitmap bitmap = ((BitmapDrawable) qrCodeImageView.getDrawable()).getBitmap();
        String result = downloadQRCode(bitmap);
        Toast success_message;
        if (result.trim().equals("success")) {
            success_message = Toast.makeText(getApplicationContext(),
                    getResources().getText(R.string.my_qr_code_saved_successfully),
                    Toast.LENGTH_LONG);
        } else {
            success_message = Toast.makeText(getApplicationContext(),
                    getResources().getText(R.string.error_qr_code_save_failed),
                    Toast.LENGTH_LONG);
        }
        success_message.setGravity(Gravity.CENTER, 0, 0);
        success_message.show();
    }

    public String downloadQRCode(Bitmap bitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File wallpaperDirectory = new File(root);
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdir();
        }
        try {
            if (isExternalStorageWritable()) {
                File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 0, bytes);
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                MediaScannerConnection.scanFile(this,
                        new String[]{f.getPath()},
                        new String[]{"image/jpeg"}, null);
                fo.close();
                return "success";
            } else {
                return "failed";
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            return "failed";
        }

    }

    public void displayQRCode() {
        Calendar cal = Calendar.getInstance(tz);

        try {
            JSONObject contentJson = new JSONObject();
            contentJson.put("name", name);
            contentJson.put("tel", phone);
            contentJson.put("device_id", device_id);
            contentJson.put("time", String.valueOf(cal.getTimeInMillis() / 1000));
            String encrypted_content = EasyAES.encryptString(contentJson.toString());
            Bitmap qrCode = QRCodeHelper.newInstance(this).setContent(encrypted_content).getQRCode();
            qrCodeImageView.setImageBitmap(qrCode);

        } catch (JSONException ex) {
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error_qr_generation_failed, Toast.LENGTH_LONG).show();
        }

    }

    public boolean isExternalStorageWritable() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
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
