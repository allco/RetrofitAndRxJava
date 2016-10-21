
package com.alsk.onebyone.hugejsonservice.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("MAPBLKLOT")
    @Expose
    public String mAPBLKLOT;
    @SerializedName("BLKLOT")
    @Expose
    public String bLKLOT;
    @SerializedName("BLOCK_NUM")
    @Expose
    public String bLOCKNUM;
    @SerializedName("LOT_NUM")
    @Expose
    public String lOTNUM;
    @SerializedName("FROM_ST")
    @Expose
    public String fROMST;
    @SerializedName("TO_ST")
    @Expose
    public String tOST;
    @SerializedName("STREET")
    @Expose
    public String sTREET;
    @SerializedName("ST_TYPE")
    @Expose
    public Object sTTYPE;
    @SerializedName("ODD_EVEN")
    @Expose
    public String oDDEVEN;

}
