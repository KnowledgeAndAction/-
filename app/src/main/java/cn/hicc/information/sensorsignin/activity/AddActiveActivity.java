package cn.hicc.information.sensorsignin.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.hicc.information.sensorsignin.R;
import com.uuzuche.lib_zxing.activity.CodeUtils;
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
public class AddActiveActivity extends AppCompatActivity {
    private static final int SCAN_CODE = 0;
    private static final int REAL = 1;
    private static final int NO_REAL = 0;
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
    private String mEndTime = "";
    private String mDate = "";
    private String mEndDate = "";
    private int mRule = -1;
    private Toolbar toolbar;
    private Button bt_active_end_time;
    private Button bt_active_end_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_active);

        // 初始化控件
        initView();

        // 提交活动
        submitActive();
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
                        && !yunziId.equals("") && !mDate.equals("") && !mTime.equals("") && mRule != -1
                        && !mEndDate.equals("") && !mEndTime.equals("")) {
                    String dateTime = mDate + " " + mTime;
                    String endDateTime = mEndDate + " " + mEndTime;
                    showConfirmDialog(activeName, activeDes, activeLocation, yunziId, dateTime,endDateTime);
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

    // 显示确认对话框
    protected void showConfirmDialog(final String activeName, final String activeDes, final String activeLocation, final String yunziId, final String dateTime, final String endDateTime) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        // 设置对话框左上角图标
        builder.setIcon(R.mipmap.logo2);
        // 设置不能取消
        builder.setCancelable(false);
        // 设置对话框标题
        builder.setTitle("添加活动");
        // 设置对话框内容
        builder.setMessage("您确认活动填写正确，添加该活动？");
        // 设置积极的按钮
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 提交活动
                showProgressDialog();
                submitService(activeName, activeDes, activeLocation, yunziId, dateTime,endDateTime,mRule);
                dialog.dismiss();
            }
        });
        // 设置消极的按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    // 提交活动信息到数据库
    private void submitService(String activeName, String activeDes, String activeLocation, String yunziId, String dateTime, String endDateTime, int mRule) {
        // GET方法提交
        /*OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TActivity/AddActivity")
                .addParams("activityname", activeName)
                .addParams("activitydec", activeDes)
                .addParams("sensorid", yunziId)
                .addParams("time", dateTime)
                .addParams("location", activeLocation)
                .addParams("rule", ""+mRule)
                .addParams("account", SpUtil.getString(Constant.ACCOUNT,""))
                .addParams("endtime", endDateTime)
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

        // post
        OkHttpUtils
                .post()
                .url(Constant.API_URL + "api/TActivity/AddActivityPost?sensroId=" + yunziId)
                .addParams("ActivityName", activeName)
                .addParams("ActivityDescription", activeDes)
                .addParams("Time", dateTime)
                .addParams("Location", activeLocation)
                .addParams("Rule", ""+mRule)
                .addParams("Account", SpUtil.getString(Constant.ACCOUNT,""))
                .addParams("EndTime", endDateTime)
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
                finish();
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("添加活动");
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
        et_yunzi_id = (EditText) findViewById(R.id.et_yunzi_id);
        et_active_location = (EditText) findViewById(R.id.et_active_location);

        bt_active_date = (Button) findViewById(R.id.bt_active_date);
        bt_active_time = (Button) findViewById(R.id.bt_active_time);
        bt_active_end_date = (Button) findViewById(R.id.bt_active_end_date);
        bt_active_end_time = (Button) findViewById(R.id.bt_active_end_time);
        bt_submit = (Button) findViewById(R.id.bt_submit);

        rg_rule = (RadioGroup) findViewById(R.id.rg_rule);

        ImageView iv_scan = (ImageView) findViewById(R.id.iv_scan);
        iv_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AddActiveActivity.this, ScanActivity.class),SCAN_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == SCAN_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    try {
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                        // 解析后操作
                        if (result.substring(0,4).equals("http")) {
                            result = result.substring(13,25);
                        } else {
                            result = result.substring(0,12);
                        }
                        et_yunzi_id.setText(result);
                    } catch (StringIndexOutOfBoundsException e) {
                        ToastUtil.showLong("请确保您扫描的是云子上的二维码");
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    ToastUtil.show("解析二维码失败");
                }
            }
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("提交中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
