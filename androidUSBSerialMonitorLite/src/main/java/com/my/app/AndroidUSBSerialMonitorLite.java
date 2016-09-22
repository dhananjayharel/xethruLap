/*
 * Android USB Serial Monitor Lite
 *
 * Copyright (C) 2012 Keisuke SUZUKI
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * thanks to Arun.
 */
package com.my.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import jp.ksksue.driver.serial.FTDriver;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;




public class AndroidUSBSerialMonitorLite extends Activity  implements OnChartValueSelectedListener{
    // debug settings
    private static final boolean SHOW_DEBUG                 = false;
    private static final boolean USE_WRITE_BUTTON_FOR_DEBUG = false;

    public static final boolean isICSorHigher = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2 );

    // occurs USB packet loss if TEXT_MAX_SIZE is over 6000
    private static final int TEXT_MAX_SIZE = 8192;

    private static final int MENU_ID_SETTING        = 0;
    private static final int MENU_ID_CLEARTEXT      = 1;
    private static final int MENU_ID_SENDTOEMAIL    = 2;
    private static final int MENU_ID_OPENDEVICE     = 3;
    private static final int MENU_ID_CLOSEDEVICE    = 4;
    private static final int MENU_ID_WORDLIST       = 5;
    public int ageInMonths=6;
    public String name="Blob";

    private static final int REQUEST_PREFERENCE         = 0;
    private static final int REQUEST_WORD_LIST_ACTIVITY = 1;

    // Defines of Display Settings
    private static final int DISP_CHAR  = 0;
    private static final int DISP_DEC   = 1;
    private static final int DISP_HEX   = 2;

    // Linefeed Code Settings
    private static final int LINEFEED_CODE_CR   = 0;
    private static final int LINEFEED_CODE_CRLF = 1;
    private static final int LINEFEED_CODE_LF   = 2;

    // Load Bundle Key (for view switching)
    private static final String BUNDLEKEY_LOADTEXTVIEW = "bundlekey.LoadTextView";

    FTDriver mSerial;

    private LineChart mChart;

    //private ScrollView mSvText;
    private TextView mTvSerial,mTvMinBreath,mTvMaxBreath,mTvAvgBreath,conclusion,mTvName,mTvAge,mTvtimeRemain,mTvtotalReadings;
    private StringBuilder mText = new StringBuilder();
    private boolean mStop = false;

    String TAG = "AndroidSerialTerminal";

    Handler mHandler = new Handler();
    Handler stopHandler = new Handler();
    AlertDialog.Builder alertDialogBuilder;
    private Button btWrite;
    private TextView etWrite;
    private String currentState="";
    // Default settings
    private int mTextFontSize       = 12;
    private Typeface mTextTypeface  = Typeface.MONOSPACE;
    private int mDisplayType        = DISP_CHAR;
    private int mReadLinefeedCode   = LINEFEED_CODE_LF;
    private int mWriteLinefeedCode  = LINEFEED_CODE_LF;
    private int mBaudrate           = FTDriver.BAUD9600;
    private int mDataBits           = FTDriver.FTDI_SET_DATA_BITS_8;
    private int mParity             = FTDriver.FTDI_SET_DATA_PARITY_NONE;
    private int mStopBits           = FTDriver.FTDI_SET_DATA_STOP_BITS_1;
    private int mFlowControl        = FTDriver.FTDI_SET_FLOW_CTRL_NONE;
    private int mBreak              = FTDriver.FTDI_SET_NOBREAK;
    private String mEmailAddress    = "@gmail.com";

    private boolean mRunningMainLoop = false;
    private Set<Integer> breathCount= new HashSet<Integer>();
    private static final String ACTION_USB_PERMISSION =
            "jp.ksksue.app.terminal.USB_PERMISSION";

    // Linefeed
    private final static String BR = System.getProperty("line.separator");
    private AlertDialog popup;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/* FIXME : How to check that there is a title bar menu or not.
        // Should not set a Window.FEATURE_NO_TITLE on Honeycomb because a user cannot see menu button.
        if(isICSorHigher) {
            if(!getWindow().hasFeature(Window.FEATURE_ACTION_BAR)) {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
        }
*/
        alertDialogBuilder = new AlertDialog.Builder(this);
        setContentView(R.layout.main);

        // mSvText = (ScrollView) findViewById(R.id.svText);
        mTvSerial = (TextView) findViewById(R.id.tvSerial);
        mTvMinBreath = (TextView) findViewById(R.id.minBreath);
        mTvMaxBreath = (TextView) findViewById(R.id.maxBreath);
        mTvAvgBreath = (TextView) findViewById(R.id.avgBreath);
        conclusion = (TextView) findViewById(R.id.conclusion);
        mTvtimeRemain=(TextView) findViewById(R.id.timeRemain);
        mTvtotalReadings=(TextView) findViewById(R.id.totalReadings);
        mTvName = (TextView) findViewById(R.id.name);
        mTvAge = (TextView) findViewById(R.id.age);

        btWrite = (Button) findViewById(R.id.btWrite);
        //dj
        btWrite.setEnabled(true);
        // etWrite = (TextView) findViewById(R.id.etWrite);
        // etWrite.setEnabled(true);
        //etWrite.setHint("CR : \\r, LF : \\n");

        if (SHOW_DEBUG) {
            Log.d(TAG, "New FTDriver");
        }

        // get service
        mSerial = new FTDriver((UsbManager) getSystemService(Context.USB_SERVICE));

        if (SHOW_DEBUG) {
            Log.d(TAG, "New instance : " + mSerial);
        }
        // listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        // load default baud rate
        mBaudrate = loadDefaultBaudrate();


        // for requesting permission
        // setPermissionIntent() before begin()
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        mSerial.setPermissionIntent(permissionIntent);

        if (SHOW_DEBUG) {
            Log.d(TAG, "FTDriver beginning");
        }

        if (mSerial.begin(mBaudrate)) {
            if (SHOW_DEBUG) {
                Log.d(TAG, "FTDriver began");
                Toast.makeText(this, "FTD began", Toast.LENGTH_LONG);
            }
            loadDefaultSettingValues();
            mTvSerial.setTextSize(mTextFontSize);
            mainloop();
        } else {
            if (SHOW_DEBUG) {
                Log.d(TAG, "FTDriver no connection");
            }
            Toast.makeText(this, "no connection", Toast.LENGTH_SHORT).show();
        }


        // ---------------------------------------------------------------------------------------
        // Write Button
        // ---------------------------------------------------------------------------------------
        if (!USE_WRITE_BUTTON_FOR_DEBUG) {
            btWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // writeDataToSerial();
                    showDialog();

                }
            });
        } else {
            // Write test button for debug
            btWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new CountDownTimer(60000,1000) {

                        public void onTick(long millisUntilFinished) {
                            //btWrite.setText(Long.toString(millisUntilFinished / 1000));
                            mTvtimeRemain.setText(Long.toString(millisUntilFinished / 1000));
                        }
                        public void onFinish() {
                            btWrite.setText("Start");
                            closeUsbSerial();
                        }
                    }.start();
                    openUsbSerial();
                    //feedMultiple();

                	/*
                    String strWrite = "";
                    for (int i = 0; i < 3000; ++i) {
                        strWrite = strWrite + " " + Integer.toString(i);
                    }
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "FTDriver Write(" + strWrite.length() + ") : " + strWrite);
                    }
                    mSerial.write(strWrite.getBytes(), strWrite.length());
                    */
                }
            });
        } // end of if(SHOW_WRITE_TEST_BUTTON)



        mChart = (LineChart) findViewById(R.id.chart1);


        // no description text
        mChart.setDescription("Respiration");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);
        mChart.setOnChartValueSelectedListener(this);
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        //Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.LINE);
       // l.setTypeface(tf);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
       // xl.setTypeface(tf);
        xl.setTextColor(Color.WHITE);
        xl.setTextSize(12f);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(70f);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


    }

    private int year = 2015;

    private void addEntry() {
        Log.e("dd","in add entry");
        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {

                set = createSet();
                data.addDataSet(set);
            }

            // add a new x-value first
           // data.addXValue(mMonths[data.getXValCount() % 12] + " "
           //         + (year + data.getXValCount() / 12));
            data.addXValue(set.getEntryCount()+"");
            float en = (float) (Math.random() * 20) + 30f;
            data.addEntry(new Entry(en, set.getEntryCount()), 0);
            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
           // Log.e("chart","notifyDataSetChanged"+set.getEntryCount());
            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(50);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getXValCount() - 60);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.GREEN);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
    BreathCounts getMinandMax(Set<Integer> breathCount) {
        // TODO Auto-generated method stub
        if(breathCount.size()==0)
            return new BreathCounts(0, 0, 0);
        int min=0,max=0,sum=0;
        double avg=0;
        Iterator iter = breathCount.iterator();
        min=(int) Integer.parseInt(iter.next().toString());
        max=min;
        while(iter.hasNext()){
            int val=(int)Integer.parseInt(iter.next().toString());
            if(min>val)
                min=val;
            if(max<val)
                max=val;
        }
        avg=sum/breathCount.size();
        return new BreathCounts(min, max,avg);
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



    public void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.addContentView(new View(this), (new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT)));
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
                ageInMonths = Integer.parseInt(age);
                String name = (String) tvName.getText().toString();
                mTvName.setText(name);
                mTvAge.setText(age);
                dialog.dismiss();
                breathCount.clear();
                new CountDownTimer(180000,1000) {
                    int i=0;
                    public void onTick(long millisUntilFinished) {
                        //btWrite.setText(Long.toString(millisUntilFinished / 1000));
                        mTvtimeRemain.setText("Test will be finishing in "+Long.toString(millisUntilFinished / 1000)+" secs.");
                        mTvSerial.setText(currentState);
                       // addEntry();
                    }
                    public void onFinish() {
                        btWrite.setText("Start");
                        closeUsbSerial();
                        mTvtimeRemain.setText("Finished.");
                        mTvtotalReadings.setText("126");
                        BreathCounts ob=getMinandMax(breathCount);
                        mTvSerial.setText("");
                        mText.setLength(0);
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                popup.dismiss();
                            }
                        });
                        alertDialogBuilder.setMessage("Test is finished.");
                        popup	=   alertDialogBuilder.show();
                        mTvMinBreath.setText(""+ob.getMin());
                        mTvMaxBreath.setText(""+ob.getMax());
                        mTvAvgBreath.setText(""+ob.getAvg());
                        // Toast.makeText(AndroidUSBSerialMonitorLite.this, "ageInMonths="+ageInMonths+"Maxage="+ob.getMax(), Toast.LENGTH_LONG).show();
                        if(hasPnemonia(ob.getMax(), ageInMonths)){
                            conclusion.setText("Pneumonia detected!");
                            conclusion.setTextColor(Color.RED);
                        }
                        else{
                            conclusion.setText("Pneumonia not detected!");
                            conclusion.setTextColor(Color.GREEN);
                        }

                    }

                }.start();
                feedMultiple();
                openUsbSerial();
                btWrite.setText("Stop");
            }
        });

        dialog.show();


    }


    private void writeDataToSerial() {
        String strWrite = etWrite.getText().toString();
        strWrite = changeLinefeedcode(strWrite);
        if (SHOW_DEBUG) {
            Log.d(TAG, "FTDriver Write(" + strWrite.length() + ") : " + strWrite);
        }
        mSerial.write(strWrite.getBytes(), strWrite.length());
    }

    private String changeLinefeedcode(String str) {
        str = str.replace("\\r", "\r");
        str = str.replace("\\n", "\n");
        switch (mWriteLinefeedCode) {
            case LINEFEED_CODE_CR:
                str = str + "\r";
                break;
            case LINEFEED_CODE_CRLF:
                str = str + "\r\n";
                break;
            case LINEFEED_CODE_LF:
                str = str + "\n";
                break;
            default:
        }
        return str;
    }

    public void setWriteTextString(String str)
    {
        etWrite.setText(str);
    }

    // ---------------------------------------------------------------------------------------
    // Menu Button
    // ---------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_OPENDEVICE, Menu.NONE, "Start");
        menu.add(Menu.NONE, MENU_ID_CLOSEDEVICE, Menu.NONE, "Close Device");
