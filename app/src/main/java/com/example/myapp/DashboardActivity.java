package com.example.myapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 101;

    private FirebaseAuth mAuth;
    private TextView tvWelcome, tvHelloUser;
    private Button btnBluetoothOn, btnBluetoothOff;
    private BluetoothAdapter bluetoothAdapter;

    private boolean userRequestedEnable = false;
    private boolean userRequestedDisable = false;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private float[] gravity, geomagnetic;
    private ImageView imgCompass;
    private TextView tvCoordinates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        Button btnLogout = findViewById(R.id.btnLogout);
        btnBluetoothOn = findViewById(R.id.btnBluetoothOn);
        btnBluetoothOff = findViewById(R.id.btnBluetoothOff);
        Button btnPairedDevices = findViewById(R.id.btnPairedDevices);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvHelloUser = findViewById(R.id.tvHelloUser);

        updateBluetoothButtonStates();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        tvWelcome.setText(currentUser != null ?
                "Welcome, " + (currentUser.getEmail() != null ? currentUser.getEmail() : "User") :
                "Welcome, Guest");

        tvHelloUser.setText("Hello User");

        btnBluetoothOn.setOnClickListener(v -> {
            userRequestedEnable = true;
            userRequestedDisable = false;

            if (!bluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (hasBluetoothPermissions()) {
                        promptEnableBluetooth();
                    } else {
                        requestBluetoothPermissions();
                    }
                } else {
                    promptEnableBluetooth();
                }
            } else {
                Toast.makeText(this, "Bluetooth is already ON", Toast.LENGTH_SHORT).show();
            }
        });

        btnBluetoothOff.setOnClickListener(v -> {
            if (bluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+: Open Bluetooth settings
                    Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(this, "Please turn off Bluetooth manually", Toast.LENGTH_SHORT).show();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (hasBluetoothPermissions()) {
                        disableBluetooth();
                    } else {
                        requestBluetoothPermissions();
                    }
                } else {
                    disableBluetooth();
                }
            } else {
                Toast.makeText(this, "Bluetooth is already OFF", Toast.LENGTH_SHORT).show();
            }
        });

        btnPairedDevices.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (hasBluetoothPermissions()) {
                    startActivity(new Intent(this, PairedDevicesActivity.class));
                } else {
                    requestBluetoothPermissions();
                }
            } else {
                startActivity(new Intent(this, PairedDevicesActivity.class));
            }
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });
    }

    private boolean hasBluetoothPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                },
                BLUETOOTH_PERMISSION_REQUEST_CODE);
    }

    private void promptEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE);
    }

    private void disableBluetooth() {
        try {
            bluetoothAdapter.disable();
            Toast.makeText(this, "Turning Bluetooth OFF...", Toast.LENGTH_SHORT).show();
            updateBluetoothButtonStates();
        } catch (SecurityException e) {
            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBluetoothButtonStates() {
        if (bluetoothAdapter == null) return;

        boolean isEnabled = bluetoothAdapter.isEnabled();
        btnBluetoothOn.setEnabled(!isEnabled);
        btnBluetoothOff.setEnabled(isEnabled);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        updateBluetoothButtonStates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBluetoothButtonStates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (userRequestedEnable) {
                    promptEnableBluetooth();
                } else if (userRequestedDisable) {
                    disableBluetooth();
                }
            } else {
                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BLUETOOTH_REQUEST_CODE) {
            if (bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Bluetooth turned ON", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth not turned ON", Toast.LENGTH_SHORT).show();
            }
            updateBluetoothButtonStates();
        }
    }
}