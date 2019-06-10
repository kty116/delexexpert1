package com.delex.delexexpert.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MqttLocationModel {
    private String clientId;
    private String carNum;
    private boolean isWorking;
    private double latitude;
    private double longitude;

}
