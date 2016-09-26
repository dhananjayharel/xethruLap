package com.my.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {


    private Button startButton;
    private static final int MENU_ID_OPENDEVICE     = 0;
    private static final int MENU_ID_CLOSEDEVICE    = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button)findViewById(R.id.buttonStart);

        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // writeDataToSerial()
               Intent testPage = new Intent(v.getContext(), ResultsActivity.class);
                testPage.putExtra("MSG","hi");
              startActivity(testPage);

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_OPENDEVICE, Menu.NONE, "Start the Test");
        menu.add(Menu.NONE, MENU_ID_CLOSEDEVICE, Menu.NONE, "Back");
/*        if(mSerial!=null) {
            if(mSerial.isConnected()) {
                menu.getItem(MENU_ID_OPENDEVICE).setEnabled(false);
            } else {
                menu.getItem(MENU_ID_CLOSEDEVICE).setEnabled(false);
            }
        }
*/        return super.onCreateOptionsMenu(menu);
    }
}
