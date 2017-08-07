package cn.hicc.information.sensorsignin.model;

/**
 * Created by 陈帅 on 2017/8/7/007.
 */

public class FeedBack {
    private String account;
    private String msg;
    private String PhoneBrand;
    private String PhoneBrandType;
    private String AndroidVersion;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getPhoneBrand() {
        return PhoneBrand;
    }

    public void setPhoneBrand(String phoneBrand) {
        PhoneBrand = phoneBrand;
    }

    public String getPhoneBrandType() {
        return PhoneBrandType;
    }

    public void setPhoneBrandType(String phoneBrandType) {
        PhoneBrandType = phoneBrandType;
    }

    public String getAndroidVersion() {
        return AndroidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        AndroidVersion = androidVersion;
    }
}
