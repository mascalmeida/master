package org.fog.test.my.utils;

public class HostConfig{
    private int mips;
    private int ram;
    private int upBw;
    private int downBw;
    private double rate;

    public HostConfig(int mips, int ram, int upBw, int downBw, double rate) {
        this.mips = mips;
        this.ram = ram;
        this.upBw = upBw;
        this.downBw = downBw;
        this.rate = rate;
    }

    public int getMips() {
        return mips;
    }

    public int getRam() {
        return ram;
    }

    public int getUpBw() {
        return upBw;
    }

    public int getDownBw() {
        return downBw;
    }

    public double getRate() {
        return rate;
    }
}
