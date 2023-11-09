package org.fog.utils.estatisticas;

import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.fog.entities.FogDevice;
import org.fog.placement.Controller;
import org.fog.utils.TimeKeeper;

import java.util.ArrayList;
import java.util.List;

public class DeviceCPUUsage extends  StatisticsCollector<TimeKeeper.TupleExecution> {

    List<TimeKeeper.TupleExecution> rows = new ArrayList<>();

    public DeviceCPUUsage(Controller controller, String filePrefix) {
        super(controller,filePrefix);
    }

    @Override
    public void computeStatistics() {

    }

    class StateHistory extends HostStateHistoryEntry {
        private String device;

        public StateHistory(double time, double allocatedMips, double requestedMips, boolean isActive) {
            super(time, allocatedMips, requestedMips, isActive);
        }
    }

    @Override
    public void printMyStatistics() {
        for(FogDevice device: controller.getFogDevices()){
            //device.getHost().getStateHistory().
        }


        //this.printMyStatistics(TimeKeeper.getInstance().tupleExecutions.toArray(new TimeKeeper.TupleExecution[0]), "stateStory.csv");
    }
}