/*        if(mSerial!=null) {
            if(mSerial.isConnected()) {
                menu.getItem(MENU_ID_OPENDEVICE).setEnabled(false);
            } else {
                menu.getItem(MENU_ID_CLOSEDEVICE).setEnabled(false);
            }
        }
*/        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_OPENDEVICE:
                openUsbSerial();
                return true;

            case MENU_ID_CLOSEDEVICE:
                closeUsbSerial();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WORD_LIST_ACTIVITY) {
            if(resultCode == RESULT_OK) {
                try {
                    String strWord = data.getStringExtra("word");
                    // etWrite.setText(strWord);
                    // Set a cursor position last
                    //  etWrite.setSelection(etWrite.getText().length());
                } catch(Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == REQUEST_PREFERENCE) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

            String res = pref.getString("display_list", Integer.toString(DISP_CHAR));
            mDisplayType = Integer.valueOf(res);

            res = pref.getString("fontsize_list", Integer.toString(12));
            mTextFontSize = Integer.valueOf(res);
            mTvSerial.setTextSize(mTextFontSize);

            res = pref.getString("typeface_list", Integer.toString(3));
            switch(Integer.valueOf(res)){
                case 0:
                    mTextTypeface = Typeface.DEFAULT;
                    break;
                case 1:
                    mTextTypeface = Typeface.SANS_SERIF;
                    break;
                case 2:
                    mTextTypeface = Typeface.SERIF;
                    break;
                case 3:
                    mTextTypeface = Typeface.MONOSPACE;
                    break;
            }
            mTvSerial.setTypeface(mTextTypeface);


            res = pref.getString("readlinefeedcode_list", Integer.toString(LINEFEED_CODE_CRLF));
            mReadLinefeedCode = Integer.valueOf(res);

            res = pref.getString("writelinefeedcode_list", Integer.toString(LINEFEED_CODE_CRLF));
            mWriteLinefeedCode = Integer.valueOf(res);

            res = pref.getString("email_edittext", "@gmail.com");
            mEmailAddress = res;

            res = pref.getString("databits_list", Integer.toString(FTDriver.FTDI_SET_DATA_BITS_8));
            if (mDataBits != Integer.valueOf(res)) {
                mDataBits = Integer.valueOf(res);
                mSerial.setSerialPropertyDataBit(mDataBits, FTDriver.CH_A);
                mSerial.setSerialPropertyToChip(FTDriver.CH_A);
            }

            int intRes;
            res = pref.getString("parity_list",
                    Integer.toString(FTDriver.FTDI_SET_DATA_PARITY_NONE));
            intRes = Integer.valueOf(res) << 8;
            if (mParity != intRes) {
                mParity = intRes;
                mSerial.setSerialPropertyParity(mParity, FTDriver.CH_A);
                mSerial.setSerialPropertyToChip(FTDriver.CH_A);
            }

            res = pref.getString("stopbits_list",
                    Integer.toString(FTDriver.FTDI_SET_DATA_STOP_BITS_1));
            intRes = Integer.valueOf(res) << 11;
            if (mStopBits != intRes) {
                mStopBits = intRes;
                mSerial.setSerialPropertyStopBits(mStopBits, FTDriver.CH_A);
                mSerial.setSerialPropertyToChip(FTDriver.CH_A);
            }

            res = pref.getString("flowcontrol_list",
                    Integer.toString(FTDriver.FTDI_SET_FLOW_CTRL_NONE));
            intRes = Integer.valueOf(res) << 8;
            if (mFlowControl != intRes) {
                mFlowControl = intRes;
                mSerial.setFlowControl(FTDriver.CH_A, mFlowControl);
            }

            res = pref.getString("break_list", Integer.toString(FTDriver.FTDI_SET_NOBREAK));
            intRes = Integer.valueOf(res) << 14;
            if (mBreak != intRes) {
                mBreak = intRes;
                mSerial.setSerialPropertyBreak(mBreak, FTDriver.CH_A);
                mSerial.setSerialPropertyToChip(FTDriver.CH_A);
            }

            // reset baudrate
            res = pref.getString("baudrate_list", Integer.toString(FTDriver.BAUD9600));
            if (mBaudrate != Integer.valueOf(res)) {
                mBaudrate = Integer.valueOf(res);
                mSerial.setBaudrate(mBaudrate, 0);
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // End of Menu button
    // ---------------------------------------------------------------------------------------

    /**
     * Saves values for view switching
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLEKEY_LOADTEXTVIEW, mTvSerial.getText().toString());
    }

    /**
     * Loads values for view switching
     */

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTvSerial.setText(savedInstanceState.getString(BUNDLEKEY_LOADTEXTVIEW));
    }

    @Override
    public void onDestroy() {
        mSerial.end();
        mStop = true;
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    private void mainloop() {
        mStop = false;
        mRunningMainLoop = true;
        btWrite.setEnabled(true);

        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
        if (SHOW_DEBUG) {
            Log.d(TAG, "start mainloop");
        }
        new Thread(mLoop).start();
    }

    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int len;
            byte[] rbuf = new byte[4096];
            int timer=1;

            boolean secondStarted=true;
            boolean secondFinished=false;
            int prevReading=0;
            int currentCount=0;
            int prevCount=0;
            for (;;) {// this is the main loop for transferring
                if(timer==20){
                    secondFinished=true;
                    secondStarted=false;
                    timer=1;
                    prevCount=currentCount;
                    currentCount++;

                }
                //	else{

                // ////////////////////////////////////////////////////////
                // Read and Display to Terminal
                // ////////////////////////////////////////////////////////

                len = mSerial.read(rbuf);
                rbuf[len] = 0;
                timer++;
                if (len > 0) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Read  Length : " + len);
                    }
                    mDisplayType=DISP_HEX;
                    switch (mDisplayType) {
                        case DISP_CHAR:
                            setSerialDataToTextView(mDisplayType, rbuf, len, "", "",currentCount,prevCount);
                            break;
                        case DISP_DEC:
                            setSerialDataToTextView(mDisplayType, rbuf, len, "013", "010",currentCount,prevCount);
                            break;
                        case DISP_HEX:
                            setSerialDataToTextView(mDisplayType, rbuf, len, "0d", "0a",currentCount,prevCount);
                            break;
                    }

                    mHandler.post(new Runnable() {
                        public void run() {
                            if (mTvSerial.length() > TEXT_MAX_SIZE) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(mTvSerial.getText());
                                sb.delete(0, TEXT_MAX_SIZE / 2);
                                mTvSerial.setText(sb);
                            }
                            //mTvSerial.append(mText);

                            mText.setLength(0);
                            // mSvText.fullScroll(ScrollView.FOCUS_DOWN);

                        }
                    });
                }

                //   }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mStop) {
                    mRunningMainLoop = false;
                    return;
                }
            }
        }
    };

    private String IntToHex2(int Value) {
        char HEX2[] = {
                Character.forDigit((Value >> 4) & 0x0F, 16),
                Character.forDigit(Value & 0x0F, 16)
        };
        String Hex2Str = new String(HEX2);
        return Hex2Str;
    }

    boolean lastDataIs0x0D = false;

    void setSerialDataToTextView(int disp, byte[] rbuf, int len, String sCr, String sLf,int currentCount,int prevCount) {
        int tmpbuf;
        for (int i = 0; i < len; ++i) {
            if (SHOW_DEBUG) {
                Log.e(TAG, "Read  Data[" + i + "] : " + rbuf[i]);
            }

            // "\r":CR(0x0D) "\n":LF(0x0A)
            if ((mReadLinefeedCode == LINEFEED_CODE_CR) && (rbuf[i] == 0x0D)) {
                mText.append(sCr);
                mText.append(BR);
            } else if ((mReadLinefeedCode == LINEFEED_CODE_LF) && (rbuf[i] == 0x0A)) {
                mText.append(sLf);
                mText.append(BR);
            } else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)
                    && (rbuf[i + 1] == 0x0A)) {
                mText.append(sCr);
                if (disp != DISP_CHAR) {
                    mText.append(" ");
                }
                mText.append(sLf);
                mText.append(BR);
                ++i;
            } else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)) {
                // case of rbuf[last] == 0x0D and rbuf[0] == 0x0A
                mText.append(sCr);
                lastDataIs0x0D = true;
            } else if (lastDataIs0x0D && (rbuf[0] == 0x0A)) {
                if (disp != DISP_CHAR) {
                    mText.append(" ");
                }
                mText.append(sLf);
                mText.append(BR);
                lastDataIs0x0D = false;
            } else if (lastDataIs0x0D && (i != 0)) {
                // only disable flag
                lastDataIs0x0D = false;
                --i;
            } else {
                switch (disp) {
                    case DISP_CHAR:
                        mText.append((char) rbuf[i]);
                        break;
                    case DISP_DEC:
                        tmpbuf = rbuf[i];
                        if (tmpbuf < 0) {
                            tmpbuf += 256;
                        }
                        //mText.append(String.format("%1$03d", tmpbuf));
                        // mText.append(" ");
                        break;
                    case DISP_HEX:
                        String byteData=IntToHex2((int) rbuf[10]);

                        if(byteData.equals("00")){
                            //mText.append((int)rbuf[14]+" rpm");
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    addEntry();
                                }
                            });
                           // addEntry();
                            if(rbuf[14]>0)
                                breathCount.add((int)rbuf[14]);
                        }

                        //mText.append(getStatus(byteData));
                        final String str=(String) getStatus(byteData);
                        mHandler.post(new Runnable() {
                            public void run() {
                                // mTvSerial.setText(str);
                                currentState=str;
                            }
                        });
                        mText.append(" ");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void feedMultiple() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                for(int i = 0; i < 500; i++) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private Object getStatus(String byteData) {
        // TODO Auto-generated method stub
        if(byteData.equals("00"))
            return "Reading...";
        if(byteData.equals("02"))
            return "Reading..";
        if(byteData.equals("04"))
            return "Initializing..";
        if(byteData.equals("01"))
            return "Reading";
        return "Reading";
    }

    void loadDefaultSettingValues() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String res = pref.getString("display_list", Integer.toString(DISP_CHAR));
        mDisplayType = Integer.valueOf(res);

        res = pref.getString("fontsize_list", Integer.toString(12));
        mTextFontSize = Integer.valueOf(res);

        res = pref.getString("typeface_list", Integer.toString(3));
        switch(Integer.valueOf(res)){
            case 0:
                mTextTypeface = Typeface.DEFAULT;
                break;
            case 1:
                mTextTypeface = Typeface.SANS_SERIF;
                break;
            case 2:
                mTextTypeface = Typeface.SERIF;
                break;
            case 3:
                mTextTypeface = Typeface.MONOSPACE;
                break;
        }
        mTvSerial.setTypeface(mTextTypeface);
        // etWrite.setTypeface(mTextTypeface);

        res = pref.getString("readlinefeedcode_list", Integer.toString(LINEFEED_CODE_CRLF));
        mReadLinefeedCode = Integer.valueOf(res);

        res = pref.getString("writelinefeedcode_list", Integer.toString(LINEFEED_CODE_CRLF));
        mWriteLinefeedCode = Integer.valueOf(res);

        res = pref.getString("email_edittext", "harel.dhananjay@gmail.com");
        mEmailAddress = res;

        res = pref.getString("databits_list", Integer.toString(FTDriver.FTDI_SET_DATA_BITS_8));
        mDataBits = Integer.valueOf(res);
        mSerial.setSerialPropertyDataBit(mDataBits, FTDriver.CH_A);

        res = pref.getString("parity_list", Integer.toString(FTDriver.FTDI_SET_DATA_PARITY_NONE));
        mParity = Integer.valueOf(res) << 8; // parity_list's number is 0 to 4
        mSerial.setSerialPropertyParity(mParity, FTDriver.CH_A);

        res = pref.getString("stopbits_list", Integer.toString(FTDriver.FTDI_SET_DATA_STOP_BITS_1));
        mStopBits = Integer.valueOf(res) << 11; // stopbits_list's number is 0 to 2
        mSerial.setSerialPropertyStopBits(mStopBits, FTDriver.CH_A);

        res = pref
                .getString("flowcontrol_list", Integer.toString(FTDriver.FTDI_SET_FLOW_CTRL_NONE));
        mFlowControl = Integer.valueOf(res) << 8;
        mSerial.setFlowControl(FTDriver.CH_A, mFlowControl);

        res = pref.getString("break_list", Integer.toString(FTDriver.FTDI_SET_NOBREAK));
        mBreak = Integer.valueOf(res) << 14;
        mSerial.setSerialPropertyBreak(mBreak, FTDriver.CH_A);

        mSerial.setSerialPropertyToChip(FTDriver.CH_A);
    }

    private void sendTextToEmail() {
        Intent intent =
                new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                        + mEmailAddress));

        intent.putExtra("subject", "Result of " + getString(R.string.app_name));
        intent.putExtra("body", mTvSerial.getText().toString().trim());
        startActivity(intent);
    }

    // Load default baud rate
    int loadDefaultBaudrate() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String res = pref.getString("baudrate_list", Integer.toString(FTDriver.BAUD9600));
        return Integer.valueOf(res);
    }

    private void openUsbSerial() {
        if (!mSerial.isConnected()) {
            if (SHOW_DEBUG) {
                Log.d(TAG, "onNewIntent begin");
            }
            mBaudrate = loadDefaultBaudrate();
            if (!mSerial.begin(mBaudrate)) {
                Toast.makeText(this, "cannot open", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
            }
        }

        if (!mRunningMainLoop) {
            mainloop();
        }



    }

    private void closeUsbSerial() {
        detachedUi();
        mStop = true;
        mSerial.end();
    }

    protected void onNewIntent(Intent intent) {
        if (SHOW_DEBUG) {
            Log.d(TAG, "onNewIntent");
        }

        //openUsbSerial();
    };

    private void detachedUi() {
        // btWrite.setEnabled(false);
        // etWrite.setEnabled(false);
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(AndroidUSBSerialMonitorLite.this, "done..", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // BroadcastReceiver when insert/remove the device USB plug into/from a USB
    // port
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Device attached");
                }
                if (!mSerial.isConnected()) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Device attached begin");
                    }
                    mBaudrate = loadDefaultBaudrate();
                    mSerial.begin(mBaudrate);
                    loadDefaultSettingValues();
                    mTvSerial.setTextSize(mTextFontSize);
                }
                if (!mRunningMainLoop) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Device attached mainloop");
                    }
                    mainloop();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Device detached");
                }
                mStop = true;
                detachedUi();
                mSerial.usbDetached(intent);
                mSerial.end();
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Request permission");
                }
                synchronized (this) {
                    if (!mSerial.isConnected()) {
                        if (SHOW_DEBUG) {
                            Log.d(TAG, "Request permission begin");
                        }
                        mBaudrate = loadDefaultBaudrate();
                        mSerial.begin(mBaudrate);
                        loadDefaultSettingValues();
                        mTvSerial.setTextSize(mTextFontSize);
                    }
                }
                if (!mRunningMainLoop) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Request permission mainloop");
                    }
                    mainloop();
                }
            }
        }
    };

    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight) {
        Log.e("sa","selected");
    }

    @Override
    public void onNothingSelected() {
    Log.e("dd","not slected");
    }
}
