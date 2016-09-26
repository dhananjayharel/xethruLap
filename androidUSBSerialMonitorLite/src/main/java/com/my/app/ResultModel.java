package com.my.app;

import java.text.DateFormat;

/**
 * Created by dj on 9/23/2016.
 */
public class ResultModel {

    public String name;
    public String age;
    public String dateTime;

    public ResultModel(String name,String age,String dateTime){
        this.name=name;
        this.age=age;
        this.dateTime=dateTime;
    }
    public String getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getDateTime() {
        return dateTime;
    }
}
