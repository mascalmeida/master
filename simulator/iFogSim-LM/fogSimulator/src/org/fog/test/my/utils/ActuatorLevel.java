package org.fog.test.my.utils;

public class ActuatorLevel extends Level {
    private String type;
    private int userId;
    private String appId;

    public ActuatorLevel(int hostPerParent, String prefix, String type, double upLatency, int userId, String appId) {
        super(hostPerParent, prefix);
        this.type = type;
        this.userId = userId;
        this.appId = appId;
        this.upLatency = upLatency;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getType() {
        return type;
    }
}
