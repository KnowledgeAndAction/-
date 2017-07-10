package com.example.administrator.my.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.administrator.my.R;
import com.example.administrator.my.activity.LoginActivity;
import com.example.administrator.my.bean.ExitEvent;
import com.example.administrator.my.utils.Constant;
import com.example.administrator.my.utils.SpUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * 设置界面——崔国钊
 */

public class SettingFragment extends BaseFragment implements View.OnClickListener{
    private Button button_changeWorld;
    private Button button_esc;
    @Override
    public void fetchData() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_bottom_setting, container, false);
        inItUI(view);
        return view;
    }

    private void inItUI(View view) {
        button_esc= (Button) view.findViewById(R.id.button_esc);
        button_changeWorld= (Button) view.findViewById(R.id.button_changeWorld);
        button_esc.setOnClickListener(this);
        button_changeWorld.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.button_changeWorld:
                Toast.makeText(getContext(),"正在开发中", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_esc:
                EventBus.getDefault().post(new ExitEvent());
                startActivity(new Intent(getContext(), LoginActivity.class));

                SpUtil.putBoolean(Constant.IS_REMBER_PWD,false);

                break;
        }
    }
}
