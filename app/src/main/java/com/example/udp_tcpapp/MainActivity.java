package com.example.udp_tcpapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private EditText ip1, udpport;
    private TextView lat, lon, hora1, fech, usuario1;
    private Socket user;
    private Button bluetoothON, visibilidad1, viewDevices;
    private String smsave;
    private DatagramSocket socketudp;
    private Spinner placa1;
    public String coords;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private final long MIN_TIME = 4000;
    private final long MIN_DIST = 0;
    public LatLng latLng;

    private static final String TAG = "MainActivity";
    BluetoothAdapter mBluetoothAdapter;
    //public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    //public DeviceListAdapter mDeviceListAdapter;
    //ListView listDevices;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();       //hold BluetoothDevices that are discovered
    public DeviceListAdapter mDeviceListAdapter;
    public ListView lvNewDevices;


    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onRecive: Estado OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onRecive:   TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onRecive: Estado ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onRecive: TURNING ON");
                        break;
                }
            }

        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, mBluetoothAdapter.ERROR);

                switch (mode){
                    //Dispositivo en modo visible
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadCastReceiver2: Discoverability enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadCastReceiver2: Discoverability disable. Able to connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadCastReceiver2: Discoverability disable. No able to connections");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadCastReceiver2: CONNECTING...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadCastReceiver2: CONNECTED");
                        break;
                }
            }

        }
    };
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "Recibiendo: "+device.getName() + " : " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }

        }
    };
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //3 casos

                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BOND_BONDED");
                }
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BOND_BONDING");
                }
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BOND_NONE");
                }

            }

        }
    };
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fech = findViewById(R.id.fecha1);
        hora1 = findViewById(R.id.hora);
        lon = findViewById(R.id.long1);
        lat = findViewById(R.id.lat1);
        //p1 = findViewById(R.id.ip);
        //udpport = findViewById(R.id.port2);
        //usuario1 = findViewById(R.id.usuario);
        placa1 = findViewById(R.id.placa);
        bluetoothON = findViewById(R.id.bluetooth);
        visibilidad1 = findViewById(R.id.visibilidad);
        viewDevices = findViewById(R.id.viewDevices1);
        lvNewDevices = findViewById(R.id.ListDevices1);
        mBTDevices = new ArrayList<>();


        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, PackageManager.PERMISSION_GRANTED);

        //Para alertar si se tiene el GPS apagado

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = true;
        boolean network_enabled = true;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(MainActivity.this).setMessage("El GPS está apagado").setNegativeButton( "Ok" , null ).show() ;
        }


        //Poner datos en el spinner
        String [] placas = {"WXA834", "TLO847", "GRS523", "WPB289", "TRU189"};
        ArrayAdapter <String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, placas);
        placa1.setAdapter(adapter);


        locationListener = new LocationListener() {


            //Para que la app no se cierre si el GPS está apagado
            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }
            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {

                    Date d = new Date();

                    CharSequence fecha = DateFormat.format("yyyy-MM-dd", d.getTime());
                    String fecha_ = String.valueOf(fecha);

                    latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    Long time = location.getTime();
                    Date date = new Date(time);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                    String h1 = sdf.format(date);

                    Log.i("tiempo", "tiempo: " + h1);

                    String myLatitude = String.valueOf(location.getLatitude());
                    String myLongitude = String.valueOf(location.getLongitude());
                    String seleccion = placa1.getSelectedItem().toString();

                    String nplaca = "";

                    if (seleccion.equals("WXA834")){
                         nplaca = "WXA834";
                    } else  if (seleccion.equals("TLO847")) {
                         nplaca = "TLO847";
                    } else  if (seleccion.equals("GRS523")) {
                         nplaca = "GRS523";
                    } else  if (seleccion.equals("WPB289")) {
                         nplaca = "WPB289";
                    } else  if (seleccion.equals("TRU189")) {
                         nplaca = "TRU189";
                    }

                    coords = myLatitude + "," + myLatitude + "," + myLongitude + "," + h1 + "," + fecha_ + "," + fecha_ + "," + nplaca + "," + nplaca;
                    Log.i("Coords", "Coords: " + coords);

                    lat.setText(myLatitude);
                    lon.setText(myLongitude);
                    hora1.setText(h1);
                    fech.setText(fecha_);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (coords != null) {

                    byte[] buffer;
                    String puerto = "8051";
                    int port = Integer.parseInt(puerto);

                    try {
                        // Para UDP Daritza
                        DatagramSocket socketudp = new DatagramSocket();

                        try {

                            String direccion = "18.117.138.175";
                            InetAddress address = InetAddress.getByName(direccion);
                            buffer = coords.getBytes();

                            DatagramPacket peticion = new DatagramPacket(buffer, buffer.length, address, port);
                            socketudp.send(peticion);
                            Log.i("Confirmation", "Packet Sent!");

                        }catch(NumberFormatException ex){

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        // Para UDP Diana
                        DatagramSocket socketudp = new DatagramSocket();

                        try {

                            String direccion = "3.129.148.31";
                            InetAddress address = InetAddress.getByName(direccion);
                            buffer = coords.getBytes();

                            DatagramPacket peticion = new DatagramPacket(buffer, buffer.length, address, port);
                            socketudp.send(peticion);
                            Log.i("Confirmation", "Packet Sent!");

                        }catch(NumberFormatException ex){

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        // Para UDP laura
                        DatagramSocket socketudp = new DatagramSocket();

                        try {

                            String direccion = "18.191.244.71";
                            InetAddress address = InetAddress.getByName(direccion);
                            buffer = coords.getBytes();

                            DatagramPacket peticion = new DatagramPacket(buffer, buffer.length, address, port);
                            socketudp.send(peticion);
                            Log.i("Confirmation", "Packet Sent!");

                        }catch(NumberFormatException ex){

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        // Para UDP Rafael
                        DatagramSocket socketudp = new DatagramSocket();

                        try {

                            String direccion = "18.119.128.230";
                            InetAddress address = InetAddress.getByName(direccion);
                            buffer = coords.getBytes();

                            DatagramPacket peticion = new DatagramPacket(buffer, buffer.length, address, port);
                            socketudp.send(peticion);
                            Log.i("Confirmation", "Packet Sent!");

                        }catch(NumberFormatException ex){

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        //Para UDP John
                        DatagramSocket socketudp = new DatagramSocket();

                        try {

                            String direccion = "18.219.197.225";
                            InetAddress address = InetAddress.getByName(direccion);
                            buffer = coords.getBytes();

                            DatagramPacket peticion = new DatagramPacket(buffer, buffer.length, address, port);
                            socketudp.send(peticion);
                            Log.i("Confirmation", "Packet Sent!");

                        }catch(NumberFormatException ex){

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

            }

        }, 0, 3000);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(MainActivity.this);

        bluetoothON.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(TAG, "onClick: enabling/disabling bluetooth");
                enableDisableBT();
            }
        });

        visibilidad1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(TAG, "Haciendo el dispositivo visible por 300 segundos");
                visibilidad();
            }
        });

        viewDevices.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(TAG, "Haciendo el dispositivo visible por 300 segundos");
                Discover();
            }
        });
    }

    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "El dispositivo no tiene/soporta Bluetooth");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "Enabling Bluetooth");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            //Ver los cambios de estado del bluetooth, si se enciende o apaga externamente
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "disabling Bluetooth");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }
    public void visibilidad(){

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);

    }

    public void Discover(){
        Log.d(TAG, "Buscando dispositivos no emparejados");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "Canceling discovery");

            //Método para revisar permisos en manifest--debe hacerse para Android superior a lollipop
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            //Método para revisar permisos en manifest--debe hacerse para Android superior a lollipop
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }
    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1001);
            }

        }else{
            Log.d(TAG, "No necesita permisos ya que su versión de SDK es inferior a lollipop");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //Cancelar discovery porque consume mucha memoria
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "Diste click en un dispositivo");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "Nombre del dispositivo: " + deviceName);
        Log.d(TAG, "Dirección MAC: " + deviceAddress);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Intentando conectarse con: " + deviceName);
            mBTDevices.get(i).createBond();
        }


    }
}





