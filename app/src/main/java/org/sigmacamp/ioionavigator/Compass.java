package org.sigmacamp.ioionavigator;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Compass implements SensorEventListener {

    static float declination = -13; //magnetic declination. +=east, -=west
    private Float azimut;  // current azimuth
    private float[] gravity, magneticField; //keeps averaged value
    private SensorManager mSensorManager;
    private Context parent;
    Sensor accelerometer;
    Sensor magnetometer;

    //constructor
    public Compass(Context mContext) {
        this.parent = mContext;
        this.mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        gravity = new float[3];
        magneticField = new float[3];
    }


    protected void onResume() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        int i;
        float[] instantGravity;
        float[] instantMagnetic;
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            instantGravity = event.values.clone();
            //do the averaging = low pass filter
            for (i = 0; i < 3; i++) {
                gravity[i] = 0.7f * gravity[i] + 0.3f * instantGravity[i];
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            instantMagnetic = event.values.clone();
            //do the averaging = low pass filter
            for (i = 0; i < 3; i++) {
                magneticField[i] = 0.7f * magneticField[i] + 0.3f * instantMagnetic[i];
            }
        }

        float R[] = new float[9];
        float I[] = new float[9];
        boolean success = SensorManager.getRotationMatrix(R, I, gravity, magneticField);
        if (success) {
            float orientation[] = new float[3];
            SensorManager.getOrientation(R, orientation);
            azimut = orientation[0] * 360 / (2 * 3.14159f); // orientation contains: azimut, pitch and roll
            azimut = declination + azimut; //correct for magnetic declination
            if (azimut <= 0) {
                azimut += 360;
            }
            if (azimut > 360) {
                azimut -= 360;
            }
        }
    }

    public float getAzimut() {
        if (azimut != null) {
            return azimut;
        } else {
            return ((float) 0.0);
        }
    }
}
