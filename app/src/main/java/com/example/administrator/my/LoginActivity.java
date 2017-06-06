package com.example.administrator.my;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.app.ProgressDialog;

import com.sensoro.cloud.SensoroManager;


public class LoginActivity extends AppCompatActivity {

    private Button login;
    private EditText accountEdit;
    private EditText passwordEdit;
    private CheckBox rememberPass;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox seeEdit;
    private SharedPreferences.Editor edit;

    private ProgressDialog progressDialog;
    private SensoroManager sensoroManager;
    private boolean isShow = true;
    private Double light;
    private String serialNumber;
    private String accuracy;

    private String lightString;
    private String rssi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sensoroManager = SensoroManager.getInstance(LoginActivity.this);
        //初始化控件
        initWidget();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSee = pref.getBoolean("remember_see", false);
        if (isSee) {
            seeEdit.setChecked(true);
        }
        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        login = (Button) findViewById(R.id.bt_login);
        accountEdit = (EditText) findViewById(R.id.userNumber);
        rememberPass = (CheckBox) findViewById(R.id.cb_choose);
        seeEdit = (CheckBox) findViewById(R.id.cb_see);
        passwordEdit = (EditText) findViewById(R.id.password);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNumber();     //验证帐号密码
            }
        });
    }
    /**
     * 帐号密码的验证ss
     */
    private void checkNumber() {
        String account = accountEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        if (account.equals("123") && password.equals("123")) {
            editor = pref.edit();
            if (rememberPass.isChecked()) {
                editor.putBoolean("remember_password", true);
                editor.putString("account", account);
                editor.putString("password", password);
            } else {
                editor.clear();
            }
            editor.apply();
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "账号或密码错误", Toast.LENGTH_SHORT).show();
        }
    }


//    @Override
//    protected void onActivityResult(final int requestCode, final int resultCode,
//                                    final Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //检查蓝牙是否开启
//        if (!sensoroManager.isBluetoothEnabled()) {
//            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(bluetoothIntent, 0);
//        }
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensoroManager != null) {
            sensoroManager.stopService();
        }
        System.exit(0);//退出应用
    }
}
