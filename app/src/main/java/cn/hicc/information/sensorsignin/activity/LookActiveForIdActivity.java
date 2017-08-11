package cn.hicc.information.sensorsignin.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 管理员查看云子上的活动——陈帅
 */
public class LookActiveForIdActivity extends AppCompatActivity {
    private static final int SCAN_CODE = 0;
    private static final int REAL = 1;
    private static final int NO_REAL = 0;
    private EditText et_yunzi_id;
    private Button bt_submit;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    private LinearLayout ll_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_active_for_id);

        // 初始化控件
        initView();

        // 提交活动
        submitActive();
    }


    // 提交活动
    private void submitActive() {
        // 提交活动按钮点击事件
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String yunziId = et_yunzi_id.getText().toString().trim();
                // 如果不为空，查看活动
                if (!yunziId.equals("")) {
                    ll_root.removeAllViews();

                    showProgressDialog();
                    quireYunziActive(yunziId);
                } else {
                    ToastUtil.show("请填写云子id");
                }
            }
        });
    }


    // 查询该云子上是否已经有活动
    private void quireYunziActive(final String yunziId) {
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TActivity/GetActivity")
                .addParams("sensorId", yunziId)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        ToastUtil.show("查看活动失败，请稍后重试"+e.toString());
                        closeProgressDialog();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            boolean sucessed = jsonObject.getBoolean("sucessed");
                            if (sucessed) {
                                JSONArray data = jsonObject.getJSONArray("data");
                                for (int j=0; j<data.length(); j++) {
                                    JSONObject activity = data.getJSONObject(j);
                                    String name = activity.getString("ActivityName");
                                    String des = activity.getString("ActivityDescription");
                                    String location = activity.getString("Location");
                                    String time = activity.getString("Time");
                                    String endTime = activity.getString("EndTime");
                                    String rule = activity.getString("Rule");
                                    int show = activity.getInt("Show");

                                    // 创建view
                                    View view = LayoutInflater.from(LookActiveForIdActivity.this).inflate(R.layout.item_look_active, ll_root, false);
                                    // 初始化控件
                                    TextView tv_active_name = (TextView) view.findViewById(R.id.tv_active_name);
                                    TextView tv_active_des = (TextView) view.findViewById(R.id.tv_active_des);
                                    TextView tv_active_location = (TextView) view.findViewById(R.id.tv_active_location);
                                    TextView tv_active_date = (TextView) view.findViewById(R.id.tv_active_date);
                                    TextView tv_active_end_date = (TextView) view.findViewById(R.id.tv_active_end_date);
                                    TextView tv_active_type = (TextView) view.findViewById(R.id.tv_active_type);
                                    TextView tv_active_show = (TextView) view.findViewById(R.id.tv_active_show);
                                    // 展示信息
                                    tv_active_name.setText("活动名称：" + name);
                                    tv_active_des.setText("活动详情：" + des);
                                    tv_active_location.setText("活动地点：" + location);
                                    tv_active_date.setText("活动开始时间：" + time.replace("T", " ").substring(0, 16));
                                    tv_active_end_date.setText("活动结束时间：" + endTime.replace("T", " ").substring(0, 16));
                                    switch (Integer.valueOf(rule)) {
                                        case REAL:
                                            tv_active_type.setText("活动类型：日常活动");
                                            break;
                                        case NO_REAL:
                                            tv_active_type.setText("活动类型：普通活动");
                                            break;
                                    }
                                    switch (show) {
                                        case 0:
                                            tv_active_show.setText("已删除");
                                            break;
                                        case 1:
                                            tv_active_show.setText("未删除");
                                            break;
                                    }
                                    // 添加到父布局中
                                    ll_root.addView(view);
                                }

                                if (data.length() == 0) {
                                    ToastUtil.show("这个云子上没有活动");
                                }

                                closeProgressDialog();
                            } else {
                                ToastUtil.showLong("这个云子上没有活动");
                                closeProgressDialog();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.show("查看活动失败，请稍后重试"+e.toString());
                            closeProgressDialog();
                        }
                    }
                });
    }


    // 初始化控件
    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("查看云子活动");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ll_root = (LinearLayout) findViewById(R.id.ll_root);
        et_yunzi_id = (EditText) findViewById(R.id.et_yunzi_id);

        bt_submit = (Button) findViewById(R.id.bt_submit);


        ImageView iv_scan = (ImageView) findViewById(R.id.iv_scan);
        iv_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(LookActiveForIdActivity.this, ScanActivity.class),SCAN_CODE);
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
        progressDialog.setMessage("查询中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
