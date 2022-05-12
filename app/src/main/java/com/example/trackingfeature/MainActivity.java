package com.example.trackingfeature;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Date;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    //initialize variables

    //initialize fence variables
    private static final String TAG = "Main Activity";
    private PendingIntent mPendingIntent;
    private FenceReceiver mFenceReceiver;
    private static final String FENCE_KEY = "FENCE_KEY";
    TextView displayText, locationDisplayText, headphonesDisplayText, bikeDisplayText, runDisplayText, walkDisplayText;

    //Location Fence for Wellness Centre/MSC
    private PendingIntent lfPendingIntent;
    private LocationFenceReceiver lfFenceReceiver;
    private static final String LOCATION_FENCE_KEY = "LOCATION_FENCE_KEY";

    //Headphones Fence
    private PendingIntent hfPendingIntent;
    private HeadphonesFenceReceiver hfFenceReceiver;
    private static final String HEADPHONES_FENCE_KEY = "HEADPHONES_FENCE_KEY";

    //Activity Fences
    private PendingIntent bikePendingIntent;
    private BikeFenceReceiver bikeFenceReceiver;
    private static final String BIKE_FENCE_KEY = "BIKE_FENCE_KEY";

    private PendingIntent runPendingIntent;
    private RunFenceReceiver runFenceReceiver;
    private static final String RUN_FENCE_KEY = "RUN_FENCE_KEY";

    private PendingIntent walkPendingIntent;
    private WalkFenceReceiver walkFenceReceiver;
    private static final String WALK_FENCE_KEY = "WALK_FENCE_KEY";

    //Variables for pushing notifications
    private static final String CHANNEL_ID = "FenceChannel";
    private static final String GROUP_KEY = "FenceGroup";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        //assign variables

        //Connect variables to their UI elements
        displayText = findViewById(R.id.testView);
        locationDisplayText = findViewById(R.id.locationFenceView);
        headphonesDisplayText = findViewById(R.id.headphonesFenceView);
        bikeDisplayText = findViewById(R.id.bikeFenceView);
        runDisplayText = findViewById(R.id.runFenceView);
        walkDisplayText = findViewById(R.id.walkFenceView);


        // For each fence a new pending intent has to be created. To create a pending intent, we need to create an Intent. To create fences, we have to create Intents
        // by creating overloaded broadcast receiver classes. The pending intent variable will be intialized as an listener to the changes in our fences.
        Intent intent = new Intent(FenceReceiver.FENCE_RECEIVER_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        mFenceReceiver = new FenceReceiver();


        //Location Fence
        Intent intentLocationFence = new Intent(LocationFenceReceiver.FENCE_RECEIVER_ACTION);
        lfPendingIntent = PendingIntent.getBroadcast(this, 0, intentLocationFence, PendingIntent.FLAG_MUTABLE);
        lfFenceReceiver = new LocationFenceReceiver();

        //Headphones Fence
        Intent intentHeadphonesFence = new Intent(HeadphonesFenceReceiver.FENCE_RECEIVER_ACTION);
        hfPendingIntent = PendingIntent.getBroadcast(this, 0, intentHeadphonesFence, PendingIntent.FLAG_MUTABLE);
        hfFenceReceiver = new HeadphonesFenceReceiver();

        //Activity Fences
        Intent intentBikeFence = new Intent(BikeFenceReceiver.FENCE_RECEIVER_ACTION);
        bikePendingIntent = PendingIntent.getBroadcast(this, 0, intentBikeFence, PendingIntent.FLAG_MUTABLE);
        bikeFenceReceiver = new BikeFenceReceiver();

        Intent intentRunFence = new Intent(RunFenceReceiver.FENCE_RECEIVER_ACTION);
        runPendingIntent = PendingIntent.getBroadcast(this, 0, intentRunFence, PendingIntent.FLAG_MUTABLE);
        runFenceReceiver = new RunFenceReceiver();

        Intent intentWalkFence = new Intent(WalkFenceReceiver.FENCE_RECEIVER_ACTION);
        walkPendingIntent = PendingIntent.getBroadcast(this, 0, intentWalkFence, PendingIntent.FLAG_MUTABLE);
        walkFenceReceiver = new WalkFenceReceiver();

        setupFence();

        // register the receivers listening to changes in the fence state
        registerReceiver(hfFenceReceiver, new IntentFilter(HeadphonesFenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(mFenceReceiver, new IntentFilter(FenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(lfFenceReceiver, new IntentFilter(LocationFenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(bikeFenceReceiver, new IntentFilter(BikeFenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(runFenceReceiver, new IntentFilter(RunFenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(walkFenceReceiver, new IntentFilter(WalkFenceReceiver.FENCE_RECEIVER_ACTION));



    }


    @Override
    protected void onStart() {
        super.onStart();

//        NOTE: Fences can be either initialized and setup in the onCreate method or in the onStart method. Using either options affects performance and consistency in
//              some sort of way which should be tested before letting user use the App


//        setupFence();
//        registerReceiver(hfFenceReceiver, new IntentFilter(HeadphonesFenceReceiver.FENCE_RECEIVER_ACTION));
//        registerReceiver(mFenceReceiver, new IntentFilter(FenceReceiver.FENCE_RECEIVER_ACTION));
//        registerReceiver(lfFenceReceiver, new IntentFilter(LocationFenceReceiver.FENCE_RECEIVER_ACTION));
//        registerReceiver(bikeFenceReceiver, new IntentFilter(BikeFenceReceiver.FENCE_RECEIVER_ACTION));
//        registerReceiver(runFenceReceiver, new IntentFilter(RunFenceReceiver.FENCE_RECEIVER_ACTION));
//        registerReceiver(walkFenceReceiver, new IntentFilter(WalkFenceReceiver.FENCE_RECEIVER_ACTION));
    }

    @Override
    protected void onStop() {

        //Unregister the listeners when the user changes their screen from the app to any other screen.
        super.onStop();
        unregisterFence();
        unregisterReceiver(mFenceReceiver);
        unregisterReceiver(lfFenceReceiver);
        unregisterReceiver(hfFenceReceiver);
        unregisterReceiver(bikeFenceReceiver);
        unregisterReceiver(runFenceReceiver);
        unregisterReceiver(walkFenceReceiver);
    }

    protected void onDestroy(){
        super.onDestroy();

//        NOTE: In recent updates of the android API, it is not possible to unregister fences in the onDestroy method, as it is not allowed to have fences running in
//              the background. But in one of the test runs the fences were running in the background, even when the app was closed (not killed). This should be explored
//              further since it can be an easy way to run the fences in the background.

//        unregisterFence();
//        unregisterReceiver(mFenceReceiver);
//        unregisterReceiver(lfFenceReceiver);
//        unregisterReceiver(hfFenceReceiver);
//        unregisterReceiver(bikeFenceReceiver);
//        unregisterReceiver(runFenceReceiver);
//        unregisterReceiver(walkFenceReceiver);
    }


    // Function to setup the Fences. Edit the parameters of the fences to change its behavior.
    // Each fences can be setup in various ways, please visit the awareness api documentation for more info
    private void setupFence() {

        AwarenessFence timeFence = TimeFence.inDailyInterval(TimeZone.getDefault(),
                0L, 24L * 60L * 60L * 1000L);
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);


       //*******************************************************************************************************************************************
        @SuppressLint("MissingPermission")
        //AwarenessFence locationFence = LocationFence.in(28.064091447842966, -82.41310445046034, 50, 10 * 1000);
        AwarenessFence locationFence = LocationFence.in(28.064857189859197, -82.43403777124016, 5, 100);
// 28.064857189859197, -82.43403777124016
        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .addFence(LOCATION_FENCE_KEY, locationFence, lfPendingIntent)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Location Fence was successfully registered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Location Fence could not be registered: " + e);
                        e.printStackTrace();
                    }
                });
        //*******************************************************************************************************************************************

        //*******************************************************************************************************************************************
        @SuppressLint("MissingPermission")
        AwarenessFence headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .addFence(HEADPHONES_FENCE_KEY, headphoneFence, hfPendingIntent)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Headphones Fence was successfully registered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Headphones Fence could not be registered: " + e);
                        e.printStackTrace();
                    }
                });
        //*******************************************************************************************************************************************


        //*******************************************************************************************************************************************
        @SuppressLint("MissingPermission")
        AwarenessFence bikeFence = DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE);

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .addFence(BIKE_FENCE_KEY, bikeFence, bikePendingIntent)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Bike Fence was successfully registered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Bike Fence could not be registered: " + e);
                        e.printStackTrace();
                    }
                });
        //*******************************************************************************************************************************************

        //*******************************************************************************************************************************************
        @SuppressLint("MissingPermission")
        AwarenessFence runFence = DetectedActivityFence.during(DetectedActivityFence.RUNNING);

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .addFence(RUN_FENCE_KEY, runFence, runPendingIntent)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Run Fence was successfully registered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Run Fence could not be registered: " + e);
                        e.printStackTrace();
                    }
                });
        //*******************************************************************************************************************************************

        //*******************************************************************************************************************************************
        @SuppressLint("MissingPermission")
        AwarenessFence walkFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .addFence(WALK_FENCE_KEY, walkFence, walkPendingIntent)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Walk Fence was successfully registered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Walk Fence could not be registered: " + e);
                        e.printStackTrace();
                    }
                });
        //*******************************************************************************************************************************************
        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .addFence(FENCE_KEY, timeFence, mPendingIntent)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Fence was successfully registered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Fence could not be registered: " + e);
                    }
                });


    }



    // This function unregisters all the fences. The code is implemented using awareness api documentation.
    private void unregisterFence() {

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .removeFence(FENCE_KEY)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Fence was successfully unregistered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Fence could not be unregistered: " + e);
                    }
                });

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .removeFence(LOCATION_FENCE_KEY)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Location Fence was successfully unregistered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Location Fence could not be unregistered: " + e);
                    }
                });

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .removeFence(BIKE_FENCE_KEY)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Bike Fence was successfully unregistered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Bike Fence could not be unregistered: " + e);
                    }
                });

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .removeFence(RUN_FENCE_KEY)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Run Fence was successfully unregistered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Run Fence could not be unregistered: " + e);
                    }
                });

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .removeFence(WALK_FENCE_KEY)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Walk Fence was successfully unregistered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Walk Fence could not be unregistered: " + e);
                    }
                });

        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .removeFence(HEADPHONES_FENCE_KEY)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Headphones Fence was successfully unregistered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Headphones Fence could not be unregistered: " + e);
                    }
                });


    }


    // As mentioned earlier, to create fences we have to use pending intents which are created using Intents initialized using overloaded broadcast receiver classes.
    // The following are the implemented classes for each of the fences. Make sure to create a new one for every new fence implemented.
    // The first class has comments explaining each element for documentation purposes. The following classes have the same structure and logic.

    private class FenceReceiver extends BroadcastReceiver {

        // a string variable pointing to itself. This is used to initialize the class when creating an Intent
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.FenceReceiver.FENCE_RECEIVER_ACTION";

        // Override the onReceive functions to customize the response to changes detected in the fence.
        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
                String fenceStateStr;
                switch (fenceState.getCurrentState()) { // Fence state will be either True, False, or unknown based on the parameters set and the state of the device
                    case FenceState.TRUE:
                        fenceStateStr = "true";
                        // add logic for True here
                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "false";
                        // add logic for false here
                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "unknown";
                        // debug
                        break;
                    default:
                        fenceStateStr = "unknown value";
                }
                displayText.setText(fenceStateStr);
                Log.d(TAG, fenceStateStr);

                // Push a toast notification. Used for testing
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, fenceStateStr, duration);
                toast.show();

                //Push a notification:
                //For notifications too we need to create a pending intent. The Intent used here will be created using the  notificationReceiver
                // (overloaded broadcastReceiver)class
                Intent notifyIntent = new Intent(MainActivity.this, notificationReciever.class);
                notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent notifyPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, notifyIntent, PendingIntent.FLAG_MUTABLE);

                //Using the pending intent we can create our notification. Android offers a lot of options fro customizing, grouping, and organizing notifications.
                // Please visit the android documentation on notifications to create various notifications
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Time Fence")
                        .setContentText(fenceStateStr)
                        .setContentIntent(notifyPendingIntent)
                        .setAutoCancel(true);


                PushNotification( builder, 1);
            }


        }

    }

    private class LocationFenceReceiver extends BroadcastReceiver {
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.LocationFenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), LOCATION_FENCE_KEY)) {
                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        fenceStateStr = "Near MSC";

                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "Away from MSC";

                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "Location unknown";

                        break;
                    default:
                        fenceStateStr = "unknown value";
                }
                locationDisplayText.setText(fenceStateStr);
                Log.d(TAG, fenceStateStr);

