package com.my.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.ActionBar;
import android.widget.Toast;

public class ResultDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_details);
        Intent intent = getIntent();
        if(intent.getExtras() != null) {
            String id = intent.getStringExtra("Age");
            String name = intent.getStringExtra("Name");
            Toast.makeText(this,"go this"+id,Toast.LENGTH_LONG).show();
        }
    }


}
