package org.fog.utils.estatisticas;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.FogDevice;
import org.fog.placement.Controller;
import org.fog.utils.Config;
import org.fog.utils.NetworkUsageMonitor;

import java.util.ArrayList;
import java.util.List;

public class Network extends StatisticsCollector<Network.NetworkRow> {
    List<NetworkRow> rows = new ArrayList<>();
    public Network(Controller controller,String filePrefix) {
        super(controller,filePrefix);
    }

    @Override
    public void computeStatistics() {
        double time = CloudSim.clock();
        if(onlyFinal && ! (time== Config.MAX_SIMULATION_TIME-1)) return;
        for(FogDevice fogDevice: controller.getFogDevices()){
            NetworkRow row = new NetworkRow();

            row.time = time;
            row.currentNetwork = NetworkUsageMonitor.getNetworkUsage()/time;

            rows.add(row);
        }
    }

    public void printMyStatistics() {
        this.printMyStatistics(rows.toArray(new NetworkRow[0]), "network.csv");
    }

    public class NetworkRow {
        double time;
        double currentNetwork;

        public double getTime() {
            return time;
        }

        public double getCurrentNetwork() {
            return currentNetwork;
        }
    }
}
