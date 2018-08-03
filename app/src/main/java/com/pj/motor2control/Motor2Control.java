package com.pj.motor2control;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.pj.motor2control.R.id.Status2;

public class Motor2Control extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor2_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//
//            }
//        });

        //region Check SMS Permission is activated
        // Check if the application has the necessary permissions else ask for them to the user
        Log.d("Motor2Control", "Check SMS permissions");
        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d("Motor2Control", "SMS Permission is granted");
            } else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS}, 1);
                Log.d("Motor2Control", "SMS Permission is revoked");
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.d("Motor2Control", "SMS Permission is already granted");
        }
        //endregion
        //region Start to Control Buttons
        final Button btnMotor1On  = (Button) findViewById(R.id.btnMotor1On);
        final Button btnMotor2On  = (Button) findViewById(R.id.btnMotor2On);
        final Button btnMotor1Off  = (Button) findViewById(R.id.btnMotor1Off);
        final Button btnMotor2Off  = (Button) findViewById(R.id.btnMotor2Off);
        final Button btnOpenDoor  = (Button) findViewById(R.id.btnOpenDoor);

        btnMotor1On.setOnClickListener(this);
        btnMotor2On.setOnClickListener(this);
        btnMotor1Off.setOnClickListener(this);
        btnMotor2Off.setOnClickListener(this);
        btnOpenDoor.setOnClickListener(this);
        //endregion

        // Set correct Image Icon Status (Running or Stopped)
        RefreshStatusImages();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set correct Image Icon Status (Running or Stopped)
        RefreshStatusImages();
    }

    @Override
    public void onClick(View view){

        final ImageView Status1  = (ImageView) findViewById(R.id.Status1);
        final ImageView Status2  = (ImageView) findViewById(R.id.Status2);


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();


        switch (view.getId() /*to get clicked view id**/) {
            case R.id.btnMotor1On:
                MotorAction(1,1);
                Status1.setImageResource(R.drawable.gear);
                // Store value as shared pref
                editor.putInt(getString(R.string.pref_Motor1_Status), 1);
                editor.commit();
                break;
            case R.id.btnMotor1Off:
                MotorAction(1,0);
                Status1.setImageResource(R.drawable.cancel);
                // Store value as shared pref
                editor.putInt(getString(R.string.pref_Motor1_Status), 0);
                editor.commit();
                break;
            case R.id.btnMotor2On:
                MotorAction(2,1);
                Status2.setImageResource(R.drawable.gear);
                // Store value as shared pref
                editor.putInt(getString(R.string.pref_Motor2_Status), 1);
                editor.commit();
                break;
            case R.id.btnMotor2Off:
                MotorAction(2,0);
                Status2.setImageResource(R.drawable.cancel);
                editor.putInt(getString(R.string.pref_Motor2_Status), 0);
                editor.commit();
                break;

            case R.id.btnOpenDoor:
                OpenDoor();
                //
                break;
            default:
                break;
        }
    }

    private void OpenDoor() {
        final AlertDialog alert =   new AlertDialog.Builder(this)
                .setTitle(getString(R.string.btnOpenDoor))
                .setMessage(getString(R.string.txtConfirmationOpenDoor))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.txtYes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        MotorAction(3,1);
                        //Toast.makeText(Motor2Control.this, "Yaay", Toast.LENGTH_SHORT).show();
                    }})
                .setNegativeButton(R.string.txtNo, null).show();


        new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFinish() {
                // TODO Auto-generated method stub

                alert.dismiss();
            }
        }.start();
    }

    void RefreshStatusImages()
    {
        final ImageView Status1  = (ImageView) findViewById(R.id.Status1);
        final ImageView Status2  = (ImageView) findViewById(R.id.Status2);

        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        int intMotor1_Status = prefs.getInt(getString(R.string.pref_Motor1_Status),0);
        int intMotor2_Status = prefs.getInt(getString(R.string.pref_Motor2_Status),0);

        if(intMotor1_Status == 0)
            Status1.setImageResource(R.drawable.cancel);
        else
            Status1.setImageResource(R.drawable.gear);

        if(intMotor2_Status == 0)
            Status2.setImageResource(R.drawable.cancel);
        else
            Status2.setImageResource(R.drawable.gear);

    }
    public void MotorAction(int MotorNr, int Action)
    {

        SharedPreferences prefs = getDefaultSharedPreferences(this);
        String strPhoneNumber = prefs.getString("pref_TelephoneNr", "6");
        String strSecretKey = prefs.getString("pref_SecretKey", "SecretKey");

        if(strPhoneNumber.length()<3)           // Check if a correct phone has been configured
        {
            String strWarning = getResources().getString(R.string.WarningConfigurePhone);
            Toast.makeText(getApplicationContext(), strWarning, Toast.LENGTH_LONG).show();
        }
        else {

            String strMessage = strSecretKey + "-motor" + MotorNr;
            String strAction = "";

            final TextView txtLog = (TextView) findViewById(R.id.txtLog);
            if (Action == 0) {
                strAction = getResources().getString(R.string.btnOff);
                strMessage += "off";
            } else {
                strAction = getResources().getString(R.string.btnOn);
                strMessage += "on";
            }
            if(MotorNr == 3)
                txtLog.setText( getString(R.string.btnOpenDoor) );          // In the option 3 show in the log: "Open Door"
            else
                txtLog.setText( "Motor " + MotorNr + " " + strAction);      // Show in Log eg: "Motor 1 On"

            // SMS(Variable)Contraseña-motor2off

            sendSMS(strPhoneNumber, strMessage);
        }
    }
    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_motor2_control, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intentSettings = new Intent(this,SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_about:
                Toast.makeText(getApplicationContext(), "Motor2Control created by Patrik Jacobs and Javi Cañizares. 2017 Version 1.1", Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
