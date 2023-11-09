package org.fog.utils.estatisticas;

import org.fog.placement.Controller;
import org.fog.utils.TimeKeeper;

import java.util.ArrayList;
import java.util.List;

public class TupleIdDelay extends  StatisticsCollector<TupleIdDelay.TupleIdDelayRow> {

    List<TupleIdDelayRow> rows = new ArrayList<>();

    public class TupleIdDelayRow {
        double time;
        String application;
        double delay;
        int loop;
        String loopString;
        int tupleId;

        public double getTime() {
            return time;
        }

        public void setTime(double time) {
            this.time = time;
        }

        public String getApplication() {
            return application;
        }

        public void setApplication(String application) {
            this.application = application;
        }

        public double getDelay() {
            return delay;
        }

        public void setDelay(double delay) {
            this.delay = delay;
        }

        public int getLoop() {
            return loop;
        }

        public void setLoop(int loop) {
            this.loop = loop;
        }

        public String getLoopString() {
            return loopString;
        }

        public void setLoopString(String loopString) {
            this.loopString = loopString;
        }

        public int getTupleId() {
            return tupleId;
        }

        public void setTupleId(int tupleId) {
            this.tupleId = tupleId;
        }
    }

    public TupleIdDelay(Controller controller, String filePrefix) {
        super(controller,filePrefix);
    }

    @Override
    public void computeStatistics() {

    }

    @Override
    public void printMyStatistics() {
//        for(TimeKeeper.LoopDelay loopDelay: TimeKeeper.getInstance().getTupleTotalTime()){
//            TupleIdDelayRow tupleRow = new TupleIdDelayRow();
//
//            tupleRow.setTime(loopDelay.getTime());
//            tupleRow.setTupleId(loopDelay.getTupleId());
//            tupleRow.setDelay(loopDelay.getDelay());
//            tupleRow.setLoop(loopDelay.getLoopId());
//            tupleRow.setApplication(loopDelay.getAppId());
//            tupleRow.setLoopString(controller.getStringForLoopId(loopDelay.getLoopId()));
//
//
//            rows.add(tupleRow);
//        }
//
//
//        this.printMyStatistics(rows.toArray(new TupleIdDelayRow[0]), "tupleDelay.csv");
    }
}
