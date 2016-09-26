package com.my.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
public class Results extends ArrayAdapter<ResultModel>{

    private final Activity context;

    public Results(Activity context,int resource,
                   List<ResultModel> resultItems) {
        super(context, R.layout.list_single, resultItems);
        this.context = context;
       // this.name = items.name;
       // this.age = age;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.name);
        TextView txtDateTime = (TextView) rowView.findViewById(R.id.dateTime);

        TextView txtAge = (TextView) rowView.findViewById(R.id.age);
        ResultModel item= getItem(position);
        txtTitle.setText(item.name);
        txtAge.setText(item.age);
        txtDateTime.setText(item.dateTime);
        return rowView;
    }
}