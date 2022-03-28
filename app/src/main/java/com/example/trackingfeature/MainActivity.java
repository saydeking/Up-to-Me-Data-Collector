package com.example.trackingfeature;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Calendar;
import java.util.Date;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assign variables

        displayText = findViewById(R.id.testView);
        locationDisplayText = findViewById(R.id.locationFenceView);
        headphonesDisplayText = findViewById(R.id.headphonesFenceView);
        bikeDisplayText = findViewById(R.id.bikeFenceView);
        runDisplayText = findViewById(R.id.runFenceView);
        walkDisplayText = findViewById(R.id.walkFenceView);


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





    }


    @Override
    protected void onStart() {
        super.onStart();
        setupFence();
        registerReceiver(hfFenceReceiver, new IntentFilter(HeadphonesFenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(mFenceReceiver, new IntentFilter(FenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(lfFenceReceiver, new IntentFilter(LocationFenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(bikeFenceReceiver, new IntentFilter(BikeFenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(runFenceReceiver, new IntentFilter(RunFenceReceiver.FENCE_RECEIVER_ACTION));
        registerReceiver(walkFenceReceiver, new IntentFilter(WalkFenceReceiver.FENCE_RECEIVER_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterFence();
        unregisterReceiver(mFenceReceiver);
        unregisterReceiver(lfFenceReceiver);
        unregisterReceiver(hfFenceReceiver);
        unregisterReceiver(bikeFenceReceiver);
        unregisterReceiver(runFenceReceiver);
        unregisterReceiver(walkFenceReceiver);
    }

    private void setupFence() {

        AwarenessFence timeFence = TimeFence.inDailyInterval(TimeZone.getDefault(),
                0L, 24L * 60L * 60L * 1000L);
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);


       //*******************************************************************************************************************************************
        @SuppressLint("MissingPermission")
        AwarenessFence locationFence = LocationFence.in(28.064091447842966, -82.41310445046034, 50, 10 * 1000);

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


    private class FenceReceiver extends BroadcastReceiver {
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.FenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        fenceStateStr = "true";

                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "false";

                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "unknown";

                        break;
                    default:
                        fenceStateStr = "unknown value";
                }
                displayText.setText(fenceStateStr);
                Log.d(TAG, fenceStateStr);

                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, fenceStateStr, duration);
                toast.show();
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
            }
        }

    }

    private class BikeFenceReceiver extends BroadcastReceiver {
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.BikeFenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);
            int i = 0;

            long time = 0;
            if (TextUtils.equals(fenceState.getFenceKey(), BIKE_FENCE_KEY)) {
                if (i == 0){
                    Date currentTime = Calendar.getInstance().getTime();
                    time = currentTime.getTime() / 1000L;
                } else {
                    Date currentTime = Calendar.getInstance().getTime();
                    time = (currentTime.getTime() - time) / 1000L;
                }
                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        fenceStateStr = "Biking for" + time + " seconds";

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
            }
        }

    }

    private class RunFenceReceiver extends BroadcastReceiver {
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.RunFenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);
            int i = 0;

            long time = 0;
            if (TextUtils.equals(fenceState.getFenceKey(), RUN_FENCE_KEY)) {
                if (i == 0){
                    Date currentTime = Calendar.getInstance().getTime();
                    time = currentTime.getTime() / 1000L;
                } else {
                    Date currentTime = Calendar.getInstance().getTime();
                    time = (currentTime.getTime() - time) / 1000L;
                }
                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        fenceStateStr = "running for" + time + " seconds";

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
            }
        }

    }

    private class WalkFenceReceiver extends BroadcastReceiver {
        public static final String FENCE_RECEIVER_ACTION =
                "com.example.trackingfeature.WalkFenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {

            FenceState fenceState = FenceState.extract(intent);
            int i = 0;

            long time = 0;
            if (TextUtils.equals(fenceState.getFenceKey(), WALK_FENCE_KEY)) {

                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        if (i == 0){
                            Date currentTime = Calendar.getInstance().getTime();
                            time = currentTime.getTime() / 1000L;
                            i++;
                        } else {
                            Date currentTime = Calendar.getInstance().getTime();
                            time = (currentTime.getTime() - time) / 1000L;
                        }
                        fenceStateStr = "Walking for " + time + " seconds";

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
            }
        }

    }


}