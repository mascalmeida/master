package org.fog.test.my.topology;

import org.fog.test.my.utils.Level;

import java.util.HashMap;
import java.util.Map;

public abstract class Topology {
    private Level top;
    private Level botton;
    private String name;
    private int edgeLatency;

    public Topology(String name, int edgeLatency) {
        this.name = name;
        this.edgeLatency = edgeLatency;
    }

    public Map<Integer, Integer> dist(Integer ... configs){
        Map<Integer, Integer> distConfig = new HashMap<>();
        for (int i = 0; i < configs.length; i+=2) {
            distConfig.put(configs[i], configs[i+1]);
        }
        return distConfig;
    }

    public Level getTop() {
        return top;
    }

    public void setTop(Level top) {
        this.top = top;
    }

    public Level getBotton() {
        return botton;
    }

    public void setBottom(Level botton) {
        this.botton = botton;
    }

    public String getName() {
        return name;
    }

    public int getEdgeLatency() {
        return edgeLatency;
    }
}
