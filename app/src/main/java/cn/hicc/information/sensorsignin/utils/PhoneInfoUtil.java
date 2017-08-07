package cn.hicc.information.sensorsignin.utils;

import android.telephony.TelephonyManager;

import cn.hicc.information.sensorsignin.MyApplication;
import cn.hicc.information.sensorsignin.model.PhoneInfo;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by 陈帅 on 2017/08/07/024.
 */

public class PhoneInfoUtil {
    private static PhoneInfo phoneInfo;

    /**
     * 获取手机信息
     */
    public static PhoneInfo getPhoneInfo() {
        phoneInfo = new PhoneInfo();
        TelephonyManager tm = (TelephonyManager) MyApplication.getContext().getSystemService(TELEPHONY_SERVICE);
        String mtyb = android.os.Build.BRAND;// 手机品牌
        phoneInfo.setPhoneBrand(mtyb);
        String mtype = android.os.Build.MODEL; // 手机型号
        phoneInfo.setPhoneBrandType(mtype);
        phoneInfo.setAndroidVersion("Android "+android.os.Build.VERSION.RELEASE);

        return phoneInfo;
    }
}
