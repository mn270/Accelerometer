package com.example.accelerometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity<_> extends AppCompatActivity implements SensorEventListener {

    private static final int N_SAMPLES = 128;
    private static List<Float> acc_bodyX;
    private static List<Float> acc_bodyY;
    private static List<Float> acc_bodyZ;
    private static List<Float> gro_bodyX;
    private static List<Float> gro_bodyY;
    private static List<Float> gro_bodyZ;
    private static List<Float> acc_X;
    private static List<Float> acc_Y;
    private static List<Float> acc_Z;
    private static List<Float> acc_bodyX_new;
    private static List<Float> acc_bodyY_new;
    private static List<Float> acc_bodyZ_new;
    private static List<Float> gro_bodyX_new;
    private static List<Float> gro_bodyY_new;
    private static List<Float> gro_bodyZ_new;
    private static List<Float> acc_X_new;
    private static List<Float> acc_Y_new;
    private static List<Float> acc_Z_new;
    private static List<Float> acc_bodyX_old;
    private static List<Float> acc_bodyY_old;
    private static List<Float> acc_bodyZ_old;
    private static List<Float> gro_bodyX_old;
    private static List<Float> gro_bodyY_old;
    private static List<Float> gro_bodyZ_old;
    private static List<Float> acc_X_old;
    private static List<Float> acc_Y_old;
    private static List<Float> acc_Z_old;

    private static Memory mem_acc_X = new Memory();
    private static Memory mem_acc_Y = new Memory();
    private static Memory mem_acc_Z = new Memory();
    private static Memory mem_acc_bodyX = new Memory();
    private static Memory mem_acc_bodyY = new Memory();
    private static Memory mem_acc_bodyZ = new Memory();
    private static Memory mem_gro_bodyX = new Memory();
    private static Memory mem_gro_bodyY = new Memory();
    private static Memory mem_gro_bodyZ = new Memory();


    private Long lastAccTimer = 0L;
    private Long lastGyroTimer = 0L;
    private Long lastAcc_bodyTimer = 0L;
    private Long startTime = 0L;

    int walk = 0;
    int walk_up = 0;
    int walk_down = 0;
    int sit = 0;
    int stand = 0;
    int lay = 0;

    private float[][] results;
    private Classifier classifier;

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    Sensor accelerometer,gyroscope_body,accelerometer_body;
    TextView xValue,yValue,zValue,xLinearValue,yLinearValue,zLinearValue,roll,pitch,yaw,walking,walking_up,walking_down,sitting,standing,laying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xValue = (TextView) findViewById(R.id.x);
        yValue = (TextView) findViewById(R.id.y);
        zValue = (TextView) findViewById(R.id.z);
        xLinearValue = (TextView) findViewById(R.id.acc_body_x);
        yLinearValue = (TextView) findViewById(R.id.acc_body_y);
        zLinearValue = (TextView) findViewById(R.id.acc_body_z);
        roll = (TextView) findViewById(R.id.gyro_body_x);
        pitch = (TextView) findViewById(R.id.gyro_body_y);
        yaw = (TextView) findViewById(R.id.gyro_body_z);
        walking = (TextView) findViewById(R.id.walking);
        walking_up = (TextView) findViewById(R.id.walking_up);
        walking_down = (TextView) findViewById(R.id.walking_down);
        sitting = (TextView) findViewById(R.id.sitting);
        standing = (TextView) findViewById(R.id.standing);
        laying = (TextView) findViewById(R.id.laying);

        acc_bodyX = new ArrayList<>();
        acc_bodyY = new ArrayList<>();
        acc_bodyZ = new ArrayList<>();
        gro_bodyX = new ArrayList<>();
        gro_bodyY = new ArrayList<>();
        gro_bodyZ = new ArrayList<>();
        acc_X = new ArrayList<>();
        acc_Y = new ArrayList<>();
        acc_Z = new ArrayList<>();
        acc_bodyX_new = new ArrayList<>();
        acc_bodyY_new = new ArrayList<>();
        acc_bodyZ_new = new ArrayList<>();
        gro_bodyX_new = new ArrayList<>();
        gro_bodyY_new = new ArrayList<>();
        gro_bodyZ_new = new ArrayList<>();
        acc_X_new = new ArrayList<>();
        acc_Y_new = new ArrayList<>();
        acc_Z_new = new ArrayList<>();
        acc_bodyX_old = new ArrayList<>();
        acc_bodyY_old = new ArrayList<>();
        acc_bodyZ_old = new ArrayList<>();
        gro_bodyX_old = new ArrayList<>();
        gro_bodyY_old = new ArrayList<>();
        gro_bodyZ_old = new ArrayList<>();
        acc_X_old = new ArrayList<>();
        acc_Y_old = new ArrayList<>();
        acc_Z_old = new ArrayList<>();

        for(int i = 0;i<N_SAMPLES/2;i++){
            acc_bodyX_old.add(0.f);
            acc_bodyY_old.add(0.f);
            acc_bodyZ_old.add(0.f);
            gro_bodyX_old.add(0.f);
            gro_bodyY_old.add(0.f);
            gro_bodyZ_old.add(0.f);
            acc_X_old.add(0.f);
            acc_Y_old.add(0.f);
            acc_Z_old.add(0.f);
        }


        Log.d(TAG, "onCreate: Initialize Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope_body = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometer_body = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, 20);
            Log.d(TAG, "onCreate: Registered accelerometer listener");
        } else {
            xValue.setText("Accelerometer not Supported");
        }

        if (accelerometer_body != null) {
            sensorManager.registerListener(this, accelerometer_body,20);   // SensorManager.SENSOR_DELAY_NORMAL
            Log.d(TAG, "onCreate: Registered accelerometer_body listener");
        } else {
            xLinearValue.setText("Accelerometer not Supported");
        }

        if (gyroscope_body != null) {
            sensorManager.registerListener(this, gyroscope_body, 20);
            Log.d(TAG, "onCreate: Registered gyroscope listener");
        } else {
            xValue.setText("Gyro not Supported");
        }

        try {
            classifier = new Classifier(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (savedInstanceState != null) {
            walk = savedInstanceState.getInt("walk");
            walk_up = savedInstanceState.getInt("walk_up");
            walk_down = savedInstanceState.getInt("walk_down");
            sit = savedInstanceState.getInt("sit");
            stand = savedInstanceState.getInt("stand");
            lay = savedInstanceState.getInt("lay");

        }
    }





    @Override
    public void onSensorChanged(SensorEvent sensorEvent){
        long currentTime = (new Date()).getTime() + (sensorEvent.timestamp - System.nanoTime()) / 1000000L;
        Sensor sensor = sensorEvent.sensor;

        walking.setText("Walk: " + walk);
        walking_up.setText("Walking up: " + walk_up);
        walking_down.setText("Walking down: "+walk_down);
        sitting.setText("Sitting: "+sit);
        standing.setText("Standing: "+stand);
        laying.setText("Laying: "+lay);

//        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
//            Log.d(TAG, "onSensorChanged: X: "+ sensorEvent.values[0]+"Y: "+sensorEvent.values[1]+"Z: "+sensorEvent.values[2]);
//            if(lastAccTimer == 0 && acc_X.size() < N_SAMPLES){
//                lastAccTimer =  currentTime;
//                startTime = currentTime;
//                acc_X.add(sensorEvent.values[0]);
//                acc_Y.add(sensorEvent.values[1]);
//                acc_Z.add(sensorEvent.values[2]);
//                xValue.setText("xValue: "+ sensorEvent.values[0]);
//                yValue.setText("yValue: "+ sensorEvent.values[1]);
//                zValue.setText("zValue: "+ sensorEvent.values[2]);
//            }else {
//                long timeDifference = currentTime - lastAccTimer;
//                if (timeDifference >= 20 && acc_X.size() < N_SAMPLES) {
//                    lastAccTimer = currentTime;
//                    acc_X.add(sensorEvent.values[0]);
//                    acc_Y.add(sensorEvent.values[1]);
//                    acc_Z.add(sensorEvent.values[2]);
//                    xValue.setText("xValue: " + sensorEvent.values[0]);
//                    yValue.setText("yValue: " + sensorEvent.values[1]);
//                    zValue.setText("zValue: " + sensorEvent.values[2]);
//                }
//            }
//        }else if(sensor.getType() == Sensor.TYPE_GYROSCOPE){
//            Log.d(TAG, "onSensorChangedGYRO: X: "+ sensorEvent.values[0]+"Y: "+sensorEvent.values[1]+"Z: "+sensorEvent.values[2]);
//
//            if(lastGyroTimer == 0 && gro_bodyX.size() < N_SAMPLES){
//                lastGyroTimer =  currentTime;
//                startTime = currentTime;
//                gro_bodyX.add(sensorEvent.values[0]);
//                gro_bodyY.add(sensorEvent.values[1]);
//                gro_bodyZ.add(sensorEvent.values[2]);
//                roll.setText("roll: "+ sensorEvent.values[0]);
//                pitch.setText("pitch: "+ sensorEvent.values[1]);
//                yaw.setText("yaw: "+ sensorEvent.values[2]);
//            }else {
//                long timeDifference = currentTime - lastGyroTimer;
//                if (timeDifference >= 20 && gro_bodyX.size() < N_SAMPLES) {
//                    lastGyroTimer = currentTime;
//                    gro_bodyX.add(sensorEvent.values[0]);
//                    gro_bodyY.add(sensorEvent.values[1]);
//                    gro_bodyZ.add(sensorEvent.values[2]);
//                    roll.setText("roll: "+ sensorEvent.values[0]);
//                    pitch.setText("pitch: "+ sensorEvent.values[1]);
//                    yaw.setText("yaw: "+ sensorEvent.values[2]);
//                }
//            }
//        }else if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
//            Log.d(TAG, "onSensorChangedACCL: X: "+ sensorEvent.values[0]+"Y: "+sensorEvent.values[1]+"Z: "+sensorEvent.values[2]);
//
//            if(lastAcc_bodyTimer == 0 && acc_bodyX.size() < N_SAMPLES){
//                lastAcc_bodyTimer =  currentTime;
//                startTime = currentTime;
//                acc_bodyX.add(sensorEvent.values[0]);
//                acc_bodyY.add(sensorEvent.values[1]);
//                acc_bodyZ.add(sensorEvent.values[2]);
//                xLinearValue.setText("xLinear: "+ sensorEvent.values[0]);
//                yLinearValue.setText("yValue: "+ sensorEvent.values[1]);
//                zLinearValue.setText("zValue: "+ sensorEvent.values[2]);
//            }else {
//                long timeDifference = currentTime - lastAcc_bodyTimer;
//                if (timeDifference >= 20 && acc_bodyX.size() < N_SAMPLES) {
//                    lastAcc_bodyTimer = currentTime;
//                    acc_bodyX.add(sensorEvent.values[0]);
//                    acc_bodyY.add(sensorEvent.values[1]);
//                    acc_bodyZ.add(sensorEvent.values[2]);
//                    xLinearValue.setText("xLinear: "+ sensorEvent.values[0]);
//                    yLinearValue.setText("yValue: "+ sensorEvent.values[1]);
//                    zLinearValue.setText("zValue: "+ sensorEvent.values[2]);
//                }
//            }
//        }


        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            Log.d(TAG, "onSensorChanged: X: "+ sensorEvent.values[0]+"Y: "+sensorEvent.values[1]+"Z: "+sensorEvent.values[2]);
            if(acc_X.size() < N_SAMPLES){
                acc_X_new.add(sensorEvent.values[0]);
                acc_Y_new.add(sensorEvent.values[1]);
                acc_Z_new.add(sensorEvent.values[2]);
                if (acc_X_new.size() == N_SAMPLES/2){
                    acc_X.addAll(acc_X_old);
                    acc_Y.addAll(acc_Y_old);
                    acc_Z.addAll(acc_Z_old);
                    acc_X.addAll(acc_X_new);
                    acc_Y.addAll(acc_Y_new);
                    acc_Z.addAll(acc_Z_new);
                    acc_X_old.clear();
                    acc_Y_old.clear();
                    acc_Z_old.clear();
                    acc_X_old.addAll(acc_X_new);
                    acc_Y_old.addAll(acc_Y_new);
                    acc_Z_old.addAll(acc_Z_new);
                    acc_X_new.clear();
                    acc_Y_new.clear();
                    acc_Z_new.clear();
                }
                xValue.setText("xValue: "+ sensorEvent.values[0]);
                yValue.setText("yValue: "+ sensorEvent.values[1]);
                zValue.setText("zValue: "+ sensorEvent.values[2]);
            }
        }
        else if(sensor.getType() == Sensor.TYPE_GYROSCOPE){
            Log.d(TAG, "onSensorChangedGYRO: X: "+ sensorEvent.values[0]+"Y: "+sensorEvent.values[1]+"Z: "+sensorEvent.values[2]);
            if(gro_bodyX.size() < N_SAMPLES){
                gro_bodyX_new.add(sensorEvent.values[0]);
                gro_bodyY_new.add(sensorEvent.values[1]);
                gro_bodyZ_new.add(sensorEvent.values[2]);
                if (gro_bodyX_new.size() == N_SAMPLES/2){
                    gro_bodyX.addAll(gro_bodyX_old);
                    gro_bodyY.addAll(gro_bodyX_old);
                    gro_bodyZ.addAll(gro_bodyZ_old);
                    gro_bodyX.addAll(gro_bodyX_new);
                    gro_bodyY.addAll(gro_bodyY_new);
                    gro_bodyZ.addAll(gro_bodyZ_new);
                    gro_bodyX_old.clear();
                    gro_bodyY_old.clear();
                    gro_bodyZ_old.clear();
                    gro_bodyX_old.addAll(gro_bodyX_new);
                    gro_bodyY_old.addAll(gro_bodyY_new);
                    gro_bodyZ_old.addAll(gro_bodyZ_new);
                    gro_bodyX_new.clear();
                    gro_bodyY_new.clear();
                    gro_bodyZ_new.clear();
                }
                roll.setText("roll: "+ sensorEvent.values[0]);
                pitch.setText("pitch: "+ sensorEvent.values[1]);
                yaw.setText("yaw: "+ sensorEvent.values[2]);
            }
        }else if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                    Log.d(TAG, "onSensorChangedACCL: X: " + sensorEvent.values[0] + "Y: " + sensorEvent.values[1] + "Z: " + sensorEvent.values[2]);
                    if (acc_bodyX.size() < N_SAMPLES ) {
                        acc_bodyX_new.add(sensorEvent.values[0]);
                        acc_bodyY_new.add(sensorEvent.values[1]);
                        acc_bodyZ_new.add(sensorEvent.values[2]);
                        if (acc_bodyX_new.size() == N_SAMPLES/2){
                            acc_bodyX.addAll(acc_bodyX_old);
                            acc_bodyY.addAll(acc_bodyY_old);
                            acc_bodyZ.addAll(acc_bodyZ_old);
                            acc_bodyX.addAll(acc_bodyX_new);
                            acc_bodyY.addAll(acc_bodyY_new);
                            acc_bodyZ.addAll(acc_bodyZ_new);
                            acc_bodyX_old.clear();
                            acc_bodyY_old.clear();
                            acc_bodyZ_old.clear();
                            acc_bodyX_old.addAll(acc_bodyX_new);
                            acc_bodyY_old.addAll(acc_bodyY_new);
                            acc_bodyZ_old.addAll(acc_bodyZ_new);
                            acc_bodyX_new.clear();
                            acc_bodyY_new.clear();
                            acc_bodyZ_new.clear();
                        }
                        xLinearValue.setText("xLinear: " + sensorEvent.values[0]);
                        yLinearValue.setText("yValue: " + sensorEvent.values[1]);
                        zLinearValue.setText("zValue: " + sensorEvent.values[2]);
                    }
        }

        activityPrediction();
    }



    private void activityPrediction() {
        if (acc_X.size() == N_SAMPLES && acc_Y.size() == N_SAMPLES && acc_Z.size() == N_SAMPLES
                && gro_bodyX.size() == N_SAMPLES && gro_bodyY.size() == N_SAMPLES && gro_bodyZ.size() == N_SAMPLES
                && acc_bodyX.size() == N_SAMPLES && acc_bodyY.size() == N_SAMPLES && acc_bodyZ.size() == N_SAMPLES) {

            List<Float> data = new ArrayList<>();

            filter(acc_X,mem_acc_X);
            filter(acc_Y,mem_acc_Y);
            filter(acc_Z,mem_acc_Z);
            filter(acc_bodyX,mem_acc_bodyX);
            filter(acc_bodyY,mem_acc_bodyY);
            filter(acc_bodyZ,mem_acc_bodyZ);
            filter(gro_bodyX,mem_gro_bodyX);
            filter(gro_bodyY,mem_gro_bodyY);
            filter(gro_bodyZ,mem_gro_bodyZ);

            scaler(acc_X,8.04559187e-01f,0.41436113f);
            scaler(acc_Y,2.88250082e-02f,0.39101538f);
            scaler(acc_Z,8.61406649e-02f,0.35805633f);
            scaler(acc_bodyX,-1.24103420e-04f,0.1945598f);
            scaler(acc_bodyY,-2.39053082e-04f,0.12134089f);
            scaler(acc_bodyZ,-6.47850079e-04f,0.10539762f);
            scaler(gro_bodyX,-1.06375868e-04f,0.40094169f);
            scaler(gro_bodyY,-1.33460476e-03f,0.3803926f);
            scaler(gro_bodyZ,2.26898315e-04f, 0.25387354f);

            data.addAll(acc_X);
            data.addAll(acc_Y);
            data.addAll(acc_Z);
            data.addAll(acc_bodyX);
            data.addAll(acc_bodyY);
            data.addAll(acc_bodyZ);
            data.addAll(gro_bodyX);
            data.addAll(gro_bodyY);
            data.addAll(gro_bodyZ);

            Result resu;

            results = classifier.predictProbabilities(toFloatArray(data));
            resu = new Result(results);

            switch(resu.getNumber()){
                case 0:
                   walk++;
                    break;
                case 1:
                    walk_up++;
                    break;
                case 2:
                    walk_down++;
                    break;
                case 3:
                    sit++;
                    break;
                case 4:
                    stand++;
                    break;
                case 5:
                    lay++;
                    break;
                default:
                   break;
            }

            acc_X.clear();
            acc_Y.clear();
            acc_Z.clear();
            acc_bodyX.clear();
            acc_bodyY.clear();
            acc_bodyZ.clear();
            gro_bodyX.clear();
            gro_bodyY.clear();
            gro_bodyZ.clear();

            startTime = 0L;
            lastAccTimer = 0L;
            lastAcc_bodyTimer = 0L;
            lastGyroTimer = 0L;

        }
    }

    private float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

    // StandardScaler std - standard deviation
    private static void scaler (List<Float> input,float mean, float std){
        for (int i = 0; i<128; i++){
                input.set(i,(input.get(i)/9.80665f-mean)/std);
        }
    }

    private static void filter( List<Float> input, Memory mem){
        float[] a = new float [] {1, -2.79902201f, 2.6177355f, -0.81779236f};
        float[] b = new float [] {0.00011514f, 0.00034542f, 0.00034542f, 0.00011514f};
        List<Float> output;
        List<Float> x;
        List<Float> window;
        output = new ArrayList<>();
        x = new ArrayList<>();
        window = new ArrayList<>();
        x.add(mem.getX(0));
        x.add(mem.getX(1));
        x.addAll(input);
        x.add(input.get(127));
        x.add(input.get(127));
        //Median Filter
        for (int i = 0; i<128; i++){
            for (int j = 0; j < 5; j++){
                window.add(x.get(i+j));
            }
            Collections.sort(window);
            output.add(window.get(2));
            window.clear();
        }
        x.clear();
        x.add(mem.getX(0));
        x.add(mem.getX(1));
        x.add(mem.getX(2));
        x.addAll(output);
        output.clear();
        output.add(mem.getY(0));
        output.add(mem.getY(1));
        output.add(mem.getY(2));
        // 3rd order low pass Butterworth filter with a corner frequency of 20 Hz to remove noise
        for (int i = 0; i<128;i++){
           // output.add(b[0]*x.get(3+i)+b[1]*x.get(2+i)+b[2]*x.get(1+i)+b[3]*x.get(i)-a[1]*output.get(i+2)-a[2]*output.get(i+1)-a[3]*output.get(i));
            output.add(x.get(i));

        }

        input.clear();
        input.addAll(output.subList(3,131));
        for (int i=0;i < 3; i++){
            mem.set(i,input.get(64+i),input.get(65+i));
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        sensorManager.unregisterListener(this);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 20);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_GYROSCOPE), 20);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 20);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("walk",walk);
        outState.putInt("walk_up",walk_up);
        outState.putInt("walk_down",walk_down);
        outState.putInt("sit",sit);
        outState.putInt("stand",stand);
        outState.putInt("lay",lay);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }
    protected void onResume() {
        super.onResume();
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 20);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_GYROSCOPE), 20);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 20);


    }

    private SensorManager getSensorManager() {
        return (SensorManager) getSystemService(SENSOR_SERVICE);
    }



}
