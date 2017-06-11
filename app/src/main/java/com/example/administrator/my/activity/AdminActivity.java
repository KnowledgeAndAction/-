package com.example.administrator.my.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.administrator.my.R;
import com.example.administrator.my.utils.Constant;
import com.example.administrator.my.utils.ToastUtil;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.Call;

/**
 * 管理员添加活动界面——陈帅
 */
public class AdminActivity extends AppCompatActivity {

    private static final int REAL = 0;
    private static final int NO_REAL = 1;
    private EditText et_active_name;
    private EditText et_active_des;
    private EditText et_yunzi_id;
    private EditText et_active_location;
    private Button bt_active_date;
    private Button bt_active_time;
    private Button bt_submit;
    private RadioGroup rg_rule;
    private ProgressDialog progressDialog;
    private String mTime = "";
    private String mDate = "";
    private int mRule = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initView();



        submitActive();
    }

    private void setDateTime() {
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
    }

    private void submitActive() {
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activeName = et_active_name.getText().toString().trim();
                String activeDes = et_active_des.getText().toString().trim();
                String activeLocation = et_active_location.getText().toString().trim();
                String yunziId = et_yunzi_id.getText().toString().trim();


                if (!activeName.equals("") && !activeDes.equals("") && !activeLocation.equals("")
                        && !yunziId.equals("") && !mDate.equals("") && !mTime.equals("") && mRule != -1) {
                    String dateTime = mDate + " " + mTime;
                    showProgressDialog();
                    submitService(activeName, activeDes, activeLocation, yunziId, dateTime, mRule);
                } else {
                    ToastUtil.show("请将活动信息填写完整");
                }
            }
        });

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

        setDateTime();
    }

    private void submitService(String activeName, String activeDes, String activeLocation, String yunziId, String dateTime, int mRule) {
        OkHttpUtils.get().url(Constant.TEST_URL + "setActivity.do")
                .addParams("activityName",activeName)
                .addParams("activityDes",activeDes)
                .addParams("sensoroId",yunziId)
                .addParams("time",dateTime)
                .addParams("location",activeLocation)
                .addParams("rule",""+mRule)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        closeProgressDialog();
                        ToastUtil.show("提交失败，请稍后重试");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            closeProgressDialog();

                            JSONObject jsonObject = new JSONObject(response);
                            boolean flag = jsonObject.getBoolean("flag");
                            if (flag) {
                                et_active_name.setText("");
                                et_active_des.setText("");
                                et_yunzi_id.setText("");
                                et_active_location.setText("");
                                bt_active_date.setText("请选择日期");
                                bt_active_time.setText("请选择时间");

                                mDate = "";
                                mTime = "";

                                ToastUtil.showLong("提交成功");
                            } else {
                                ToastUtil.show("提交失败，请稍后重试");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void initView() {
        et_active_name = (EditText) findViewById(R.id.et_active_name);
        et_active_des = (EditText) findViewById(R.id.et_active_des);
        et_yunzi_id = (EditText) findViewById(R.id.et_yunzi_id);
        et_active_location = (EditText) findViewById(R.id.et_active_location);

        bt_active_date = (Button) findViewById(R.id.bt_active_date);
        bt_active_time = (Button) findViewById(R.id.bt_active_time);
        bt_submit = (Button) findViewById(R.id.bt_submit);


        rg_rule = (RadioGroup) findViewById(R.id.rg_rule);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("提交中...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
