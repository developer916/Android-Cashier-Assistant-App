package com.huione.casher_assistant.message;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huione.casher_assistant.R;
import com.huione.casher_assistant.adapter.MessageListCell;
import com.huione.casher_assistant.database.DatabaseHelper;
import com.huione.casher_assistant.database.Message;
import com.huione.casher_assistant.form.GetMessageForm;
import com.huione.casher_assistant.join_merchant.JoinMerchantActivity;
import com.huione.casher_assistant.lib.ApiClient;
import com.huione.casher_assistant.lib.HelperFunctions;
import com.huione.casher_assistant.listitem.MessageListItem;
import com.huione.casher_assistant.response.BaseResponse;
import com.huione.casher_assistant.response.GetMessageResponse;
import com.huione.casher_assistant.security_setting.SecuritySettingActivity;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.jaychang.srv.OnLoadMoreListener;
import com.jaychang.srv.SimpleCell;
import com.jaychang.srv.SimpleRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public final Handler handler = new Handler();
    private final String CUSTOM_TAG = "custom_tag";
    TabLayout tabLayout;
    Pattern replaceRegPattern = Pattern.compile("\\$(\\d+)(,(\\d+))?(,(\\d+))?(,(\\d+))?(,(\\d+))?(,(\\d+))?(\\S*)");
    long between;//客户端与服务器时间差
    long day = 0;
    long hour = 0;
    long min = 0;
    long s = 0;
    private ImageButton collapseMenuButton;
    private TextView left_nav_user_name;//商户名称
    private DrawerLayout drawerLayout;
    private LinearLayout allListPanel, newListPanel, readListPanel, noLoginPanel;
    private SimpleRecyclerView allListView, newListView, readListView;
    private TextView joinMerchantBtn1, securitySettingBtn;
    private Button joinMerchantBtn2;
    private Boolean isLoggedIn = true;
    private GetMessageForm getMessageForm;
    private String device_id = null;
    private DatabaseHelper db;
    private Boolean isLastPage = false, isLoading = true;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SpeechSynthesizer mTts;
    private Timer timer;
    private TimerTask timerTask;
    private boolean timerStopped = true;
    private int failed_count = 0;
    private MessageListCell currentSpeakingCell = null;
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {
            newListView.removeCell(currentSpeakingCell);
            if (newListView.getItemCount() < 1) {
                hideNewCircle();
            }
            MessageListItem massageListItem = currentSpeakingCell.getItem();
            MessageListCell newReadCell = new MessageListCell(massageListItem);
            newReadCell.setOnCellClickListener(new SimpleCell.OnCellClickListener<MessageListItem>() {
                @Override
                public void onCellClicked(MessageListItem massageListItem) {

                    timerStopped = true;
                    //start MessageDetailActivity
                    Intent messageDetailIntent = new Intent(getApplicationContext(), MessageDetailActivity.class);
                    messageDetailIntent.putExtra("title", getResources().getString(R.string.message_list_row_title));
                    messageDetailIntent.putExtra("content", formatVoiceMessage(massageListItem.getTel(), massageListItem.getAmount(), massageListItem.getBiz()));
                    messageDetailIntent.putExtra("created_at", massageListItem.getCreated_at());
                    startActivity(messageDetailIntent);

                }
            });
            db.setReadMark(massageListItem.getUid());
            db.setSpeakMark(massageListItem.getUid());
            loadReadData(true);
            if (newListView.getItemCount() > 0) {
                currentSpeakingCell = (MessageListCell) newListView.getCell(0);
                if (needToSpeak(currentSpeakingCell.getItem().getCreated_at())) {
                    setParam();
                    mTts.startSpeaking(formatVoiceMessage(((MessageListItem) currentSpeakingCell.getItem()).getTel(), ((MessageListItem) currentSpeakingCell.getItem()).getAmount(), ((MessageListItem) currentSpeakingCell.getItem()).getBiz()
                    ), mTtsListener);
                } else {
                    currentSpeakingCell = null;
                }
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        db = new DatabaseHelper(this);
        mTts = SpeechSynthesizer.createSynthesizer(MessageActivity.this, null);

        drawerLayout = findViewById(R.id.main_layout);
        collapseMenuButton = findViewById(R.id.collapse_menu_button);
        allListPanel = findViewById(R.id.tab_panel_all);
        allListView = findViewById(R.id.all_list);
        newListPanel = findViewById(R.id.tab_panel_new);
        newListView = findViewById(R.id.new_list);
        readListPanel = findViewById(R.id.tab_panel_read);
        readListView = findViewById(R.id.read_list);
        noLoginPanel = findViewById(R.id.tab_panel_no_login);
        joinMerchantBtn1 = findViewById(R.id.left_nav_join_merchant);
        joinMerchantBtn2 = findViewById(R.id.join_merchant_button);
        securitySettingBtn = findViewById(R.id.left_nav_security_setting);
        swipeRefreshLayout = findViewById(R.id.swipe_container);
        left_nav_user_name = findViewById(R.id.left_nav_user_name);

        drawerLayout.setScrimColor(getResources().getColor(R.color.colorTransparent));
        collapseMenuButton.setOnClickListener(this);

        joinMerchantBtn1.setOnClickListener(this);
        joinMerchantBtn2.setOnClickListener(this);
        securitySettingBtn.setOnClickListener(this);
        allListView.setAutoLoadMoreThreshold(4);
        allListView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public boolean shouldLoadMore() {
                return !isLastPage && !isLoading;
            }

            @Override
            public void onLoadMore() {
                isLoading = true;
                allListView.showLoadMoreView();
                getMessageForm.setPage_number(getMessageForm.getPage_number() + 1);
                prepareData(false, getMessageForm);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        tabLayout = findViewById(R.id.tab);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(R.layout.custom_tab);
            View tabView = tab.getCustomView();
            TextView txt;
            switch (i) {
                case 0:
                    txt = tabView.findViewById(R.id.tab_text);
                    txt.setText(R.string.tab_all);
                    txt.setTextColor(getResources().getColor(R.color.colorBlue1));
                    break;
                case 1:
                    txt = tabView.findViewById(R.id.tab_text);
                    txt.setText(R.string.tab_new);
                    break;
                case 2:
                    txt = tabView.findViewById(R.id.tab_text);
                    txt.setText(R.string.tab_read);
                    break;
            }
        }
        showAllList();
        allListView.showLoadMoreView();
        /////////
        device_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        getMessageForm = new GetMessageForm("getmessage", "", device_id, 1, 10, null, "");

        prepareData(true, getMessageForm);
        loadReadData(true);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View tabView = tab.getCustomView();
                TextView txt = tabView.findViewById(R.id.tab_text);
                txt.setTextColor(getResources().getColor(R.color.colorBlue1));
                if (tabLayout.getSelectedTabPosition() == 0) {
                    showAllList();
                } else if (tabLayout.getSelectedTabPosition() == 1) {
                    showNewList();
                } else if (tabLayout.getSelectedTabPosition() == 2) {
                    showReadList();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View tabView = tab.getCustomView();
                TextView txt = tabView.findViewById(R.id.tab_text);
                txt.setTextColor(getResources().getColor(R.color.colorBlue2));

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        failed_count = 0;
        if (timerStopped) {
            startTimer();
            timerStopped = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timerStopped) {
            stoptimertask();
            timerStopped = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stoptimertask();
        timerStopped = true;
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 3000, 3000); //
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
                        if (failed_count <= 3) {
                            GetMessageForm getLatestMessageForm = new GetMessageForm(getMessageForm.getMethod(), getMessageForm.getMerchant_id(), device_id, 1, 10, null, "");
                            getLatestMessageForm.setLatest_form(true);
                            if (getLatestMessageForm.generateMd5Sign()) {
                                prepareData(false, getLatestMessageForm);
                            }
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        isLastPage = false;
        isLoading = true;
        getMessageForm.setPage_number(1);
        prepareData(true, getMessageForm);

    }

    public void showNewCircle() {
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        View circleView = tab.getCustomView().findViewById(R.id.circle);
        if (circleView.getVisibility() != View.VISIBLE) {
            circleView.setVisibility(View.VISIBLE);
        }
    }

    public void hideNewCircle() {
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        View circleView = tab.getCustomView().findViewById(R.id.circle);
        if (circleView.getVisibility() != View.GONE) {
            circleView.setVisibility(View.GONE);
        }
    }

    public void prepareData(boolean clear, final GetMessageForm form) {
        Log.i("Caser_assistant", "--form=" + new Gson().toJson(form));
        if (clear) {
            allListView.removeAllCells(true);
            newListView.removeAllCells(true);
            hideNewCircle();
        }
        if (form.generateMd5Sign()) {
            Call<GetMessageResponse> mService = ApiClient.getInstance().getApi().getMessage(
                    form.getMethod(),
                    form.getMerchant_id(),
                    form.getDevice_id(),
                    form.getPage_number(),
                    form.getPage_limit(),
                    form.getMsg_id(),
                    form.getTime(),
                    form.getSign()
            );
            mService.enqueue(new Callback<GetMessageResponse>() {
                @Override
                public void onResponse(Call<GetMessageResponse> call, final Response<GetMessageResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            BaseResponse mBaseResponse = response.body();
                            Log.i("Assistant", "mBaseResponse=" + new Gson().toJson(mBaseResponse));
                            if (mBaseResponse.getCode() == 1) {
                                GetMessageResponse mGetMessageResponse = response.body();
                                left_nav_user_name.setText(mGetMessageResponse.getData().getMerch_name());
                                failed_count = 0;
                                isLoggedIn = true;
                                Log.e(CUSTOM_TAG, "api_success");
                                List<MessageListCell> allMessageCellList = new ArrayList<>();
                                List<MessageListCell> newMessageCellList = new ArrayList<>();
                                GetMessageResponse.DataBean data = (GetMessageResponse.DataBean) mGetMessageResponse.getData();
                                if (!form.getLatest_form() && data.getList().size() < 1) {
                                    isLastPage = true;
                                }
                                for (GetMessageResponse.DataBean.ListBean item : data.getList()) {
                                    Message msg = db.insertMessageIfNotExist(item.getId(), item.getMsg_id(), item.getTitle(), item.getContent(), item.getBiz(), item.getTel(), item.getAmount(), item.getCreated_at());
                                    MessageListItem newListItem = new MessageListItem(item.getId(), item.getTitle(), item.getContent(), item.getBiz(), item.getTel(), item.getAmount(), item.getCreated_at());
                                    MessageListCell newAllMessageListCell = new MessageListCell(newListItem);
                                    newAllMessageListCell.setOnCellClickListener(new SimpleCell.OnCellClickListener<MessageListItem>() {
                                        @Override
                                        public void onCellClicked(MessageListItem massageListItem) {
                                            newListView.removeCell(new MessageListCell(massageListItem));
                                            if (newListView.getItemCount() < 1) {
                                                hideNewCircle();
                                            }
                                            if (!db.isRead(massageListItem.getUid())) {
                                                MessageListCell newReadCell = new MessageListCell(massageListItem);
                                                newReadCell.setOnCellClickListener(new SimpleCell.OnCellClickListener<MessageListItem>() {
                                                    @Override
                                                    public void onCellClicked(MessageListItem massageListItem) {

                                                        timerStopped = true;
                                                        //start MessageDetailActivity
                                                        Intent messageDetailIntent = new Intent(getApplicationContext(), MessageDetailActivity.class);
                                                        messageDetailIntent.putExtra("title", getResources().getString(R.string.message_list_row_title));
                                                        messageDetailIntent.putExtra("content", formatVoiceMessage(massageListItem.getTel(), massageListItem.getAmount(), massageListItem.getBiz()));
                                                        messageDetailIntent.putExtra("created_at", massageListItem.getCreated_at());
                                                        startActivity(messageDetailIntent);

                                                    }
                                                });
                                                db.setReadMark(massageListItem.getUid());
                                                db.setSpeakMark(massageListItem.getUid());
                                                loadReadData(true);
                                            }
                                            timerStopped = true;
                                            //start MessageDetailActivity
                                            Intent messageDetailIntent = new Intent(getApplicationContext(), MessageDetailActivity.class);
                                            messageDetailIntent.putExtra("title", getResources().getString(R.string.message_list_row_title));
                                            messageDetailIntent.putExtra("content", formatVoiceMessage(massageListItem.getTel(), massageListItem.getAmount(), massageListItem.getBiz()));
                                            messageDetailIntent.putExtra("created_at", massageListItem.getCreated_at());
                                            startActivity(messageDetailIntent);

                                        }
                                    });
                                    MessageListCell newNewMessageListCell = new MessageListCell(newListItem);
                                    newNewMessageListCell.setOnCellClickListener(new SimpleCell.OnCellClickListener<MessageListItem>() {
                                        @Override
                                        public void onCellClicked(MessageListItem massageListItem) {
                                            newListView.removeCell(new MessageListCell(massageListItem));
                                            if (newListView.getItemCount() < 1) {
                                                hideNewCircle();
                                            }
                                            if (!db.isRead(massageListItem.getUid())) {
                                                MessageListCell newReadCell = new MessageListCell(massageListItem);
                                                newReadCell.setOnCellClickListener(new SimpleCell.OnCellClickListener<MessageListItem>() {
                                                    @Override
                                                    public void onCellClicked(MessageListItem massageListItem) {

                                                        timerStopped = true;
                                                        //start MessageDetailActivity
                                                        Intent messageDetailIntent = new Intent(getApplicationContext(), MessageDetailActivity.class);
                                                        messageDetailIntent.putExtra("title", getResources().getString(R.string.message_list_row_title));
                                                        messageDetailIntent.putExtra("content", formatVoiceMessage(massageListItem.getTel(), massageListItem.getAmount(), massageListItem.getBiz()));
                                                        messageDetailIntent.putExtra("created_at", massageListItem.getCreated_at());
                                                        startActivity(messageDetailIntent);

                                                    }
                                                });
                                                db.setReadMark(massageListItem.getUid());
                                                db.setSpeakMark(massageListItem.getUid());
                                                loadReadData(true);
                                            }
                                            timerStopped = true;
                                            //start MessageDetailActivity
                                            Intent messageDetailIntent = new Intent(getApplicationContext(), MessageDetailActivity.class);
                                            messageDetailIntent.putExtra("title", getResources().getString(R.string.message_list_row_title));
                                            messageDetailIntent.putExtra("content", formatVoiceMessage(massageListItem.getTel(), massageListItem.getAmount(), massageListItem.getBiz()));
                                            messageDetailIntent.putExtra("created_at", massageListItem.getCreated_at());
                                            startActivity(messageDetailIntent);
                                            if (!mTts.isSpeaking()) {
                                                setParam();
                                                mTts.startSpeaking(formatVoiceMessage(massageListItem.getTel(), massageListItem.getAmount(), massageListItem.getBiz()), null);

                                            }

                                        }
                                    });
                                    if (form.getLatest_form() == true && msg == null) {
                                        allMessageCellList.add(newAllMessageListCell);
                                        newMessageCellList.add(newNewMessageListCell);
                                    } else if (form.getLatest_form() == false) {
                                        allMessageCellList.add(newAllMessageListCell);
                                        if (msg != null && !msg.isIs_read() && !msg.isIs_speak()) {
                                            newMessageCellList.add(newNewMessageListCell);
                                        }
                                    }

                                }
                                allListView.hideLoadMoreView();
                                allListView.hideEmptyStateView();
                                newListView.hideEmptyStateView();
                                if (form.getLatest_form() == true) {
                                    allListView.addCells(0, allMessageCellList);
                                    newListView.addCells(0, newMessageCellList);
                                    if (allMessageCellList.size() > 0) {
                                        allListView.smoothScrollToPosition(0);
                                    }
                                    if (newMessageCellList.size() > 0) {
                                        newListView.smoothScrollToPosition(0);
                                    }
                                } else {
                                    allListView.addOrUpdateCells(allMessageCellList);
                                    newListView.addOrUpdateCells(newMessageCellList);
                                }

                                if (allListView.getItemCount() <= 0 && allMessageCellList.size() <= 0) {
                                    allListView.showEmptyStateView();
                                }
                                if (newListView.getItemCount() <= 0 && newMessageCellList.size() <= 0) {
                                    newListView.showEmptyStateView();
                                }
                                if (newListView.getItemCount() >= 1) {
                                    showNewCircle();
                                }

                                if (mTts == null) {
                                    Log.i("Casher_assistant", "mTts==null");
                                    return;
                                }
                                // speak content
                                if (!mTts.isSpeaking() && newListView.getItemCount() > 0) {
                                    currentSpeakingCell = (MessageListCell) newListView.getCell(0);
                                    if (needToSpeak(currentSpeakingCell.getItem().getCreated_at())) {
                                        setParam();
                                        mTts.startSpeaking(formatVoiceMessage(((MessageListItem) currentSpeakingCell.getItem()).getTel(), ((MessageListItem) currentSpeakingCell.getItem()).getAmount(), ((MessageListItem) currentSpeakingCell.getItem()).getBiz()), mTtsListener);
                                    } else {
                                        currentSpeakingCell = null;
                                    }
                                }
                            } else if (mBaseResponse.getCode() == 2) {
                                failed_count++;
                                GetMessageResponse mGetMessageResponse = response.body();
                                left_nav_user_name.setText("");
                                String time = (String) mGetMessageResponse.getData().getTime();
                                SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat mSimpleDateFormatResult = new SimpleDateFormat("yyyyMMddHHmmss");
                                Date resultData = null;
                                try {
                                    resultData = mSimpleDateFormatResult.parse(time);
                                    String serverDate = mSimpleDateFormat.format(resultData);
                                    Date localDate = new Date();
                                    String local = mSimpleDateFormat.format(localDate);
                                    Log.i("AAA", "111111111111111111serverDate=" + serverDate + ";local=" + local);

                                    between = resultData.getTime() - localDate.getTime();
                                    getMessageForm.setBetween(between);//设置时间差

                                    Log.i("AAA", "between=" + between);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (mGetMessageResponse.getData().getMerch_id().equals("false")) {
                                    Log.e(CUSTOM_TAG, "api_failed1");
                                    allListView.hideLoadMoreView();
                                    isLoggedIn = false;
                                    showLoginPage();
                                    db.clearMessages();
                                    readListView.removeAllCells(true);
                                    new android.os.Handler().postDelayed(
                                            new Runnable() {
                                                public void run() {
                                                    if (failed_count <= 3) {
                                                        prepareData(false, getMessageForm);
                                                    }
                                                }
                                            },
                                            3000);
                                } else {
                                    isLoggedIn = true;
                                    Log.e(CUSTOM_TAG, "api_failed2");
                                    getMessageForm.setMerchant_id(mGetMessageResponse.getData().getMerch_id());
                                    tabLayout.getTabAt(0).select();
                                    showAllList();
                                    new android.os.Handler().postDelayed(
                                            new Runnable() {
                                                public void run() {
                                                    if (failed_count <= 3) {
                                                        prepareData(false, getMessageForm);
                                                    }
                                                }
                                            },
                                            1000);
                                }
                            }
                        } else {
                            left_nav_user_name.setText("");
                            failed_count++;
                            String s = response.errorBody().string();
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                Toast.makeText(getApplicationContext(), jsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), R.string.error_failure_wrong_format, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                Log.e("failed_json_response", s);
                            }
                            allListView.hideLoadMoreView();
                        }

                    } catch (IOException e) {
                        left_nav_user_name.setText("");
                        failed_count++;
                        Toast.makeText(getApplicationContext(), R.string.error_failure_wrong_format, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        allListView.hideLoadMoreView();

                    }
                    swipeRefreshLayout.setRefreshing(false);

                    if (!form.getLatest_form()) {
                        isLoading = false;
                    }

                }

                @Override
                public void onFailure(Call<GetMessageResponse> call, Throwable t) {
                    failed_count++;
                    allListView.hideLoadMoreView();
                    left_nav_user_name.setText("");
                    swipeRefreshLayout.setRefreshing(false);
                    if (!form.getLatest_form()) {
                        isLoading = false;
                    }

                    call.cancel();
                    Toast.makeText(getApplicationContext(), R.string.error_failure_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public String formatVoiceMessage(String tel, String amount, String biz) {
//        return getString(R.string.voice_message_template, tel, HelperFunctions.filterContent(replaceRegPattern, amount));
        if (biz.equals("5") || biz.equals("95")) {
            //转账和二维码（都是汇旺的）
            return getString(R.string.voice_message_template, HelperFunctions.filterContent(replaceRegPattern, amount));
        } else if (biz.equals("7")) {
            //扫码收款（支付宝的）
            return getString(R.string.voice_message_alipay, HelperFunctions.filterContent(replaceRegPattern, amount));
        } else {
            return getString(R.string.voice_message_template, HelperFunctions.filterContent(replaceRegPattern, amount));
        }
    }

    public boolean needToSpeak(String time) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date resultData = null;
        try {
            resultData = mSimpleDateFormat.parse(time);
            Date localDate = new Date();

            between = localDate.getTime() - resultData.getTime();
            return between < 3 * 60 * 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void loadReadData(boolean clear) {
        readListView.showLoadMoreView();
        if (clear) {
            readListView.removeAllCells(true);
        }
        List<MessageListCell> readMessageCellList = new ArrayList<>();
        for (MessageListItem item : db.getMessagesByTpe(Message.READ)) {
            MessageListCell cell = new MessageListCell(item);
            cell.setOnCellClickListener(new SimpleCell.OnCellClickListener<MessageListItem>() {
                @Override
                public void onCellClicked(MessageListItem massageListItem) {
                    timerStopped = true;
                    //start MessageDetailActivity
                    Intent messageDetailIntent = new Intent(getApplicationContext(), MessageDetailActivity.class);
                    messageDetailIntent.putExtra("title", getResources().getString(R.string.message_list_row_title));
                    messageDetailIntent.putExtra("content", formatVoiceMessage(massageListItem.getTel(), massageListItem.getAmount(), massageListItem.getBiz()));
                    messageDetailIntent.putExtra("created_at", massageListItem.getCreated_at());
                    startActivity(messageDetailIntent);

                }
            });
            readMessageCellList.add(cell);
        }
        readListView.addCells(readMessageCellList);
        if (readListView.getItemCount() < 1) {
            readListView.showEmptyStateView();
        }
    }

    public void showAllList() {
        hideAllLists();
        if (isLoggedIn) {
            allListPanel.setVisibility(View.VISIBLE);
        } else {
            noLoginPanel.setVisibility(View.VISIBLE);
        }
    }

    public void showNewList() {
        hideAllLists();
        if (isLoggedIn) {
            newListPanel.setVisibility(View.VISIBLE);
        } else {
            noLoginPanel.setVisibility(View.VISIBLE);
        }
    }

    public void showReadList() {
        hideAllLists();
        if (isLoggedIn) {
            readListPanel.setVisibility(View.VISIBLE);
        } else {
            noLoginPanel.setVisibility(View.VISIBLE);
        }
    }

    public void showLoginPage() {
        hideAllLists();
        noLoginPanel.setVisibility(View.VISIBLE);
    }

    public void hideAllLists() {
        allListPanel.setVisibility(View.GONE);
        newListPanel.setVisibility(View.GONE);
        readListPanel.setVisibility(View.GONE);
        noLoginPanel.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.collapse_menu_button:
                drawerLayout.openDrawer(Gravity.START);
                break;
            case R.id.left_nav_join_merchant:
            case R.id.join_merchant_button:
                goToJoinMerchantPage();
                break;
            case R.id.left_nav_security_setting:
                goToSecuritySettingPage();
                break;
            default:
                break;
        }
    }

    public void goToJoinMerchantPage() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        }
        timerStopped = true;
        Intent joinMerchantIntent = new Intent(getApplicationContext(), JoinMerchantActivity.class);
        startActivity(joinMerchantIntent);
    }

    public void goToSecuritySettingPage() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        }
        timerStopped = true;
        Intent securitySettingIntent = new Intent(getApplicationContext(), SecuritySettingActivity.class);
        securitySettingIntent.putExtra("action_type", "old");
        startActivity(securitySettingIntent);

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //支持实时音频返回，仅在synthesizeToUri条件下支持
        //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
        // 设置在线合成发音人
        if (Locale.getDefault().getISO3Language().equals("eng")) {
            mTts.setParameter(SpeechConstant.VOICE_NAME, "catherine");
        } else {
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        }
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");

        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.pcm");
    }
}
