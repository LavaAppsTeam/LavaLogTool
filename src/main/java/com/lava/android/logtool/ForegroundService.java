package com.lava.android.logtool;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ForegroundService extends Service {
    private static final String LOG_TAG = "ForegroundService";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Received onStartCommand 0 "+intent);
        Log.i(LOG_TAG, "Received onStartCommand 1"+flags);
        Log.i(LOG_TAG, "Received onStartCommand 2 "+startId);

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {

            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            createNotificationChannel();
            createNotification();
        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Log.i(LOG_TAG, "Clicked Previous");
            try {
                ActivityManager.getService().requestBugReport(0);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "error taking bugreport bugreportType=", e);
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BUGREPORT.INTENT_BUGREPORT_STARTED);
            intentFilter.addAction(Constants.BUGREPORT.INTENT_BUGREPORT_FINISHED);
            intentFilter.addAction(Constants.BUGREPORT.INTENT_REMOTE_BUGREPORT_FINISHED);

            BroadcastReceiver mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.e(LOG_TAG, "mReceiver onReceive BUGREPORT intent = "+intent);
                }
            };
            registerReceiver(mReceiver, intentFilter);

        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ShellExecuter exe = new ShellExecuter();
                    String command = "dumpsys window";
                    String outp = exe.Executer(command);
                    Log.d("Output", outp);

                    String filename="dumpsys_win.txt";
                    String data=outp;

                    FileOutputStream fos;
                    try {

                        File extStore = Environment.getExternalStorageDirectory();
                        //File extStore = Environment.getDataDirectory();
                        // ==> /storage/emulated/0/note.txt
                        String path = extStore.getAbsolutePath() + "/" + filename;
                        Log.i(LOG_TAG, "Read file: " + path);

                        File myFile = new File(path);
                        myFile.createNewFile();
                        FileOutputStream fOut = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        myOutWriter.append(data);
                        myOutWriter.close();
                        fOut.close();
                        //Toast.makeText(getApplicationContext(),filename + " saved",Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        Log.i(LOG_TAG, "FileNotFoundException found");
                        e.printStackTrace();}
                    catch (IOException e) {
                        Log.i(LOG_TAG, "IOException found ");
                        e.printStackTrace();
                    }
                }
            }).start();



        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] cmds = {"dumpsys > /sdcard/prem.txt", "dumpstate > /sdcard/prem_du.txt"};
                    try {
                        Process p = Runtime.getRuntime().exec("su");
                        DataOutputStream os = new DataOutputStream(p.getOutputStream());
                        for (String tmpCmd : cmds) {
                            os.writeBytes(tmpCmd + "\n");
                        }
                        os.writeBytes("exit\n");
                        os.flush();
                    } catch (Exception e){
                        Log.i(LOG_TAG, "Clicked Next error");
                        e.printStackTrace();
                    }
                }
            }).start();

        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    // When you have the request results


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.d(LOG_TAG, "TASK REMOVED");

        Intent startIntent = new Intent(getApplicationContext(), ForegroundService.class);
        startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);

        PendingIntent service = PendingIntent.getService(
                getApplicationContext(),
                1001,
                startIntent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000, service);
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
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, ForegroundService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, ForegroundService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, ForegroundService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);



        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "LAVA")
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentTitle("Lava Logging Service2")
                .setContentText("Click to start taking dump2")
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.FLAG_NO_CLEAR)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_media_previous,"Previous", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play", pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent);


        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                mBuilder.build());
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//
//
//        // notificationId is a unique int for each notification that you must define
//        notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, mBuilder.build());
    }


    public File getLavaLogsStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS), "LavaLogs");
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }
}