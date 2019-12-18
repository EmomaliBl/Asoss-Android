package com.example.asoss;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Date;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class MainActivity extends AppCompatActivity {
    @IgnoreExtraProperties
    static class Item implements Serializable {
        public String name;
        public String lat;
        public String lon;

        public Item() {

        }

        Item(String name,String lat,String lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }
    public static final String PREFERENCES_FILE_NAME = "MyAppPreferences";
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    final Context context = this;


    GoogleMap map;
    private static double TARGET_LATITUDE = 55.754724;
    private static final double TARGET_LONGITUDE = 37.621380;
    private Address location;

    TextView tvEnabledGPS;
    TextView tvStatusGPS;
    TextView tvLocationGPS;
    TextView tvEnabledNet;
    TextView tvStatusNet;
    TextView tvLocationNet;

    private LocationManager locationManager;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();

    private ProgressBar progressBar;
    private LatLng myLocation;
    private Object Fragment2;
    private double st;
    private Object TextView;
    private String lat;
    private String lon;
    private TextView reg_name;
    private ItemsAdapter adapter;
    private Button add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("items");




        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        reg_name = (TextView) findViewById(R.id.reg_name);
        final Button btnLocationSettings = (Button) findViewById(R.id.btnLocationSettings);
        final EditText name = (EditText) findViewById(R.id.name);
        add = (Button) findViewById(R.id.add);
        final ListView items = (ListView) findViewById(R.id.items);
        adapter = new ItemsAdapter();


        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.reg_main, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
        mDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.input_text);
        if((!locationManager
                .isProviderEnabled(GPS_PROVIDER) ||
                !locationManager
                        .isProviderEnabled(NETWORK_PROVIDER))) {
        mDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                reg_name.setText(userInput.getText());
                            }
                        });
            AlertDialog alertDialog = mDialogBuilder.create();
            alertDialog.show();
        }

        //myRef.removeValue();
        items.setAdapter(adapter);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = new Item(reg_name.getText().toString(),lat,lon);
                adapter.add(item);
                myRef.push().setValue(item);

            }
        });


    }

    private class ItemsAdapter extends ArrayAdapter<Item> {
        ItemsAdapter() {
            super(MainActivity.this, R.layout.items);
        }

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.reg_main, null);

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final View view = getLayoutInflater().inflate(R.layout.items, null);
            final Item item = getItem(position);
            ((TextView) promptsView.findViewById(R.id.input_text)).setText(item.name);
            ((TextView) view.findViewById(R.id.price)).setText(String.valueOf(item.lat));
            return view;
        }
    }





    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        locationManager.requestLocationUpdates(GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
            }
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(
                NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
        }
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));




    }

    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: "
                + locationManager
                .isProviderEnabled(GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: "
                + locationManager
                .isProviderEnabled(NETWORK_PROVIDER));
    }

    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        new CountDownTimer(10000,1000){

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if((locationManager
                        .isProviderEnabled(GPS_PROVIDER) ||
                        locationManager
                                .isProviderEnabled(NETWORK_PROVIDER))) {

                    Item item = new Item(reg_name.getText().toString(),lat,lon);
                    myRef.push().setValue(item);
                }

            }
        }.start();
    };

    public void onClickMap(View view) {
//        Intent intent = new Intent(this, Main2Activity.class);
//        //lat = tvLocationNet.getText().toString();
//        intent.putExtra("lat", lat);
//        intent.putExtra("lon", lon);
//        startActivity(intent);
    }

}