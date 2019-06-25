package com.delex.delexexpert.firebase;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StateModel {
    private String lastLocationTime;
    private String appVersion;
    private String lastLocation;
    private boolean isNetworkState;
    private boolean isGpsState;
    private boolean isWork;
    private boolean isMqttConnection;
    private boolean isServiceState;
    private String error;
    private String errorTime;


}
