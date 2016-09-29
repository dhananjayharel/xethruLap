package com.my.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class ResultsActivity extends Activity {
    ListView list;
    private List<ResultModel> results = new ArrayList<ResultModel>();
    private SharedPreferences mPrefs;
    private Editor prefsEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Results adapter = new Results(ResultsActivity.this, R.layout.list_single,results);
        adapter.clear();
        adapter.notifyDataSetChanged();
         mPrefs  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // mPrefs.edit().remove("ResultsHistory").commit();

        String newResultModelEntry = mPrefs.getString("newEntry","");
        Set<String> set = mPrefs.getStringSet("ResultsHistory", null);

        if(set!=null){
            for (String s : set) {
                ResultModel rm = ResultModel.getObjectFromJson(s);
                results.add(rm);
            }
        }
        /*
        try {
            if (newResultModelEntry.length() > 1) {
                ResultModel rm = ResultModel.getObjectFromJson(newResultModelEntry);
                results.add(rm);
            }
        }
        catch (Exception e){

        }
        */

       // results.add( new ResultModel("Lucas","3",new Date().getTime(),11,22,11.4));
       // results.add( new ResultModel("Oliver","6",new Date().getTime(),11,22,11.4));
      //  results.add( new ResultModel("Mason","11",new Date().getTime(),14,43,11.4));
      //  results.add( new ResultModel("Aiden","12",new Date().getTime(),11,22,11.4));
        //results.add( new ResultModel("Aiden","15","21 Oct 2014 10.00am",22,43,11.4));
        //results.add( new ResultModel("Aiden","17","11 Dec 2014 10.00am",31,42,28));
       // results.add( new ResultModel("Emma","17","11 Dec 2014 10.00pm",17,22,11.4));
        /*
        results.add( new ResultModel("Emma","18","11 Jan 2015 10.00am",14,22,11.4));
        results.add( new ResultModel("Harper","20","11 Mar 2015 180.00am",23,55,11.4));
        results.add( new ResultModel("sam","21","11 May 2015 10.00am",11,22,11.4));
        results.add( new ResultModel("sam","22","11 July 2015 11.00am",11,22,11.4));
        results.add( new ResultModel("sam","26","11 Nov 2015 7.00am",11,22,11.4));
        results.add( new ResultModel("sam","26","11 Nov 2015 10.00am",11,44,23.8));
        results.add( new ResultModel("sam","34","11 March 2016 10.00am",16,22,11.4));
        results.add( new ResultModel("sam","40","11 July 2016 10.00am",11,22,11.4));
        results.add( new ResultModel("sam","41","11 Aug 2016 10.00am",15,22,11.4));
        results.add( new ResultModel("sam","41","11 Aug 2016 11.00am",11,22,15.7));
        results.add( new ResultModel("sam","41","11 Aug 2016 12.00pm",11,22,11.4));
        results.add( new ResultModel("sam","41","11 Aug 2016 1.00pm",21,22,21.1));
        results.add( new ResultModel("sam","41","11 Aug 2016 7.00pm",19,34,21.4));
        */

        Collections.sort(results, new Comparator<ResultModel>() {
            public int compare(ResultModel o1, ResultModel o2) {
                return o1.getDateTime()>(o2.getDateTime()) ? -1 : 1;
            }
        });


        list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ResultModel rs= results.get(position);
                //Toast.makeText(ResultsActivity.this, "You Clicked at " +rs.age, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(view.getContext(),ResultDetailsActivity.class);
                intent.putExtra("Name",rs.getName());
                intent.putExtra("Age",rs.getAge());
                intent.putExtra("MinBreathCount",rs.getMinBreathCount());
                intent.putExtra("MaxBreathCount",rs.getMaxBreathCount());
                intent.putExtra("AvgBreathCount",rs.getAvgBreathCount());
                startActivity(intent);

            }
        });




    }

}
