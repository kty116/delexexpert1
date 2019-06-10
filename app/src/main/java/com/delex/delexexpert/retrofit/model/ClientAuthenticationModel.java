package com.delex.delexexpert.retrofit.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ClientAuthenticationModel {

    private String access_token;
    private String token_type;
    private String device_token;
    private String refresh_token;
    private String expires_in;
    private String scope;
    private String jti;

}
