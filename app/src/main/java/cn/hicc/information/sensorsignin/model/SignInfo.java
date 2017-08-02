package cn.hicc.information.sensorsignin.model;

/**
 * 活动签到记录
 */

public class SignInfo {
    private String name;
    private String inTime;
    private String outTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }
}
