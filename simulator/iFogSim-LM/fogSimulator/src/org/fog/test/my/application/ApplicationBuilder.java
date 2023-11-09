package org.fog.test.my.application;

import org.fog.application.Application;
import org.fog.entities.FogBroker;
import org.fog.test.my.topology.Topology;

import java.util.HashMap;
import java.util.Map;

public abstract class ApplicationBuilder {

    protected String appId;
    protected FogBroker fogBroker;
    protected Topology topology;
    protected Application application;
    protected static int SEED = 800;
    protected String placementStrategy;
    protected int delay;

    public ApplicationBuilder(String appId, Topology topology, String placementStrategy, int delay) {
        this.appId = appId;
        this.topology = topology;
        this.placementStrategy = placementStrategy;
        this.delay = delay;
    }

    public static void setSeed(int seed) {
        ApplicationBuilder.SEED = seed;
    }

    public Application getApplication(){
        return application;
    }
    protected abstract Application createApplication();
    protected abstract void createEdgeDevices();

    public int getDelay() {
        return delay;
    }

    public Map<Integer, Integer> dist(Integer ... configs){
        Map<Integer, Integer> distConfig = new HashMap<>();
        for (int i = 0; i < configs.length; i+=2) {
            distConfig.put(configs[i], configs[i+1]);
        }
        return distConfig;
    }
}
