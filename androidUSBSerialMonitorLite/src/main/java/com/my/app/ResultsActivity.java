package com.my.app;

import android.content.Intent;
import android.os.Bundle;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;

import java.util.ArrayList;
import java.util.List;


public class ResultsActivity extends Activity {
    ListView list;
    private List<ResultModel> results = new ArrayList<ResultModel>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        results.add( new ResultModel("sam","3","11 June 2013 10.00am"));
        results.add( new ResultModel("sam","6","11 Aug 2013 8.00am"));
        results.add( new ResultModel("sam","11","5 Jan 2014 10.00am"));
        results.add( new ResultModel("sam","12","8 July 2014 2.00pm"));
        results.add( new ResultModel("sam","15","21 Oct 2014 10.00am"));
        results.add( new ResultModel("sam","17","11 Dec 2014 10.00am"));
        results.add( new ResultModel("sam","17","11 Dec 2014 10.00pm"));
        results.add( new ResultModel("sam","18","11 Jan 2015 10.00am"));
        results.add( new ResultModel("sam","20","11 Mar 2015 180.00am"));
        results.add( new ResultModel("sam","21","11 May 2015 10.00am"));
        results.add( new ResultModel("sam","22","11 July 2015 11.00am"));
        results.add( new ResultModel("sam","26","11 Nov 2015 7.00am"));
        results.add( new ResultModel("sam","26","11 Nov 2015 10.00am"));
        results.add( new ResultModel("sam","34","11 March 2016 10.00am"));
        results.add( new ResultModel("sam","40","11 July 2016 10.00am"));
        results.add( new ResultModel("sam","41","11 Aug 2016 10.00am"));
        results.add( new ResultModel("sam","41","11 Aug 2016 11.00am"));
        results.add( new ResultModel("sam","41","11 Aug 2016 12.00pm"));
        results.add( new ResultModel("sam","41","11 Aug 2016 1.00pm"));
        results.add( new ResultModel("sam","41","11 Aug 2016 7.00pm"));

        Results adapter = new Results(ResultsActivity.this, R.layout.list_single,results);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ResultModel rs= results.get(position);
                //Toast.makeText(ResultsActivity.this, "You Clicked at " +rs.age, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(view.getContext(),ResultDetailsActivity.class);
                intent.putExtra("Name",rs.getName());
                intent.putExtra("Age",rs.getAge());
                startActivity(intent);

            }
        });




    }

}
