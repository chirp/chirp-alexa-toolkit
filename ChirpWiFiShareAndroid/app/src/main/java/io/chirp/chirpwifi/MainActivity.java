package io.chirp.chirpwifi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import io.chirp.connect.ChirpConnect;
import io.chirp.connect.interfaces.ConnectEventListener;
import io.chirp.connect.models.ChirpError;
import io.chirp.connect.models.ConnectState;

public class MainActivity extends AppCompatActivity {

    ChirpConnect chirpConnect;
    Context context;

    TextView status;
    TextView startStopListeningBtn;

    final int RESULT_REQUEST_PERMISSIONS = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        startStopListeningBtn = findViewById(R.id.startStopListening);

        /*
        You can download licence string and credentials from your admin panel at admin.chirp.io
         */
        String KEY = "YOUR_APP_KEY";
        String SECRET = "YOUR_APP_SECRET";
        String LICENCE = "YOUR_APP_LICENCE";

        chirpConnect = new ChirpConnect(this, KEY, SECRET);
        ChirpError setLicenceError = chirpConnect.setLicence(LICENCE);
        if (setLicenceError.getCode() > 0) {
            Log.e("ChirpError:", setLicenceError.getMessage());
        }

        context = this;

        chirpConnect.setListener(new ConnectEventListener() {

            @Override
            public void onSent(byte[] payload) {
                Log.v("chirpConnectDemoApp", "This is called when a payload has been sent " + chirpConnect.payloadToHexString(payload));
            }

            @Override
            public void onSending(byte[] payload) {
                Log.v("chirpConnectDemoApp", "This is called when a payload is being sent " + chirpConnect.payloadToHexString(payload));
            }

            @Override
            public void onReceived(byte[] payload) {
                Log.v("chirpConnectDemoApp", "This is called when a payload has been received " + chirpConnect.payloadToHexString(payload));
                try {
                    String credentialsString = new String(payload, "UTF-8");
                    List<String> credentialsList = Arrays.asList(credentialsString.split(":"));
                    String networkSSID = credentialsList.get(0);
                    String networkPass = credentialsList.get(1);
                    setStatus("Searching for the network...");
                    connectToWifi(networkSSID, networkPass);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    setStatus("Received data but unable to decode credentials...");
                }
            }

            @Override
            public void onReceiving() {
                Log.v("chirpConnectDemoApp", "This is called when the SDK is expecting a payload to be received");
                setStatus("Receiving...");
            }

            @Override
            public void onStateChanged(byte oldState, byte newState) {
                Log.v("chirpConnectDemoApp", "This is called when the SDK state has changed " + oldState + " -> " + newState);
                ConnectState state = ConnectState.createConnectState(newState);
                if (state == ConnectState.AudioStateRunning) {
                    setStatus("Listening...");
                }
            }

            @Override
            public void onSystemVolumeChanged(int old, int current) {
                Log.d("chirpConnectDemoApp", "This is called when the Android system volume has changed " + old + " -> " + current);
            }

        });

    }

    public void learnMore(View view) {
        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://chirp.io"));
        startActivity(intent);
    }

    public void startStopListening(View view) {
        if (chirpConnect.getConnectState() == ConnectState.AudioStateStopped) {
            ChirpError startError = chirpConnect.start();
            if (startError.getCode() > 0) {
                Log.d("startStopListening", startError.getMessage());
            } else {
                setStatus("Listening...");
                startStopListeningBtn.setText("Stop Listening");
            }

        } else {
            ChirpError stopError = chirpConnect.stop();
            if (stopError.getCode() > 0) {
                Log.d("startStopListening", stopError.getMessage());
            } else {
                setStatus("IDLE");
                startStopListeningBtn.setText("Start Listening");
            }
        }
    }

    private void setStatus(final String newStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(newStatus);
            }
        });
    }

    private void connectToWifi(String networkSSID, String networkPass) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        //remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        boolean connected = wifiManager.reconnect();
        if (connected) {
            setStatus("Connecting to " + networkSSID + "...");
            checkConnected(20);
        }
    }

    private void checkConnected(final int repeats) {
        final Handler handler = new Handler();
        final int[] attempts = {repeats};
        final Runnable r = new Runnable() {
            public void run() {
                Log.d("checkConnected", "attempt: " + attempts[0]);
                ConnectivityManager cm =
                        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isConnected = wifiNetwork != null &&
                        wifiNetwork.isConnected();
                if (isConnected) {
                    setStatus("Connected");
                } else if (attempts[0] > 0){
                    attempts[0]--;
                    handler.postDelayed(this, 1000);
                }

            }
        };
        handler.postDelayed(r, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] permissionsToRequest = new String[] {
                Manifest.permission.RECORD_AUDIO
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, RESULT_REQUEST_PERMISSIONS);
        }
    }
}