//                int duration = Toast.LENGTH_SHORT;
//                Toast toast = Toast.makeText(context, fenceStateStr, duration);
//                toast.show();
                Intent lnotifyIntent = new Intent(MainActivity.this, notificationReciever.class);
                lnotifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent lnotifyPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, lnotifyIntent, PendingIntent.FLAG_MUTABLE);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Location Fence")
                        .setContentText(fenceStateStr)
                        .setContentIntent(lnotifyPendingIntent)
                        .setAutoCancel(true);


                PushNotification( builder, 2);


            }
        }
    }

    private class HeadphonesFenceReceiver extends BroadcastReceiver {
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.HeadphonesFenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), HEADPHONES_FENCE_KEY)) {
                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        fenceStateStr = "Headphones Plugged in, play some music";

                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "Headphones unplugged";

                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "Headphones state unknown";

                        break;
                    default:
                        fenceStateStr = "unknown value";
                }
                headphonesDisplayText.setText(fenceStateStr);
                Log.d(TAG, fenceStateStr);

//                int duration = Toast.LENGTH_SHORT;
//                Toast toast = Toast.makeText(context, fenceStateStr, duration);
//                toast.show();
                Intent hnotifyIntent = new Intent(MainActivity.this, notificationReciever.class);
                hnotifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent hnotifyPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, hnotifyIntent, PendingIntent.FLAG_MUTABLE);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Headphones Fence")
                        .setContentText(fenceStateStr)
                        .setContentIntent(hnotifyPendingIntent)
                        .setAutoCancel(true);

                PushNotification( builder, 3);
            }
        }

    }

    private class BikeFenceReceiver extends BroadcastReceiver {
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.BikeFenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);
            ;
            if (TextUtils.equals(fenceState.getFenceKey(), BIKE_FENCE_KEY)) {

                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        fenceStateStr = "Biking";

                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "Not Biking";

                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "Unknown biking state";

                        break;
                    default:
                        fenceStateStr = "unknown value";
                }
                bikeDisplayText.setText(fenceStateStr);
                Log.d(TAG, fenceStateStr);

