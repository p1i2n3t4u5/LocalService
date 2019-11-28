package com.servicenet.ls;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.servicenet.ls.util.AppConstants;
import com.servicenet.ls.util.GpsUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressSearchActivity extends AppCompatActivity implements GpsUtils.OnGpsListener {

    private EditText searchEditText;
    private TextView userCurrentLocation;
    private GpsUtils gpsUtils;
    private boolean isGPS = false;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // == 5 SEC

    //Define fields for Google API Client
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;

    //lists for permission

    private List<String> permissionsToRequest;
    private List<String> permissionsRejected = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    private static final int ALL_PERMISSIONS_RESULT = 1011;

    private static final int MAX_LOCATION_UPDATES = 1;
    private static int location_update_count = 0;
    private static double LATITUDE;
    private static double LONGITUDE;
    private static String ADDRESS_STRING;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_search);
        Toolbar toolbar = findViewById(R.id.address_search_id);
        setSupportActionBar(toolbar);
        Log.d("LocalService", "AddressSearchActivity  onCreate()");
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        searchEditText=findViewById(R.id.search_edittext_id);


        //WE add permission we need to request location of the user
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.
                        toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }


        try {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//            mFusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            // Got last known location. In some rare situations this can be null.
//                            if (location != null) {
//                                // Logic to handle location object
//
//                                Log.d("LocalService", "AddressSearchActivity onCreate() addOnSuccessListener() : " + location.getLatitude() + "  " + location.getLongitude());
//                                LATITUDE = location.getLatitude();
//                                LONGITUDE = location.getLongitude();
//                                getCompleteAddressString(location.getLatitude(), location.getLongitude());
//
//                            } else {
//                                Toast.makeText(AddressSearchActivity.this, "location is null", Toast.LENGTH_SHORT).show();
//                                Log.d("LocalService", "AddressSearchActivity onCreate() addOnSuccessListener() location is null");
//                            }
//                        }
//                    });
//
//
//            mFusedLocationClient.getLastLocation().addOnFailureListener(this, new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.d("LocalService", "AddressSearchActivity onCreate() addOnFailureListener() : " + e.getMessage());
//                }
//            });

            locationRequest = LocationRequest.create();
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
//            if (address.getText().toString().equals(""))
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            else
            //locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        Log.d("LocalService", "AddressSearchActivity onCreate() onLocationResult() : " + location.getLatitude() + "  " + location.getLongitude());
                        if (location_update_count < MAX_LOCATION_UPDATES) {
                            location_update_count++;
                            LATITUDE=location.getLatitude();
                            LONGITUDE=location.getLongitude();
                            getCompleteAddressString(LATITUDE, LONGITUDE);
                        }else {
                            Intent intent = new Intent(AddressSearchActivity.this, HomeActivity.class);
                            intent.putExtra("ADDRESS",ADDRESS_STRING);
                            intent.putExtra("LATITUDE",LATITUDE);
                            intent.putExtra("LONGITUDE",LONGITUDE);
                            startActivity(intent);
                            finish();
                        }

                    }
                }
            };
        } catch (SecurityException ex) {
            ex.printStackTrace();
            Log.w("LocalService", "AddressSearchActivity onCreate() SecurityException : " + ex.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            Log.w("LocalService", "AddressSearchActivity onCreate() Exception : " + e.getMessage());
        }


        gpsUtils = new GpsUtils(AddressSearchActivity.this);

        userCurrentLocation = findViewById(R.id.user_current_location_id);
        userCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gpsUtils.turnGPSOn(AddressSearchActivity.this);

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LocalService", "AddressSearchActivity  onStart()");

    }

    @Override
    protected void onResume() {
        Log.d("LocalService", "AddressSearchActivity onResume()");
        super.onResume();
    }


    @Override
    protected void onPause() {
        Log.d("LocalService", "AddressSearchActivity  onPause()");
        stopLocationUpdates();
        super.onPause();

    }


    private void stopLocationUpdates() {
        Log.d("LocalService", "AddressSearchActivity  stopLocationUpdates()");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void gpsStatus(boolean isGPSEnable) {
        // turn on GPS

        isGPS = isGPSEnable;
        Log.d("LocalService", "AddressSearchActivity isGPS:" + isGPS);
        getLastLocation();
        startLocationUpdates();
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(AddressSearchActivity.this).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }
                }

                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
                Log.d("LocalService", "AddressSearchActivity isGPS:" + isGPS);
                getLastLocation();
                startLocationUpdates();
            }
        }
    }


    private void startLocationUpdates() {
        Log.d("LocalService", "AddressSearchActivity startLocationUpdates()");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

    }


    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        Log.d("LocalService", "AddressSearchActivity  LATITUDE:  "+LATITUDE+"   LONGITUDE:"+LONGITUDE);
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                ADDRESS_STRING = strAdd;
                //searchEditText.setText(strAdd);

                Log.d("LocalService", "AddressSearchActivity My Current location address" + strReturnedAddress.toString());
            } else {

                Log.d("LocalService", "AddressSearchActivity No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("LocalService", "AddressSearchActivity Cannot get Address!"+e.getMessage());

        }
        return strAdd;
    }


    private ArrayList<String> permissionsToRequest(List<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wantedPermissions) {
            if (!hashPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }


    private boolean hashPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ;
        {
            return ActivityCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;

        }
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }


    private void getLastLocation() {
        Log.d("LocalService", "HomeActivity getLastLocation()");
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            lastLocation = task.getResult();

                            Log.d("LocalService", "HomeActivity getLastLocation() : " + lastLocation.getLatitude() + "  " + lastLocation.getLongitude());
                            if (location_update_count < MAX_LOCATION_UPDATES) {
                                location_update_count++;
                                LATITUDE=lastLocation.getLatitude();
                                LONGITUDE=lastLocation.getLongitude();
                                getCompleteAddressString(LATITUDE, LONGITUDE);
                            }else {
                                Intent intent = new Intent(AddressSearchActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            Log.w("LocalService", "HomeActivity getLastLocation:exception", task.getException());
                            Toast.makeText(AddressSearchActivity.this, "No Location Detected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
