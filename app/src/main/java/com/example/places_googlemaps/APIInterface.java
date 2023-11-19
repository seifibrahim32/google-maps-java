package com.example.places_googlemaps;

import com.example.places_googlemaps.pojo.Response;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("api/geocode/json")
    Call<Response> getAPIResponse(@Query("latlng") String latlng,
                                  @Query("key") String key);
}