//                int duration = Toast.LENGTH_SHORT;
//                Toast toast = Toast.makeText(context, fenceStateStr, duration);
//                toast.show();
                Intent bnotifyIntent = new Intent(MainActivity.this, notificationReciever.class);
                bnotifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent bnotifyPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, bnotifyIntent, PendingIntent.FLAG_MUTABLE);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Bike Fence")
                        .setContentText(fenceStateStr)
                        .setContentIntent(bnotifyPendingIntent)
                        .setAutoCancel(true);

                PushNotification( builder, 4);
            }
        }

    }

    private class RunFenceReceiver extends BroadcastReceiver {
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.RunFenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), RUN_FENCE_KEY)) {

                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        fenceStateStr = "running";

                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "Not running";

                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "Unknown running state";

                        break;
                    default:
                        fenceStateStr = "unknown value";
                }
                runDisplayText.setText(fenceStateStr);
                Log.d(TAG, fenceStateStr);

//                int duration = Toast.LENGTH_SHORT;
//                Toast toast = Toast.makeText(context, fenceStateStr, duration);
//                toast.show();
                Intent rnotifyIntent = new Intent(MainActivity.this, notificationReciever.class);
                rnotifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent rnotifyPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, rnotifyIntent, PendingIntent.FLAG_MUTABLE);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Running Fence")
                        .setContentText(fenceStateStr)
                        .setContentIntent(rnotifyPendingIntent)
                        .setAutoCancel(true);

                PushNotification( builder, 5);
            }
        }

    }

    private class WalkFenceReceiver extends BroadcastReceiver {
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.WalkFenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), WALK_FENCE_KEY)) {

                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:

                        fenceStateStr = "Walking";

                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "Not Walking";

                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "Unknown Walking state";

                        break;
                    default:
                        fenceStateStr = "unknown value";
                }
                walkDisplayText.setText(fenceStateStr);
                Log.d(TAG, fenceStateStr);

//                int duration = Toast.LENGTH_SHORT;
//                Toast toast = Toast.makeText(context, fenceStateStr, duration);
//                toast.show();
                Intent wnotifyIntent = new Intent(MainActivity.this, notificationReciever.class);
                wnotifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent wnotifyPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, wnotifyIntent, PendingIntent.FLAG_MUTABLE);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Walking Fence")
                        .setContentText(fenceStateStr)
                        .setContentIntent(wnotifyPendingIntent)
                        .setAutoCancel(true);

                PushNotification( builder, 6);
            }
        }

    }
    // Overloaded broadcastReceiver class that receives notification pushes
    private class notificationReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "notifcation tapped)");
        }
    }

    //Helper function to push notifications
    private void PushNotification( NotificationCompat.Builder builder  , int notificationId){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.setGroup(GROUP_KEY).build());

    }

    //Function ot create notification channel. Source: android documentation
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "fenceChannel";
            String description = "Notification channel for fences";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}