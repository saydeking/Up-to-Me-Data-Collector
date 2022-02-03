package com.example.trackingfeature;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //initialize variables
    Button btLocation;
    TextView textView1,textView2,textView3,textView4,textView5;
    FusedLocationProviderClient fusedLocationProviderClient;
    Switch powerSwitch;
    LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assign variables
        powerSwitch = findViewById(R.id.powerSwitch);

        btLocation = findViewById(R.id.bt_location);
        textView1 = findViewById(R.id.text_view1);
        textView2 = findViewById(R.id.text_view2);
        textView3 = findViewById(R.id.text_view3);
        textView4 = findViewById(R.id.text_view4);
        textView5 = findViewById(R.id.text_view5);

        //initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        powerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (powerSwitch.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                }
                else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                }
            }
        });

        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check app permission
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //when permission granted
                    getLocation();
                }
                else {
                    //permission denied
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //initialize location
                Location location = task.getResult();
                if (location != null) {

                    try {
                        //initialize geoCoder
                        Geocoder geocoder = new Geocoder(MainActivity.this,
                                Locale.getDefault());

                        //initialize address list
                        List <Address> addressList = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1);

                        //set latitude on TextView
                        textView1.setText(Html.fromHtml(
                                "<font color = '#6200EE '><b>Latitude :</b><br></font>" + addressList.get(0).getLatitude()
                        ));

                        textView2.setText(Html.fromHtml(
                                "<font color = '#6200EE'><b>Latitude : </b><br></font>"
                                + addressList.get(0).getLongitude()
                        ));
                        //set country name
                        textView3.setText(Html.fromHtml(
                                "<font color = '#6200EE'><b>Country Name : </b><br></font>"
                                        + addressList.get(0).getCountryName()
                        ));
                        //set locality
                        textView4.setText(Html.fromHtml(
                                "<font color = '#6200EE'><b>Locality : </b><br></font>"
                                        + addressList.get(0).getLocality()
                        ));
                        //set address
                        textView5.setText(Html.fromHtml(
                                "<font color = '#6200EE'><b>Adress : </b><br></font>"
                                        + addressList.get(0).getAddressLine(0)
                        ));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}