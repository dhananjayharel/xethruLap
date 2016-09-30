package com.programmr.pneumoapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {


    private Button startButton,historyButton;
    private static final int MENU_ID_OPENDEVICE     = 0;
    private static final int MENU_ID_CLOSEDEVICE    = 1;
    private int babyAge=0;
    private String babyName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button)findViewById(R.id.buttonStart);
        historyButton = (Button)findViewById(R.id.buttonHistory);
        historyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // writeDataToSerial()
               Intent testPage = new Intent(v.getContext(), ResultsActivity.class);
                testPage.putExtra("MSG","hi");
              startActivity(testPage);

            }
        });

        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showDialog();
                // writeDataToSerial()


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_OPENDEVICE, Menu.NONE, "Start the Test");
        menu.add(Menu.NONE, MENU_ID_CLOSEDEVICE, Menu.NONE, "Back");
        return super.onCreateOptionsMenu(menu);
    }

    public void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.addContentView(new View(this), (new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)));
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.user_input_dialog);

        dialog.setTitle("Xethru");



        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
        final TextView tvName = (TextView) dialog.findViewById(R.id.inputName);
        tvName.requestFocus();
        final TextView tvAge = (TextView) dialog.findViewById(R.id.inputAge);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //feedMultiple();
                final String age=  (String) tvAge.getText().toString();
                babyAge = Integer.parseInt(age);
                String babyName = (String) tvName.getText().toString();
                dialog.dismiss();
                Intent testPage = new Intent(v.getContext(),PneumoniaTestActivity.class);
                testPage.putExtra("Name",babyName);
                testPage.putExtra("Age",age);
                startActivity(testPage);

            }
        });

        dialog.show();


    }
}
