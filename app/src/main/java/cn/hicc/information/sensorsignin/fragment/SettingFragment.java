package cn.hicc.information.sensorsignin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import cn.hicc.information.sensorsignin.activity.ChangePswActivity;
import cn.hicc.information.sensorsignin.activity.LoginActivity;
import cn.hicc.information.sensorsignin.model.ExitEvent;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * 设置界面——崔国钊
 */

public class SettingFragment extends BaseFragment implements View.OnClickListener{
    private TextView button_changeWorld;
    private TextView button_esc;
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
        button_esc= (TextView) view.findViewById(R.id.button_esc);
        button_changeWorld= (TextView) view.findViewById(R.id.button_changeWorld);
        button_esc.setOnClickListener(this);
        button_changeWorld.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.button_changeWorld:
                Intent intent=new Intent(getContext(),ChangePswActivity.class);
                startActivity(intent);
                break;
            case R.id.button_esc:
                EventBus.getDefault().post(new ExitEvent());
                startActivity(new Intent(getContext(), LoginActivity.class));

                SpUtil.putBoolean(Constant.IS_REMBER_PWD,false);

                break;
        }
    }
}
