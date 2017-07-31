package com.example.jalfredo.oddccallback;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.sql.Time;
import java.util.Arrays;
import java.util.List;



import android.util.Log;
import android.widget.TextView;

import 	java.sql.Timestamp;


import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


// NeuSoft executes these four lines somewhere to attach ODDC callback to NeuSoftClass
// and NueSoft callback to ODDC
        final ODDCclass oddc = new ODDCclass();
        final NeuSoftClass nsc = new NeuSoftClass();
        nsc.setListener(oddc);
        oddc.setListener(nsc);
    }


    // ODDC callbacks which NeuSoft invokes to send data to ODDC
    public interface ODDCinterface {
        public void onContinuousData(ContinuousData data);
        public List<String> getContinuousLog();
        public List<String> getEvenbtLog();
        public List<String> getOnDemandLog();
        public boolean reqShutdown();
        public boolean ok2Startup();
    }

    // NeuSoft callbacks which ODDC invokes to send data to NeuSoft
    public interface NeuSoftInterface {
        public void onFLAparam(int param); // TBD
    }



// NeuSoft class to which Fujitsu callback interface is attached
// NeuSoft delivers data to Fujitsu by invoking listener.onContinuousData()
    public class NeuSoftClass extends Thread implements NeuSoftInterface {
        private ODDCclass listener;

        public void setListener(ODDCclass listener){
        this.listener = listener;
    }

        public void onFLAparam(int param){} // example only at this time, param(s) TBD

        public NeuSoftClass() {}



        private void someNeuSoftFunctionForSendingData() {
            // NeuSoft prepares data for transfer somewhere in their code

            // for continuous data and event data
            // events are identified as ContinuousData.fcwEvent==true or ContinuousData.ldwEvent==true
            // create data packet and assign values with direct assignment, i.e. cd.longitude = value
            ContinuousData cd = new ContinuousData();
            listener.onContinuousData(cd); // invoke callback to send to ODDC
        }
    }


    // Fujitsu supplied class containing callback functions to receive data from NeuSoft
    public class ODDCclass extends Thread implements ODDCinterface {
        private static final int SENDCOUNT = 10;
        private NeuSoftClass listener;
        private int sentCount = 0;

        public void setListener(NeuSoftClass listener){
            this.listener = listener;
        }

        // NeuSoft waits for boolean return value from reqShutdown before shutdown
        public boolean reqShutdown(){return true;}

        // return value of false indicates some condition exists which should prevent startup
        // otherwise true indicate OK to startup
        public boolean ok2Startup(){return true;}

        public List<String> getContinuousLog(){return Arrays.asList("TBD");}
        public List<String> getEvenbtLog(){return Arrays.asList("TBD");}
        public List<String> getOnDemandLog(){return Arrays.asList("TBD");}

        public void onContinuousData(final ContinuousData data){
            // Fujitsu processing of continuous data received from NeuSoft

            insertSQLite(data);
            if (sentCount == SENDCOUNT){
                sendToFLA();
                sentCount = 0;
                checkFileSpace();
            }
            else sentCount++;

        }

        public boolean insertSQLite(ContinuousData data){
            return  true;
        }
        public void sendToFLA(){} /* code supplied by Yuri */

        public void checkFileSpace(){}

    }


    // Fujitsu supplied class for encapsulating data, testing version shown
    public class ContinuousData {
        public int messageID;
        public String sessionID;
        public String vehicleID; // VIN
        public String driverID;
        public String submitterID;

        public Timestamp gpsTimestamp; // from OS not GPS
        public float longitude;
        public float latitude;
        public float speed;
        public int speedDetectionType;

        public Timestamp accelerationTimeStamp;
        public float accelerationX;
        public float accelerationY;
        public float accelerationZ;

        public Timestamp gShockTimeStamp;
        public boolean gShockEvent;
        //public float gShockEventThreshold; // might be a parameter from FLA

        public Timestamp fcwTimeStamp;
        public boolean fcwExistFV;
        public boolean fcwCutIn;
        public float fcwDistanceToFV;
        public float fcwRelativeSpeedToFV;
        public boolean fcwEvent;
        public float fcwEventThreshold;

        public Timestamp ldwTimeStamp;
        public float ldwDistanceToLeftLane;
        public float ldwDistanceToRightLane;
        public boolean ldwEvent;

        public String mediaURI;
    }
}
