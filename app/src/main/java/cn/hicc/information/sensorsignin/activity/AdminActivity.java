package cn.hicc.information.sensorsignin.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
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

import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
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
    private Button bt_exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // 初始化控件
        initView();

        // 提交活动
        submitActive();

        // 注销
        exit();
    }

    private void exit() {
        bt_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                SpUtil.putBoolean(Constant.IS_REMBER_PWD,false);
                finish();
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
    }

    // 提交活动
    private void submitActive() {
        // 提交活动按钮点击事件
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activeName = et_active_name.getText().toString().trim();
                String activeDes = et_active_des.getText().toString().trim();
                String activeLocation = et_active_location.getText().toString().trim();
                String yunziId = et_yunzi_id.getText().toString().trim();

                // 如果都不为空，提交活动
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

    // 提交活动信息到数据库
    private void submitService(String activeName, String activeDes, String activeLocation, String yunziId, String dateTime, int mRule) {
        // GET方法提交
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TActivity/AddActivity")
                .addParams("activityname", activeName)
                .addParams("activitydec", activeDes)
                .addParams("sensorid", yunziId)
                .addParams("time", dateTime)
                .addParams("location", activeLocation)
                .addParams("rule", ""+mRule)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.show("提交失败：" + e.toString());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        AnalysisJson(response);
                    }
                });

        // POST方法提交
        /*OkHttpUtils
                .post()
                .url(Constant.API_URL + "api/TActivity/AddActivityPost")
                .addParams("ActivityName", activeName)
                .addParams("ActivityDescription", activeDes)
                .addParams("Time", dateTime)
                .addParams("Location", activeLocation)
                .addParams("Rule", ""+mRule)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.show("提交失败：" + e.toString());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        AnalysisJson(response);
                    }
                });*/
    }

    // 解析json数据
    private void AnalysisJson(String response) {
        try {
            closeProgressDialog();

            JSONObject jsonObject = new JSONObject(response);
            boolean sucessed = jsonObject.getBoolean("sucessed");
            if (sucessed) {
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
            closeProgressDialog();
            ToastUtil.show("提交失败：" + e.toString());
        }
    }

    // 初始化控件
    private void initView() {
        et_active_name = (EditText) findViewById(R.id.et_active_name);
        et_active_des = (EditText) findViewById(R.id.et_active_des);
        et_yunzi_id = (EditText) findViewById(R.id.et_yunzi_id);
        et_active_location = (EditText) findViewById(R.id.et_active_location);

        bt_active_date = (Button) findViewById(R.id.bt_active_date);
        bt_active_time = (Button) findViewById(R.id.bt_active_time);
        bt_submit = (Button) findViewById(R.id.bt_submit);
        bt_exit = (Button) findViewById(R.id.bt_exit);

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
