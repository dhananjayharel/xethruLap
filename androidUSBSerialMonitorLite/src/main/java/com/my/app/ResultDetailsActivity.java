package com.my.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ResultDetailsActivity extends Activity {

    private TextView tvName,tvAge,tvMinBreath,tvMaxBreath,tvAvgBreath,tvConclusion;
    private String babyAge,babyName,minBreathCount,maxBreathCount,avgBreathCount,conclusion;
    private Button emailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_details);
        tvName=(TextView)findViewById(R.id.name);
        tvAge=(TextView)findViewById(R.id.age) ;
        tvMinBreath = (TextView) findViewById(R.id.minBreath);
        tvMaxBreath = (TextView) findViewById(R.id.maxBreath);
        tvAvgBreath = (TextView) findViewById(R.id.avgBreath);
        tvConclusion = (TextView) findViewById(R.id.conclusion);
        emailButton = (Button) findViewById(R.id.sendEmail);
        Intent intent = getIntent();
        if(intent.getExtras() != null) {
            String age = intent.getStringExtra("Age");
            String name = intent.getStringExtra("Name");
            int minBreath=intent.getIntExtra("MinBreathCount",0);
            int maxBreath=intent.getIntExtra("MaxBreathCount",0);
            double avgBreath=intent.getDoubleExtra("AvgBreathCount",0.0);
            if(hasPnemonia(maxBreath,Integer.parseInt(age))){
                tvConclusion.setText("Pneumonia detected!");
                conclusion="Pneumonia detected!";
                tvConclusion.setTextColor(Color.RED);
            }
            else{
                tvConclusion.setText("Pneumonia not detected!");
                tvConclusion.setTextColor(Color.GREEN);
                conclusion="Pneumonia not detected!";
            }

            tvName.setText(name);
            tvAge.setText(age);
            tvMinBreath.setText(minBreath+"");
            tvMaxBreath.setText(maxBreath+"");
            tvAvgBreath.setText(avgBreath+"");

            babyName=name;
            babyAge=age;
            minBreathCount=minBreath+"";
            maxBreathCount=maxBreath+"";
            avgBreathCount=avgBreath+"";
            conclusion="Pneumonia not detected!";

           // Toast.makeText(this,"go this"+id,Toast.LENGTH_LONG).show();
        }

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextToEmail("dj@programmr.com");
            }
        });
    }


    public boolean hasPnemonia(int breathsPerMin,int age){

        if(age<=2 && breathsPerMin>60)
            return true;
        else
        if(age>2 && age <=11 && breathsPerMin>50)
            return true;
        else
        if(age>11 && age <=59 && breathsPerMin>40)
            return true;
        else
            return false;
    }

    private void sendTextToEmail(String emailId) {
        Intent intent =
                new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                        + emailId));

        intent.putExtra("subject", "Result of " + getString(R.string.app_name));
        intent.putExtra("body",getData());
        startActivity(intent);
    }

   private String getData(){
       String str="";
       str+="--------------------------- Result -------------------------------\r\n";
       str+="Name:"+babyName+"\r\n";
       str+="Age:"+babyAge+"\r\n";
       str+="Minimum breathing count observed :"+minBreathCount+"\r\n";
       str+="Maximum breathing count observed :"+maxBreathCount+"\r\n";
       str+="Average breathing count :"+minBreathCount+"\r\n";
       str+="Conclusion:"+conclusion+"\r\n";

       return str;
   }
}
