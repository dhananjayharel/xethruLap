package com.programmr.pneumoapp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dj on 9/23/2016.
 */
public class ResultModel {

    public String name;
    public String age;
    public long dateTime;
    public int maxBreathCount=0;
    public int minBreathCount=0;
    public double avgBreathCount=0;

    public ResultModel(String name,String age,Long dateTime){
        this.name=name;
        this.age=age;
        this.dateTime=dateTime;

    }

    public ResultModel(String name,String age,long dateTime,int meanBreath,int maxBreath,double avgBreath){
        this.name=name;
        this.age=age;
        this.dateTime=dateTime;
        this.minBreathCount=meanBreath;
        this.maxBreathCount=maxBreath;
        this.avgBreathCount=avgBreath;

    }
    public String getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public long getDateTime() {
        return dateTime;
    }

    public int getMinBreathCount() {
        return minBreathCount;
    }

    public int getMaxBreathCount() {
        return maxBreathCount;
    }

    public double getAvgBreathCount() {
        return avgBreathCount;
    }

    public static String getJsonStringOfObject(ResultModel obj){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", obj.getName());
            jsonObject.put("age", obj.getAge());
            jsonObject.put("minBreathCount", obj.getMinBreathCount());
            jsonObject.put("maxBreathCount",obj.getMaxBreathCount());
            jsonObject.put("avgBreathCount",obj.getAvgBreathCount());
            jsonObject.put("dateTime",obj.getDateTime());
            return jsonObject.toString();
        }
        catch (Exception e){

        }
    return "";
    }

    public static ResultModel getObjectFromJson(String json){
        try {
        JSONObject jsonObject = new JSONObject(json);
        String name=jsonObject.get("name").toString();
        String age = jsonObject.get("age").toString();
        int minBreathCount =  Integer.parseInt(jsonObject.get("minBreathCount").toString());
        int maxBreathCount =  Integer.parseInt(jsonObject.get("maxBreathCount").toString());
        int avgBreathCount =  Integer.parseInt(jsonObject.get("avgBreathCount").toString());
        long dateTime =   Long.parseLong(jsonObject.get("dateTime").toString());
     return new ResultModel(name,age,dateTime,minBreathCount,maxBreathCount,avgBreathCount);

        } catch (JSONException e) {
            e.printStackTrace();
        }
     return null;
    }
}
