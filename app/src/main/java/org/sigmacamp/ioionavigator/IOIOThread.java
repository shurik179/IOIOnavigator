package org.sigmacamp.ioionavigator;



        import ioio.lib.api.DigitalOutput;
        import ioio.lib.api.IOIO;
        import ioio.lib.api.IOIO.VersionType;
        import ioio.lib.api.PulseInput;
        import ioio.lib.api.PwmOutput;
        import ioio.lib.api.exception.ConnectionLostException;
        import ioio.lib.util.BaseIOIOLooper;



public class IOIOThread extends BaseIOIOLooper {
    private DigitalOutput led0, ledleft, ledright;
    private Motor rightMotor, leftMotor;
    private Sonar leftSonar, rightSonar;
    public MainActivity parent;
    private Compass compass;
    boolean heartBeat=false; //used for built-in LED, as heartbeat
    int mode=0; //0: going straight; 1: turning right; 2: turning left
    //constructor
    IOIOThread(MainActivity p) {
        super();
        this.parent = p;
    }

    /**
     * Called when the IOIO is disconnected.
     */
    @Override
    public void disconnected() {
        parent.popup("IOIO disconnected");
    }

    /**
     * Called when the IOIO is connected, but has an incompatible firmware version.
     */
    @Override
    public void incompatible() {
        parent.popup("IOIO: Incompatible firmware version!");
    }

    /**
     * Setup - called when the IOIO is connected
     */
    @Override
    protected void setup() throws ConnectionLostException {
        //print connection information
        parent.popup(String.format("IOIO connected\n" +
                        "IOIOLib: %s\n" +
                        "Application firmware: %s\n" +
                        "Bootloader firmware: %s\n" +
                        "Hardware: %s",
                ioio_.getImplVersion(VersionType.IOIOLIB_VER),
                ioio_.getImplVersion(VersionType.APP_FIRMWARE_VER),
                ioio_.getImplVersion(VersionType.BOOTLOADER_VER),
                ioio_.getImplVersion(VersionType.HARDWARE_VER)));
        //set up various LEDs etc
        led0 = ioio_.openDigitalOutput(0, true);
        ledleft = ioio_.openDigitalOutput(40, true);
        ledright = ioio_.openDigitalOutput(39, true);
        rightMotor=new Motor(13,14);
        leftMotor=new Motor(11,12);
        leftSonar=new Sonar(7,6);//trigger pin=7, echo pin=6
        rightSonar=new Sonar(9,10);//trigger pin=7, echo pin=6
        compass=parent.compass;
    }

    /**
     * Called repetitively while the IOIO is connected.
     */
    @Override
    public void loop() throws ConnectionLostException, InterruptedException {
        float leftDistance, rightDistance, heading, error;
        boolean obstacleleft, obstacleright;
        //pulse the built in LED
        led0.write(heartBeat);
        heartBeat=!heartBeat;
        //get heading
        if (!parent.startButton.isChecked()) {
            heading = compass.getAzimut();
            error = heading - 180;
            leftMotor.setPower(50 - error * 0.3f);
            rightMotor.setPower(50 + error * 0.3f);
        } else {
            leftMotor.setPower(0f);
            rightMotor.setPower(0f);
        }
        Thread.sleep(50);
    }
    private class Motor {
        private PwmOutput pin1, pin2;
        //constructor
        //requires 2 arguments: pin numbers
        public Motor( int  n1, int n2) throws ConnectionLostException {
            pin1 = ioio_.openPwmOutput(n1, 5000); //opens as PWM output, frequency =5Khz
            pin2 = ioio_.openPwmOutput(n2, 5000);
        }

        public void setPower(float power) throws ConnectionLostException {
            //make sure power is between -100 ... 100
            if (power > 100) {
                power = 100;
            } else if (power < -100) {
                power = -100;
            }
            if (power > 0) {
                pin1.setDutyCycle(power / 100);
                pin2.setDutyCycle(0);
            } else {
                pin1.setDutyCycle(0);
                pin2.setDutyCycle(Math.abs(power) / 100);
            }
        }

        public void stop() throws ConnectionLostException {
            pin1.setDutyCycle(0);
            pin2.setDutyCycle(0);
        }
    }

    /*
  Class sonar. Uses ultrasonic sensor to measure distance.
 */
    private class Sonar {
        private DigitalOutput triggerPin;
        private PulseInput echoPin;
        //constructor
        //requires 2 arguments: trigger pin number, echo pin number (echo must be 5V tolerant)
        public Sonar(int  trigger, int echo) throws ConnectionLostException{
            echoPin = ioio_.openPulseInput(echo, PulseInput.PulseMode.POSITIVE);
            triggerPin = ioio_.openDigitalOutput(trigger);
        }
        //measure distance, in cm. Returns -1 if connection lost.
        public float getDistance() throws ConnectionLostException, InterruptedException {
            int echoMseconds;
            triggerPin.write(false);
            Thread.sleep(5);
            // set trigger pin to HIGH for 1 ms
            triggerPin.write(true);
            Thread.sleep(1);
            triggerPin.write(false);
            //get response time in microseconds
            echoMseconds = (int) (echoPin.getDuration() * 1000 * 1000);
            //convert microseconds to cm
            return ((float) echoMseconds / 58);
        }
    }


}
