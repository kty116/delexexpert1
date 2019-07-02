package com.delex.delexexpert.firebase;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StateModel {
    private String modelName;
    private int apiLevel;
    private String androidVersion;
    private String carNumber;
    private String userId;
    private String lastLocationTime;
    private String appVersion;
    private double lat;
    private double lon;
    private boolean isGpsState;
    private boolean isWork;
    private boolean isMqttConnection;
    private boolean isServiceState;
    private String error;
    private String errorTime;
    private String text;


}
