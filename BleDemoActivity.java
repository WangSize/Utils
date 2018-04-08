package com.beikatech.bledemo;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTING;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQ_ENABLE_BLUETOOTH = 0;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> mGattServices;
    private final String TAG = "BLE_LOG";

    //电量service UUID
    private final String BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb";
    //电量characteristics UUID
    private final String BATTERY_CHARACTERISTICS_UUID = "00002a19-0000-1000-8000-00805f9b34fb";
    //按键及app写入数据service UUID
    private final String PRIVATE_SERVICE_UUID = "0000ff10-0000-1000-8000-00805f9b34fb";
    //按键characteristics UUID
    private final String KEYPRESS_CHARACTERISTICS_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    //写入数据characteristics UUID
    private final String WRITE_DATA_UUID = "0000ffe2-0000-1000-8000-00805f9b34fb";
    //电量，读
    private BluetoothGattCharacteristic mBatteryCharac;
    //按键，读
    private BluetoothGattCharacteristic mReadKeypressCharac;
    //写入数据
    private BluetoothGattCharacteristic mWriteDataCharac;

    //关闭设备命令
    private byte[] shutDownCommand = new byte[]{0x21, 0x20, 0x16, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, (byte) 0xFE};

    private TextView mBatteryTv;
    private TextView mKeypressTv;

    private BLEUtil mBLEUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBatteryTv = (TextView) findViewById(R.id.battery_tv);
        mKeypressTv = (TextView) findViewById(R.id.keypress_tv);

    }

    public void connectDevice(View view) {
        if (mBluetoothGatt == null) {
            checkBluetooth();
            Toast.makeText(this, "开始搜索连接设备", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "设备已连接", Toast.LENGTH_SHORT).show();
        }

    }

    public void shutdown(View view) {
        shutdownDevice();
    }

    public void disconnectDevice(View view) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt = null;

            isStop = true;
            Toast.makeText(this, "设备已断开", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkBluetooth() {
        //是否支持蓝牙功能
        if (mBluetoothAdapter == null) {
            return;
        }

        //是否支持BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE功能", Toast.LENGTH_SHORT).show();
            return;
        }

        //是否打开蓝牙开关
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_BLUETOOTH);
            return;
        }

        //搜索设备
        scanBleDevice(true);

    }


    /**
     * 设备搜索回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, device.getAddress() + " " + device.getName());
                            if (!TextUtils.isEmpty(device.getName()) && device.getName().equals(
                                    "SP-001")) {
                                mHandler.removeCallbacksAndMessages(null);
                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                if (isScanning) {
                                    Log.d(TAG, "START CONNECT");
                                    connect(device.getAddress());
                                    isScanning = false;
                                }

                            }
                        }
                    });
                }


            };


    //回调接口是在非ui线程回调
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (characteristic.equals(mReadKeypressCharac)) {
                displayKeypressStaus(characteristic.getValue()[0]);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //发现service
            Log.d(TAG, "discovery service successfully");
            mGattServices = gatt.getServices();
            displayGattServices(mGattServices);


        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.equals(mWriteDataCharac)) {
                if (characteristic.getValue().equals(shutDownCommand)) {
                    isStop = true;
                    isWriting = false;
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt = null;

                    Log.d(TAG, "写入关机命令成功");
                }

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.equals(mBatteryCharac)) {
                //字节数组第一位：电量 第二位：电池状态
                displayBattery(characteristic.getValue()[0], characteristic.getValue()[1]);
                Log.d(TAG, "Battery value" + byteArray2HexString(characteristic.getValue()));
            }

        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //连接或断开连接操作是否成功
            if (status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else {
                    //断开连接
                    Log.d(TAG, "断开连接成功");
                    isStop = true;
                    return;

                }
            } else {
                if (newState == BluetoothProfile.STATE_DISCONNECTED ) {
                    //连接失败，重连
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt = null;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            connectDevice(null);
                        }
                    });

                    Log.d(TAG, "连接失败重连");
                }
            }

        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }
        for (BluetoothGattService gattService : gattServices) { // 遍历出gattServices里面的所有服务
            //只取电量service和private service
            if (gattService.getUuid().equals(UUID.fromString(BATTERY_SERVICE_UUID))
                    || gattService.getUuid().equals(UUID.fromString(PRIVATE_SERVICE_UUID))) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                Log.d(TAG, "service uuid: " + gattService.getUuid());
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) { //
                    // 遍历每条服务里的所有Characteristic,保存所需Characteristics
                    Log.d(TAG, "characteristics uuid: " + gattCharacteristic.getUuid());
                    if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(
                            BATTERY_CHARACTERISTICS_UUID)) {
                        mBatteryCharac = gattCharacteristic;
                        //开启线程监听电池状态
                        startListnerBattery();
                    } else if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(
                            KEYPRESS_CHARACTERISTICS_UUID)) {
                        mReadKeypressCharac = gattCharacteristic;
                        //设置notification通知，按键回调在onCharacteristicChanged
                        boolean isEnableNotification = mBluetoothGatt.setCharacteristicNotification(
                                mReadKeypressCharac, true);
                        if (isEnableNotification) {
                            List<BluetoothGattDescriptor> descriptorList =
                                    mReadKeypressCharac.getDescriptors();
                            if (descriptorList != null && descriptorList.size() > 0) {
                                for (BluetoothGattDescriptor descriptor : descriptorList) {
                                    descriptor.setValue(
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    mBluetoothGatt.writeDescriptor(descriptor);
                                }
                            }
                        }

                    } else if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(
                            WRITE_DATA_UUID)) {
                        mWriteDataCharac = gattCharacteristic;
                    }
                }
            }

        }
    }

    private Handler mHandler = new Handler();
    private boolean isScanning;

    /**
     * 搜索ble设备
     *
     * @param enable 开始搜索或停止搜索
     */
    private void scanBleDevice(final boolean enable) {
        if (enable) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.d(TAG, "start scanning");

            isScanning = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   scanBleDevice(false);
                    Log.d(TAG, "stop scanning");
                    Toast.makeText(MainActivity.this, "搜索超时，请重试", Toast.LENGTH_SHORT).show();
                }
            }, 10 * 1000);


        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            isScanning = false;
        }
    }

    private boolean connect(final String address) {
        if (mBluetoothAdapter == null || TextUtils.isEmpty(address)) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.d(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");

        return true;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ENABLE_BLUETOOTH) {
            if (requestCode != RESULT_OK) {
                Toast.makeText(this, "请打开蓝牙！", Toast.LENGTH_SHORT).show();
            } else {
                checkBluetooth();
            }
        }
    }

    /**
     * 获取电池状态
     */
    private void getBatteryInfo() {
        if (mBluetoothGatt != null && mBatteryCharac != null) {
            mBluetoothGatt.readCharacteristic(mBatteryCharac);
            Log.d(TAG, "读取电量");
        }
    }

    private boolean isStop; //电池监听线程停止标志位

    // 是否有写入命令.由于characteristic的读写都是串行的，一个命令回调成功后才会执行下一个读写
    //若是在前一个命令回调前又进行一个读写，那么这个读写命令会被抛弃
    //isWrite标志用语保证写入命令优先执行而不会被阻塞抛弃
    private boolean isWriting;

    private void startListnerBattery() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
                    while (!isWriting) {
                        getBatteryInfo();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }).start();
    }

    private void displayBattery(byte battery, byte status) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("电量：").
                append(battery)
                .append("\n");
        switch (status) {
            case 0:
                stringBuffer.append("没充电");
                break;
            case 1:
                stringBuffer.append("充电中");
                break;
            case 2:
                stringBuffer.append("充满电");
                break;
            case 3:
                stringBuffer.append("低电量");
                break;
        }
        final String s = stringBuffer.toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBatteryTv.setText(s);
            }
        });
    }

    private void shutdownDevice() {
        if (mBluetoothGatt != null && mWriteDataCharac != null) {
            Log.d(TAG, "关闭设备");
            isWriting = true;
            mWriteDataCharac.setValue(shutDownCommand);
            mWriteDataCharac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothGatt.writeCharacteristic(mWriteDataCharac);
                }
            }, 1000);


        }
    }

    private void displayKeypressStaus(byte status) {
        String string = "";
        switch (status) {
            case 1:
                string = "单击";
                break;
            case 2:
                string = "双击";
                break;
            case 17:
                string = "长按3s,未超过6s";
                break;
            case 33:
                string = "拔出拉环";
                break;
            case 32:
                string = "还原拉环";
                break;
        }
        final String s = string;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mKeypressTv.setText(s);
            }
        });
    }


    public static final String[] hex =
            {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public static String byteArray2HexString(byte... byteArray) {
        if (byteArray != null) {
            StringBuffer buffer = new StringBuffer();
            for (byte b : byteArray) {
                int i = b & 0xFF;
                buffer.append(hex[i / 16] + hex[i % 16] + " ");
            }
            return buffer.toString().trim();
        }
        return "";
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStop = true;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }
}
