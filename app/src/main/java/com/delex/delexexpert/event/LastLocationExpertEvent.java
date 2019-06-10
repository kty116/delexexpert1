package com.delex.delexexpert.event;

import android.location.Location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LastLocationExpertEvent implements ExpertEvent {
    private Location location;

}
