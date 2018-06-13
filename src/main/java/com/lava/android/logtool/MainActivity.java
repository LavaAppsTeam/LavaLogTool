package com.lava.android.logtool;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

import static com.lava.android.logtool.Constants.PERMISSION.REQUEST_ID_READ_PERMISSION;
import static com.lava.android.logtool.Constants.PERMISSION.REQUEST_ID_WRITE_PERMISSION;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "LAVA";
    private static final int notificationId = 43526;
    EditText input;
    Button btn;
    TextView out;
    String command;

    Button dumpBtn;
    Button notiBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = (EditText)findViewById(R.id.txt);
        out = (TextView)findViewById(R.id.out);

        btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                ShellExecuter exe = new ShellExecuter();
                command = input.getText().toString();

                String outp = exe.Executer(command);
                out.setText(outp);
                Log.d("Output", outp);
            }
        });

        dumpBtn = (Button) findViewById(R.id.dumpBtn);
        dumpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        submit();
                    }
                }).start();
            }
        });

        notiBtn = (Button) findViewById(R.id.notiBtn);
        notiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                startService(startIntent);
                //createNotificationChannel();
                //createNotification();
            }
        });
    }

    private void submit() {
        try {
            Log.d("submit","dump all started");
            String[] commands = {"dumpsys > /sdcard/dumpsys.txt",
                    "dumpstate > /sdcard/dumpstate.zip" };
            Process p = Runtime.getRuntime().exec("/system/bin/sh -");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : commands) {
                os.writeBytes(tmpCmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
            Log.d("submit","dump all finished");
        } catch (IOException e) {
            Log.d("submit","dump IOException occurred");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("submit","dump Exception occurred");
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("LAVA", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "LAVA")
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentTitle("Lava Logging Service2")
                .setContentText("Click to start taking dump2")
                .setOngoing(true)
                .setPriority(NotificationCompat.FLAG_NO_CLEAR)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);


        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        askPermissionAndWriteFile();
    }

    private void askPermissionAndWriteFile() {
        boolean canWrite = this.askPermission(REQUEST_ID_WRITE_PERMISSION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

    // With Android Level >= 23, you have to ask the user
    // for permission with device (For example read/write data on the device).
    private boolean askPermission(int requestId, String permissionName) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            // Check if we have permission
            int permission = ActivityCompat.checkSelfPermission(this, permissionName);


            if (permission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{permissionName},
                        requestId
                );
                return false;
            }
        }
        return true;


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        // Note: If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            switch (requestCode) {
                case REQUEST_ID_READ_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "Permission read!", Toast.LENGTH_SHORT).show();
                    }
                }
                case REQUEST_ID_WRITE_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "Permission write!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Permission Cancelled!", Toast.LENGTH_SHORT).show();
        }
    }
}