package org.fog.utils.estatisticas;

import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.utils.TimeKeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TupleIdExecutionTime extends  StatisticsCollector<TupleIdExecutionTime.InformacoesTupla> {

    class InformacoesTupla{
        public String appId;
        public String module;
        public Integer cloudletId;
        public Integer associatedModuleInstance;
        public Integer userId;
        public Long cloudletLenght;
        public double startTime;
        public double endTime;
        public double cpuTime;
        public String deviceName;
        public String tupleType;

        public String getTupleType() {
            return tupleType;
        }

        public String getModule() {
            return module;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public Integer getCloudletId() {
            return cloudletId;
        }

        public void setCloudletId(Integer cloudletId) {
            this.cloudletId = cloudletId;
        }

        public Integer getAssociatedModuleInstance() {
            return associatedModuleInstance;
        }

        public void setAssociatedModuleInstance(Integer associatedModuleInstance) {
            this.associatedModuleInstance = associatedModuleInstance;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Long getCloudletLenght() {
            return cloudletLenght;
        }

        public void setCloudletLenght(Long cloudletLenght) {
            this.cloudletLenght = cloudletLenght;
        }

        public double getStartTime() {
            return startTime;
        }

        public void setStartTime(double startTime) {
            this.startTime = startTime;
        }

        public double getEndTime() {
            return endTime;
        }

        public void setEndTime(double endTime) {
            this.endTime = endTime;
        }

        public double getCpuTime() {
            return cpuTime;
        }

        public void setCpuTime(double cpuTime) {
            this.cpuTime = cpuTime;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }
    }

    public TupleIdExecutionTime(Controller controller, String filePrefix) {
        super(controller,filePrefix);
    }

    @Override
    public void computeStatistics() {

    }

    @Override
    public void printMyStatistics() {
        InformacoesTupla[] tuplas = TimeKeeper.getInstance().tuplas.values()
                .stream()
                .map(tuple -> {
                    InformacoesTupla info = new InformacoesTupla();

                    info.appId = tuple.getAppId();
                    info.module = tuple.getDestModuleName();
                    info.cloudletId = tuple.getCloudletId();
                    info.associatedModuleInstance = tuple.getAssociatedModuleInstance();
                    info.userId = tuple.getUserId();
                    info.cloudletLenght = tuple.getCloudletLength();
                    info.startTime = tuple.getExecStartTime();
                    info.endTime = tuple.getFinishTime();
                    info.cpuTime  = tuple.getActualCPUTime();
                    info.deviceName = tuple.getResourceName(tuple.getResourceId());
                    info.tupleType = tuple.getTupleType();

                    return info;
                })
                .collect(Collectors.toList())
                .toArray(new InformacoesTupla[0]);


        this.printMyStatistics(
                tuplas, "tupleExecution.csv");
    }
}
