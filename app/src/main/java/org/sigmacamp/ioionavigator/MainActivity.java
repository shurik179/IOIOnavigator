package org.sigmacamp.ioionavigator;

        import android.location.Location;
        import android.os.Bundle;
        import ioio.lib.util.android.IOIOActivity;
        import ioio.lib.util.IOIOLooper;
        import android.content.Context;
        import android.widget.Toast;
        import android.widget.ToggleButton;
        import android.widget.TextView;

        import java.util.Timer;
        import java.util.TimerTask;

public class MainActivity extends IOIOActivity {
    public ToggleButton startButton;
    public TextView message;
    private IOIOLooper IOIOcontroller;
    public Gps gps;
    public Compass compass;
    private Timer autoUpdate;


    /**
     * Called when the activity is first created. Here we normally initialize
     * our GUI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //define buttons and othr GUI elements
        startButton = (ToggleButton) findViewById(R.id.toggleButton);
        message=(TextView) findViewById(R.id.textView);
        //create new gps
        gps=new Gps(this);
        // create new compass
        compass=new Compass(this);

    }
    @Override
    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateDisplay();
                    }
                });
            }
        }, 0, 1000); // updates each 1 sec
        gps.resume();
    }
    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
        gps.pause();
    }
    /**
     * create a new IOIO Thread
     */
    @Override
    protected IOIOLooper createIOIOLooper() {
        IOIOcontroller = new IOIOThread(this);
        return IOIOcontroller;
    }

    /*
     *  Create a pop-up ('toast') notification
     *
     */
    public void popup(final String message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
    public void updateDisplay() {
        String latitude, longitude, timestamp;
        Location location;

        if (gps.hasLocation()) {
            location = gps.getLocation();
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            timestamp = String.valueOf(location.getTime());
        } else {
            latitude="";
            longitude="";
            timestamp="";
        }
        message.setText("Longitude: " + longitude
                + "\n Latitude: " + latitude
                + "\n Timestamp: " + timestamp
                + "\n Azimut: " + String.valueOf(compass.getAzimut()));
    }

}