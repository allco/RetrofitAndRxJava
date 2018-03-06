# RetrofitAndRxJava

This is the demonstration how to get JSON objects one by one from a JSON array using Retrofit and RxJava.

# Motivation

[Retrofit](https://github.com/square/retrofit) by default **downloads whole available data**,
converts it to java-objects and only then provides the result to the consumer code.
This is not effective way, since **the data can be too big to keep it in memory of mobile phone**. 
It would be super nice to do not keep whole received data in the memory but process it piece by piece 
and return to the consumer code parsed objects one by one as fast as they becoming parsed.

# Solution

1. Annotation `@Streaming` for *retrofit's service interface method* is able to switch off the inner caching of retrofit.
2. RxJava's `Observable<ResponseBody>` as a return type for *retrofit's service interface method* gives a chance 
to get an access to the HTTP-response body's `InputStream` as fast as the connection established 
and whole the headers received (but the body still not received).
3. It is possible to parse received body's `InputStream` with Gson in semi-automatic mode in order to receiving desired JSON objects ony by one.
4. Parsed JSON object should be delivered to the consumer code as fast as they becoming available (parsed). 

# Implementation

[This](https://github.com/zemirco/sf-city-lots-json/blob/master/citylots.json) huge JSON file (180+Mb) is choosen as a sample source. 

It has structure like:
```
{
"type": "FeatureCollection",
"features": [
{ "type": "Feature", "properties": { "MAPBLKLOT": "0001001", "BLKLOT": "0001001", "BLOCK_NUM": "0001", "LOT_NUM": "001", "FROM_ST": "0", "TO_ST": "0", "STREET": "UNKNOWN", "ST_TYPE": null, "ODD_EVEN": "E" }, "geometry": { "type": "Polygon", "coordinates": [ [ [ -122.422003528252475, 37.808480096967251, 0.0 ], [ -122.422076013325281, 37.808835019815085, 0.0 ], [ -122.421102174348633, 37.808803534992904, 0.0 ], [ -122.421062569067274, 37.808601056818148, 0.0 ], [ -122.422003528252475, 37.808480096967251, 0.0 ] ] ] } }
,
{ "type": "Feature", "properties": { "MAPBLKLOT": "0002001", "BLKLOT": "0002001", "BLOCK_NUM": "0002", "LOT_NUM": "001", "FROM_ST": "0", "TO_ST": "0", "STREET": "UNKNOWN", "ST_TYPE": null, "ODD_EVEN": "E" }, "geometry": { "type": "Polygon", "coordinates": [ [ [ -122.42082593937107, 37.808631474146033, 0.0 ], [ -122.420858049679694, 37.808795641369592, 0.0 ], [ -122.419811958704301, 37.808761809714007, 0.0 ], [ -122.42082593937107, 37.808631474146033, 0.0 ] ] ] } }
,
...
]}
```

The retrofit's service interface declared like that:
```
public interface HugeJsonApi {
    @Streaming
    @GET("/zemirco/sf-city-lots-json/master/citylots.json")
    Observable<ResponseBody> get();
}
```

Then RxJava's operator `concatMap()` turns `ResponseBody` to a stream of parsed JSON objects.

The actual conversation is happens at `MainActivity.convertObjectsStream(...)` method.

The one can ensure with any network sniffer (like [Charles Proxy](https://www.charlesproxy.com/)) that actual parsing is happening on the fly.

# Improvements
It would be nice to have an retrofit's CallAdapter.
