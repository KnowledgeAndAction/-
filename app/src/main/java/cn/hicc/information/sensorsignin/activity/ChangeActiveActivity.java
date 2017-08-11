package cn.hicc.information.sensorsignin.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.hicc.information.sensorsignin.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 修改活动——陈帅
 */
public class ChangeActiveActivity extends AppCompatActivity {

    private static final int REAL = 1;
    private static final int NO_REAL = 0;
    private Active active;
    private Toolbar toolbar;
    private EditText et_active_name;
    private EditText et_active_des;
    private EditText et_active_location;
    private Button bt_active_date;
    private Button bt_active_time;
    private Button bt_active_end_date;
    private Button bt_active_end_time;
    private Button bt_submit;
    private RadioGroup rg_rule;
    private ProgressDialog progressDialog;
    private String mTime = "";
    private String mDate = "";
    private String mEndDate = "";
    private String mEndTime = "";
    private int mRule = -1;
    private String oldDateTime;
    private String oldEndDateTime;
    // 记录发请求的个数
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_active);

        // 获得数据
        getData();

        // 初始化控件
        initView();

        // 修改活动点击事件
        submitActiveClick();
    }


    // 获得数据
    private void getData() {
        Intent intent = getIntent();
        active = (Active) intent.getSerializableExtra("active");
        oldDateTime = active.getActiveTime().replace("T"," ").substring(0, 16);
        oldEndDateTime = active.getEndTime().replace("T"," ").substring(0, 16);
    }

    // 初始化控件
    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("修改活动");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        et_active_name = (EditText) findViewById(R.id.et_active_name);
        et_active_des = (EditText) findViewById(R.id.et_active_des);
        et_active_location = (EditText) findViewById(R.id.et_active_location);

        bt_active_date = (Button) findViewById(R.id.bt_active_date);
        bt_active_time = (Button) findViewById(R.id.bt_active_time);
        bt_active_end_date = (Button) findViewById(R.id.bt_active_end_date);
        bt_active_end_time = (Button) findViewById(R.id.bt_active_end_time);
        bt_submit = (Button) findViewById(R.id.bt_submit);

        rg_rule = (RadioGroup) findViewById(R.id.rg_rule);

        // 设置活动名称，描述，地点
        String dateTime = active.getActiveTime();
        String[] dateAndTime = dateTime.split("T");
        String endTime = active.getEndTime();
        String[] et = endTime.split("T");
        et_active_name.setText(active.getActiveName());
        et_active_des.setText(active.getActiveDes());
        et_active_location.setText(active.getActiveLocation());
        // 设置活动开始时间和结束时间
        mDate = dateAndTime[0];
        mTime = dateAndTime[1].substring(0, 5);
        mEndDate = et[0];
        mEndTime = et[1].substring(0, 5);
        bt_active_date.setText(mDate);
        bt_active_time.setText(mTime);
        bt_active_end_date.setText(mEndDate);
        bt_active_end_time.setText(mEndTime);
        // 设置活动类型
        int id = -1;
        mRule = active.getRule();
        switch (active.getRule()) {
            case REAL:
                id = R.id.rb_real;
                break;
            case NO_REAL:
                id = R.id.rb_no_real;
                break;
        }
        rg_rule.check(id);
    }

    // 修改活动按钮点击事件
    private void submitActiveClick() {
        // 修改活动按钮点击事件
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activeName = et_active_name.getText().toString().trim();
                String activeDes = et_active_des.getText().toString().trim();
                String activeLocation = et_active_location.getText().toString().trim();

                // 如果都不为空，提交活动
                if (!activeName.equals("") && !activeDes.equals("") && !activeLocation.equals("")
                        && !mDate.equals("") && !mTime.equals("") && mRule != -1
                        && !mEndDate.equals("") && !mEndTime.equals("")) {
                    String dateTime = mDate + " " + mTime;
                    String endTime = mEndDate + " " + mEndTime;
                    // 提交修改的活动到服务器
                    submitChangeActive(activeName, activeDes, activeLocation, dateTime, endTime, mRule);
                } else {
                    ToastUtil.show("请将活动信息填写完整");
                }
            }
        });

        // RadioGroup点击监听事件
        rg_rule.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_real:
                        mRule = REAL;
                        break;
                    case R.id.rb_no_real:
                        mRule = NO_REAL;
                        break;
                }
            }
        });

        // 设置时间选择按钮点击事件
        setDateTime();
    }

    // 提交修改的活动  如果和原来的不一样 就提交修改
    private void submitChangeActive(String activeName, String activeDes, String activeLocation, String dateTime, String endTime, int mRule) {
        if (!activeName.equals(active.getActiveName())) {
            count++;
        }

        if (!activeDes.equals(active.getActiveDes())) {
            count++;
        }

        if (!activeLocation.equals(active.getActiveLocation())) {
            count++;
        }

        if (!dateTime.equals(oldDateTime)) {
            count++;
        }

        if (!endTime.equals(oldEndDateTime)) {
            count++;
        }

        if (mRule != active.getRule()) {
            count++;
        }

        // 如果要更改的个数不变，就不提交
        if (count == 0) {
            ToastUtil.show("您没有做任何修改");
        } else {
            changeActive(activeName, activeDes, dateTime, activeLocation, mRule, endTime);
            showProgressDialog();
        }
    }

    // TODO 提交修改的活动到服务器
    private void changeActive(String activityName, String activityDescription, String time, String location, int rule, String endTime) {
        OkHttpUtils
                .post()
                .url(Constant.API_URL + "api/TActivity/UpdT_Activity")
                .addParams("Nid", active.getActiveId()+"")
                .addParams("ActivityName", activityName)
                .addParams("ActivityDescription", activityDescription)
                .addParams("Time", time)
                .addParams("EndTime", endTime)
                .addParams("Location", location)
                .addParams("Rule", rule+"")
                .addParams("Show", "1")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                            closeProgressDialog();
                            ToastUtil.show("修改活动失败，请稍后重试" + e.toString());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean sucessed = jsonObject.getBoolean("sucessed");
                            closeProgressDialog();
                            if (sucessed) {
                                ToastUtil.show("修改活动成功");
                                finish();
                            } else {
                                ToastUtil.show("修改活动失败");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            closeProgressDialog();
                            ToastUtil.show("修改活动失败，请稍后重试" + e.toString());
                        }
                    }
                });
    }

    // 设置时间选择按钮点击事件
    private void setDateTime() {
        // 选择日期按钮
        bt_active_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
                                mDate = i + "-" + (i1 + 1) + "-" + i2;
                                bt_active_date.setText(mDate);
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                //dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                dpd.setAccentColor("#154db4");
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        // 选择时间按钮
        bt_active_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePickerDialog timePickerDialog, int i, int i1, int i2) {
                                mTime = i + ":" + i1;
                                bt_active_time.setText(mTime);
                            }
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                tpd.setAccentColor("#154db4");
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });

        // 选择结束日期按钮
        bt_active_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
                                mEndDate = i + "-" + (i1 + 1) + "-" + i2;
                                bt_active_end_date.setText(mEndDate);
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                //dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                dpd.setAccentColor("#154db4");
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        // 选择结束时间按钮
        bt_active_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePickerDialog timePickerDialog, int i, int i1, int i2) {
                                mEndTime = i + ":" + i1;
                                bt_active_end_time.setText(mEndTime);
                            }
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                tpd.setAccentColor("#154db4");
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("修改中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
