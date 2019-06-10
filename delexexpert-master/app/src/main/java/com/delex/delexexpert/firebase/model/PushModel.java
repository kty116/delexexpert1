package com.delex.delexexpert.firebase.model;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PushModel {
    private PushDataModel data;

    @Getter
    @Setter
    @ToString
    public class PushImageModel {
        private String fileName;
        private String path;
        private String fullPath;
        private String size;
        private String is_image;
    }

    @Getter
    @Setter
    @ToString
    public class PushDataModel {
        private String title;
        private String body;
        private ArrayList<PushImageModel> image;
    }
}