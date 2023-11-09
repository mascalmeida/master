package org.fog.test.my.utils;

import java.util.*;

public class Level {
    private Integer hostPerParent;
    private HostConfig hostConfig;
    private String prefix;
    protected double upLatency;
    private List<Level> childrens = new ArrayList<>();
    //<Host in Level, Quantidade>
    private Map<Integer, Integer> childrensDists = null;

    public Map<String, Integer> modulesCount;
    public Map<String, Integer> modulesCountUp;

    public Level(int hostPerParent, HostConfig hostConfig, String prefix, int upLatency) {
        this(hostPerParent, null,hostConfig,prefix,upLatency);
    }

    public Level(Map<Integer, Integer> childrensDists, HostConfig hostConfig, String prefix, int upLatency) {
        this(null, childrensDists,hostConfig,prefix,upLatency);
    }

    private Level(Integer hostPerParent, Map<Integer, Integer> childrensDists, HostConfig hostConfig, String prefix, int upLatency){
        this.modulesCount = new HashMap<>();
        this.modulesCountUp = new HashMap<>();
        this.hostPerParent = hostPerParent;
        this.childrensDists = childrensDists;
        this.hostConfig = hostConfig;
        this.prefix = prefix;
        this.upLatency = upLatency;
    }

    public Level(int hostPerParent, String prefix) {
        this.hostPerParent = hostPerParent;
        this.prefix = prefix;
    }

    public Level addNext(Level level){
        this.childrens = Arrays.asList(level);
        return level;
    }

    public Level addGroup(Level... levels){
        this.childrens = Arrays.asList(levels);
        return this;
    }

    public Level addEdge(Level level){
        this.getChildrens().add(level);
        return this;
    }

    public Integer getHostPerParent() {
        return hostPerParent;
    }

    public HostConfig getHostConfig() {
        return hostConfig;
    }

    public String getPrefix() {
        return prefix;
    }

    public double getUpLatency() {
        return upLatency;
    }

    public List<Level> getChildrens() {
        return childrens;
    }

    public Map<Integer, Integer> getChildrensDists() {
        return childrensDists;
    }


    @Override
    public String toString() {
        return "Level{" +
                "class='" + this.getClass().getName() + "\'" +
                "prefix='" + prefix + '\'' +
                '}';
    }

    public Level addAppModules(String moduleName, int count) {
        this.modulesCount.put(moduleName, count);
        return this;
    }

    public Level addAppModulesUp(String moduleName, int count) {
        this.modulesCountUp.put(moduleName, count);
        return this;
    }

    public Map<String, Integer> getModulesCount() {
        return modulesCount;
    }

    public Map<String, Integer> getModulesCountUp() {
        return modulesCountUp;
    }

    public boolean hasModules(){
        return getModulesCount().size()>0;
    }

    public boolean hasModulesToPlaceUp(){
        return getModulesCountUp().size()>0;
    }
}
