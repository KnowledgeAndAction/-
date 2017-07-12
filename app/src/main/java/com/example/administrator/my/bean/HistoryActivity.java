package com.example.administrator.my.bean;

import java.io.Serializable;

/**
 * Created by 666 on 2017/7/12.
 */

public class HistoryActivity implements Serializable{
    private String hStudnetNum;
    private String hInTime;
    private String hActivityId;
    private String hOutTime;
    private String hTime;
    private String hLocationt;
    private String hActivityName;
    private String ActivityDescription;

    public String gethStudnetNum() {
        return hStudnetNum;
    }

    public void sethStudnetNum(String hStudnetNum) {
        this.hStudnetNum = hStudnetNum;
    }

    public String gethInTime() {
        return hInTime;
    }

    public void sethInTime(String hInTime) {
        this.hInTime = hInTime;
    }

    public String gethActivityId() {
        return hActivityId;
    }

    public void sethActivityId(String activityId) {
        hActivityId = activityId;
    }

    public String gethOutTime() {
        return hOutTime;
    }

    public void sethOutTime(String hOutTime) {
        this.hOutTime = hOutTime;
    }

    public String gethTime() {
        return hTime;
    }

    public void sethTime(String hTime) {
        this.hTime = hTime;
    }

    public String getLocation() {
        return hLocationt;
    }

    public void sethLocation(String hLocationt) {
        this.hLocationt = hLocationt;
    }

    public String gethActivityName() {
        return hActivityName;
    }

    public void sethActivityName(String hActivityName) {
        this.hActivityName= hActivityName;
    }

    public String getActivityDescription() {
        return ActivityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        ActivityDescription = activityDescription;
    }
}
