package com.smartgeeks.busticket.Api;

public class Service {

    public static final String TAG = "Test" ;

    public static final String URL = "http://testing.smartgeeks.com.co/busticket/" ;

    public static ApiService getApiService(){
        return RetrofitClient.getClient(URL).create(ApiService.class);
    }

}
