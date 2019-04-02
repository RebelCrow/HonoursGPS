package com.example.honoursgps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private Button bCalibrate;
    private Button Start;
    private TextView t;
    private TextView EditStepDistance;
    private float StepDistance =80;
    private LocationManager locationManager;
    private LocationListener listener;
    private Location PreviousCords;
    private SensorManager SensorManager;
    private Sensor Accel;
    private float TotalDistance=0;
    private int GPSCount = 0;
    private int StepCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t = (TextView) findViewById(R.id.TxtViewCords);
        bCalibrate = (Button) findViewById(R.id.ButCalibrate);
        EditStepDistance =(TextView) findViewById(R.id.TxtViewStepDistance);
        Start = (Button) findViewById(R.id.ButStart);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SensorManager =(SensorManager)getSystemService(SENSOR_SERVICE);
        Accel = SensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        SensorManager.registerListener(this, Accel, SensorManager.SENSOR_DELAY_FASTEST);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (PreviousCords != null) {
                    float[] DistanceResults = new float[3];
                    Location.distanceBetween(PreviousCords.getLatitude(),PreviousCords.getLongitude(),location.getLatitude(),location.getLongitude(),DistanceResults);
                    TotalDistance =+ DistanceResults[0];
                    t.append("\n"+ String.valueOf(TotalDistance) + " meters");
                }
                PreviousCords = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        configure_GPS();

    }    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configure_GPS();
                break;
            default:
                break;
        }
    }

    void configure_GPS(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        bCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                try{
                Thread.sleep(3000);}catch(Exception e){
                }
                t.setText("");
                StepCount = 0;
                do{
                locationManager.requestLocationUpdates("gps", 10000, 0, listener);
                GPSCount++;
                }
                while(GPSCount <= 6);
                StepDistance = TotalDistance/StepCount;
                EditStepDistance.setText(String.valueOf(TotalDistance));
            }
        });
        Start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                TotalDistance =0;
                t.setText("");
                Toast.makeText(MainActivity.this,String.valueOf(StepCount),Toast.LENGTH_LONG).show();
                TotalDistance = TotalDistance+(StepDistance*StepCount);
                t.setText("currently traveled " + String.valueOf(TotalDistance) + "Cm. \n in " + String.valueOf(StepCount) + " steps");

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
            StepCount++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

