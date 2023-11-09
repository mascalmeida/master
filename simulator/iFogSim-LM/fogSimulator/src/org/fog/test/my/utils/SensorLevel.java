package org.fog.test.my.utils;

import org.fog.utils.distribution.Distribution;

public class SensorLevel extends Level {
    private String type;
    private Distribution distribution;
    private int userId;
    private String appId;

    public SensorLevel(int hostPerParent, String prefix, String type, Distribution distribution, double upLatency, int userId, String appId) {
        super(hostPerParent, prefix);
        this.type = type;
        this.distribution = distribution;
        this.userId = userId;
        this.appId = appId;
        this.upLatency = upLatency;
    }

    public String getType() {
        return type;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public int getUserId() {
        return userId;
    }

    public String getAppId() {
        return appId;
    }
}
