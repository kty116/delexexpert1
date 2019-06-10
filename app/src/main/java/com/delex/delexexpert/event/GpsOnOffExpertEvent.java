package com.delex.delexexpert.event;

public class GpsOnOffExpertEvent implements ExpertEvent {
    private boolean isGpsOnOff;

    public GpsOnOffExpertEvent(boolean isGpsOnOff) {
        this.isGpsOnOff = isGpsOnOff;
    }

    public boolean isGpsOnOff() {
        return isGpsOnOff;
    }

    public void setGpsOnOff(boolean gpsOnOff) {
        isGpsOnOff = gpsOnOff;
    }

    @Override
    public String toString() {
        return "GpsOnOffExpertEvent{" +
                "isGpsOnOff=" + isGpsOnOff +
                '}';
    }
}
