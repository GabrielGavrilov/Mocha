package com.gabrielgavrilov.mocha;

import com.google.gson.JsonObject;

import java.util.HashMap;

public class MochaRequest<T>
{
    public MochaPayload<T> payload;
    public HashMap<String, String> parameter = new HashMap<>();
    public HashMap<String, String> cookie = new HashMap<>();
    public String header = "";

//    public String get(String value)
//    {
//        if(payload instanceof HashMap<?,?>)
//            return ((HashMap<String, String>)payload).get(value);
//
//        else if(payload instanceof JsonObject)
//            return ((JsonObject)payload).get(value).getAsString();
//
//        return null;
//    }
}
