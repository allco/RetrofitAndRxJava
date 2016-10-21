
package com.alsk.onebyone.hugejsonservice.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Geometry {

    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("coordinates")
    @Expose
    public List<List<List<Double>>> coordinates = new ArrayList<List<List<Double>>>();

}
