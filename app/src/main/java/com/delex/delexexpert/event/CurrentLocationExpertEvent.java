package com.delex.delexexpert.event;

import android.location.Location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * Created by Administrator on 2018-08-22.
 */

@Getter
@Setter
@AllArgsConstructor
public class CurrentLocationExpertEvent implements ExpertEvent {
    private Location location;

}
