package org.fog.utils.estatisticas;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.placement.Controller;
import org.fog.utils.Config;
import org.fog.utils.TimeKeeper;

import java.util.ArrayList;
import java.util.List;

public class AverageLoopDelay extends  StatisticsCollector<AverageLoopDelay.LoopDelayRow> {

    List<LoopDelayRow> rows = new ArrayList<>();

    public class LoopDelayRow {
        double time;
        String application;
        double delay;
        int loop;
        String loopString;

        public double getTime() {
            return time;
        }

        public String getApplication() {
            return application;
        }

        public double getDelay() {
            return delay;
        }

        public int getLoop() {
            return loop;
        }

        public String getLoopString() {
            return loopString;
        }
    }

    public AverageLoopDelay(Controller controller, String filePrefix) {
        super(controller,filePrefix);
    }

    @Override
    public void computeStatistics() {
        double time = CloudSim.clock();
        if(onlyFinal && ! (time== Config.MAX_SIMULATION_TIME-1)) return;
        for(Application app : controller.getApplications().values()){
            for(AppLoop loop : app.getLoops()){
                LoopDelayRow row = new LoopDelayRow();

                row.application = app.getAppId();
                row.time = time;
                row.loop = loop.getLoopId();

                if(TimeKeeper
                        .getInstance()
                        .getLoopIdToCurrentAverage().containsKey(row.loop))
                    row.delay = TimeKeeper
                            .getInstance()
                            .getLoopIdToCurrentAverage()
                            .get(row.loop);

                row.loopString = controller.getStringForLoopId(row.loop);

                rows.add(row);
            }
        }

    }

    @Override
    public void printMyStatistics() {
        this.printMyStatistics(rows.toArray(new LoopDelayRow[0]), "loopsDelays.csv");
    }
}
