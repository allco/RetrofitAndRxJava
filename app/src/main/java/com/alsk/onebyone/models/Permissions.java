
package com.alsk.onebyone.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Permissions {

    @SerializedName("admin")
    @Expose
    public Boolean admin;
    @SerializedName("push")
    @Expose
    public Boolean push;
    @SerializedName("pull")
    @Expose
    public Boolean pull;

}
