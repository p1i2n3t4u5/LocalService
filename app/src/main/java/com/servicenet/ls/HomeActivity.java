package com.servicenet.ls;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {


    private TextView address;
    private LocationManager locationManager;
    private boolean gps;
    private boolean networkProvider;


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // == 5 SEC


    //lists for permission

    private List<String> permissionsToRequest;
    private List<String> permissionsRejected = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    private static final int ALL_PERMISSIONS_RESULT = 1011;


    //Define fields for Google API Client
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;

    private boolean doubleBackToExitPressedOnce = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.address_search_id);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        address = (TextView) findViewById(R.id.search_address_edittext_id);
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,AddressSearchActivity.class));
                finish();
            }
        });





        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gps = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
        networkProvider = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

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


        if (!gps) {
            Toast.makeText(this, "Gps Not enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gps enabled", Toast.LENGTH_SHORT).show();
        }
        if (!networkProvider) {
            Toast.makeText(this, "Network Not enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Network enabled", Toast.LENGTH_SHORT).show();
        }


        try {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object

                                //address.setText(String.valueOf(location.getLatitude()) + "  (1)  " + String.valueOf(location.getLongitude()));
                                getCompleteAddressString(location.getLatitude(),location.getLongitude());

                            } else {
                                Toast.makeText(HomeActivity.this, "location is null (1)", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });



            mFusedLocationClient.getLastLocation().addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(HomeActivity.this, "Failed to fetch location in (1)", Toast.LENGTH_SHORT).show();
                }
            });

            locationRequest = LocationRequest.create();
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            if (address.getText().toString().equals(""))
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            else
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        // address.setText(String.valueOf(location.getLatitude()) + " (2) " + String.valueOf(location.getLongitude()));
                        getCompleteAddressString(location.getLatitude(),location.getLongitude());
                    }
                }

                ;
            };
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Log.d("LocalService", "Menu clicked:" + item.getItemId());

        Log.d("LocalService", "R.id.share_id:" + R.id.share_id);
        Log.d("LocalService", "R.id.profile_id:" + R.id.profile_id);

        switch (item.getItemId()) {

            case R.id.share_id:
                Toast.makeText(this, "Share option clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.profile_id:
                Toast.makeText(this, "Profile option clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        getLastLocation();
        startLocationUpdates();

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.doubleBackToExitPressedOnce = false;

    }


    @Override
    protected void onPause() {
        stopLocationUpdates();
        super.onPause();

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
                            new AlertDialog.Builder(HomeActivity.this).
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


    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }


    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            lastLocation = task.getResult();

                            //address.setText(String.valueOf(lastLocation.getLatitude()) + "  (3)  " + String.valueOf(lastLocation.getLongitude()));
                            getCompleteAddressString(lastLocation.getLatitude(),lastLocation.getLongitude());

                        } else {
                            Log.w("LocalService", "getLastLocation:exception", task.getException());
                            Toast.makeText(HomeActivity.this, "No Location Detected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    private void startLocationUpdates() {
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

                address.setText(strAdd);

                Log.d("LocalService", "My Current location address" + strReturnedAddress.toString());
            } else {

                Log.d("LocalService", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("LocalService", "Cannot get Address!");
        }
        return strAdd;
    }



    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to Exit the application", Toast.LENGTH_SHORT).show();

    }


}
