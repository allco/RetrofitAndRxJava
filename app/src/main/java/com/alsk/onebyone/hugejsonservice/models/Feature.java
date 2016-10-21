
package com.alsk.onebyone.hugejsonservice.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/*
Json sample:

{
"type": "FeatureCollection",
"features": [
{ "type": "Feature", "properties": { "MAPBLKLOT": "0001001", "BLKLOT": "0001001", "BLOCK_NUM": "0001", "LOT_NUM": "001", "FROM_ST": "0", "TO_ST": "0", "STREET": "UNKNOWN", "ST_TYPE": null, "ODD_EVEN": "E" }, "geometry": { "type": "Polygon", "coordinates": [ [ [ -122.422003528252475, 37.808480096967251, 0.0 ], [ -122.422076013325281, 37.808835019815085, 0.0 ], [ -122.421102174348633, 37.808803534992904, 0.0 ], [ -122.421062569067274, 37.808601056818148, 0.0 ], [ -122.422003528252475, 37.808480096967251, 0.0 ] ] ] } }
,
{ "type": "Feature", "properties": { "MAPBLKLOT": "0002001", "BLKLOT": "0002001", "BLOCK_NUM": "0002", "LOT_NUM": "001", "FROM_ST": "0", "TO_ST": "0", "STREET": "UNKNOWN", "ST_TYPE": null, "ODD_EVEN": "E" }, "geometry": { "type": "Polygon", "coordinates": [ [ [ -122.42082593937107, 37.808631474146033, 0.0 ], [ -122.420858049679694, 37.808795641369592, 0.0 ], [ -122.419811958704301, 37.808761809714007, 0.0 ], [ -122.42082593937107, 37.808631474146033, 0.0 ] ] ] } },
...
]}
 */

public class Feature {

    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("properties")
    @Expose
    public Properties properties;
    @SerializedName("geometry")
    @Expose
    public Geometry geometry;
}
