package cn.hicc.information.sensorsignin.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/6/9.
 */

public class Active implements Serializable{
    private String activeName;
    private String activeDes;
    private String activeTime;
    private String activeLocation;
    private String sersorID;

    public String getSersorID() {
        return sersorID;
    }

    public void setSersorID(String sersorID) {
        this.sersorID = sersorID;
    }

    public String getActiveName() {
        return activeName;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
    }

    public String getActiveDes() {
        return activeDes;
    }

    public void setActiveDes(String activeDes) {
        this.activeDes = activeDes;
    }

    public String getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(String activeTime) {
        this.activeTime = activeTime;
    }

    public String getActiveLocation() {
        return activeLocation;
    }

    public void setActiveLocation(String activeLocation) {
        this.activeLocation = activeLocation;
    }
}
