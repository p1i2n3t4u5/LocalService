package com.servicenet.ls;


import android.Manifest;
import android.app.Activity;
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
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.servicenet.ls.adapter.Adapter;
import com.servicenet.ls.util.AppConstants;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {


    private TextView address;
    private LocationManager locationManager;
    private boolean gps;
    private boolean networkProvider;


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // == 5 SEC

    private static final int MAX_LOCATION_UPDATES = 3;
    private static int location_update_count = 0;
    private static Double LATITUDE = 0.0;
    private static Double LONGITUDE = 0.0;
    private static String addressString;


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


    private RecyclerView recyclerView;
    private Adapter adapter;
    private ArrayList<String> data;


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


        recyclerView = findViewById(R.id.service_recylerview_id);
        data = new ArrayList<>();
        data.add("First Service Label");
        data.add("Second Service Label");
        data.add("Third Service Label");
        data.add("Fourth Service Label");
        data.add("Fifth Service Label");
        data.add("Sixth Service Label");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this, data);
        recyclerView.setAdapter(adapter);


        address = (TextView) findViewById(R.id.search_edittext_id);
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AddressSearchActivity.class));
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

                                Log.d("LocalService", "HomeActivity onCreate() addOnSuccessListener() : " + location.getLatitude() + "  " + location.getLongitude());
                                if (location_update_count < MAX_LOCATION_UPDATES) {
                                    location_update_count++;
                                    LATITUDE=location.getLatitude();
                                    LONGITUDE=location.getLongitude();
                                    getCompleteAddressString(LATITUDE, LONGITUDE);
                                }

                            } else {
                                Toast.makeText(HomeActivity.this, "location is null", Toast.LENGTH_SHORT).show();
                                Log.d("LocalService", "HomeActivity onCreate() addOnSuccessListener() location is null");
                            }
                        }
                    });


            mFusedLocationClient.getLastLocation().addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("LocalService", "HomeActivity onCreate() addOnFailureListener() : " + e.getMessage());
                }
            });

            locationRequest = LocationRequest.create();
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
//            if (address.getText().toString().equals(""))
//                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            else
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        Log.d("LocalService", "HomeActivity onCreate() onLocationResult() : " + location.getLatitude() + "  " + location.getLongitude());
                        if (location_update_count < MAX_LOCATION_UPDATES) {
                            location_update_count++;
                            LATITUDE=location.getLatitude();
                            LONGITUDE=location.getLongitude();
                            getCompleteAddressString(LATITUDE, LONGITUDE);
                        }
                    }
                }
            };
        } catch (SecurityException ex) {
            ex.printStackTrace();
            Log.w("LocalService", "onCreate() SecurityException : " + ex.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            Log.w("LocalService", "onCreate() Exception : " + e.getMessage());
        }


    }


    private ArrayList<String> permissionsToRequest(List<String> wantedPermissions) {
        Log.w("LocalService", "permissionsToRequest() wantedPermissions: " + wantedPermissions);
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wantedPermissions) {
            if (!hashPermission(perm)) {
                result.add(perm);
            }

        }
        Log.w("LocalService", "permissionsToRequest() needed permissions: " + result.size());
        return result;
    }


    private boolean hashPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ;
        {
            return ActivityCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LocalService", "HomeActivity onStart()");

        if (LATITUDE !=0.0 && LONGITUDE != 0.0){
            address.setText(addressString);
            stopLocationUpdates();
        }else {
            getLastLocation();
            startLocationUpdates();
        }



    }

    @Override
    protected void onResume() {
        Log.d("LocalService", "HomeActivity onResume()");
        super.onResume();
        if (LATITUDE !=0.0 && LONGITUDE != 0.0){
            address.setText(addressString);
            stopLocationUpdates();
        }else {
            startLocationUpdates();
        }
        this.doubleBackToExitPressedOnce = false;

    }


    @Override
    protected void onPause() {
        Log.d("LocalService", "HomeActivity onPause()");
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
        Log.d("LocalService", "HomeActivity hasPermission():" + permission);
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
                            }

                        } else {
                            Log.w("LocalService", "HomeActivity getLastLocation:exception", task.getException());
                            Toast.makeText(HomeActivity.this, "No Location Detected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void stopLocationUpdates() {
        Log.d("LocalService", "HomeActivity stopLocationUpdates()");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    private void startLocationUpdates() {
        Log.d("LocalService", "HomeActivity startLocationUpdates()");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("LocalService", "HomeActivity startLocationUpdates()  permission not granted");
            return;
        }
        Log.d("LocalService", "HomeActivity startLocationUpdates()  permission  granted");
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        Log.d("LocalService", "HomeActivity getCompleteAddressString()" + LATITUDE + "  " + LONGITUDE+"  "+location_update_count);
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
                addressString=strAdd;

                Log.d("LocalService", "HomeActivity My Current location address" + strReturnedAddress.toString());
            } else {

                Log.d("LocalService", "HomeActivity No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("LocalService", "HomeActivity Cannot get Address!");
        }
        return strAdd;
    }


    @Override
    public void onBackPressed() {
        Log.d("LocalService", "HomeActivity onBackPressed()");
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to Exit the application", Toast.LENGTH_SHORT).show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);

        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Toast.makeText(HomeActivity.this, "OnActionExpandListener Expanded", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Toast.makeText(HomeActivity.this, "OnActionExpandListener Collapsed", Toast.LENGTH_SHORT).show();
                return true;
            }
        };

        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchMenuItem.setOnActionExpandListener(onActionExpandListener);

        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Log.d("LocalService", "HomeActivityMenu clicked:" + item.getItemId());

        Log.d("LocalService", "HomeActivity R.id.share_id:" + R.id.share_id);
        Log.d("LocalService", "HomeActivity R.id.profile_id:" + R.id.profile_id);

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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        String userInputText = newText.toLowerCase();
        List<String> newList = new ArrayList<>();

        for (String service : data) {

            if (service.toLowerCase().contains(userInputText)) {
                newList.add(service);
            }

        }
        adapter.updateList(newList);

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("LocalService", "HomeActivity onActivityResult()  : " + resultCode);
    }
}
