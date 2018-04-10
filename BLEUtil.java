
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2018/4/8.
 */

public class BLEUtil {
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> mGattServices;
    private final String TAG = "BLE_LOG";

    private Context mContext;
    private OnKeyDownListener mOnKeyDownListner;
    private OnBatteryStatusListener mOnBatteryStatusListner;

    //电池监听线程停止标志位
    private volatile boolean isStop;

    // 是否有写入命令.由于characteristic的读写都是串行的，一个命令回调成功后才会执行下一个读写
    //若是在前一个命令回调前又进行一个读写，那么这个读写命令会被抛弃
    //isWrite标志是为了暂停轮询线程的读取，保证写入命令优先执行而不会被阻塞抛弃
    private volatile boolean isWriting;

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

    //单击
    public final static int ACTION_TYPE_PRESS = 0;
    //双击
    public final static int ACTION_TYPE_DOUBLE_PRESS = 1;
    //拉环拔出
    public final static int ACTION_TYPE_RING_PULL = 2;
    //拉环复位
    public final static int ACTION_TYPE_RING_RESET = 3;
    //长按3s,未超过6s
    public final static int ACTION_TYPE_LONG_PRESS = 4;


    //关闭设备命令
    private byte[] shutDownCommand = new byte[]{0x21, 0x20, 0x16, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, (byte) 0xFE};
    //解除报警状态命令
    private byte[] relieveSosCommand = new byte[]{0x21, 0x20, 0x11, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, (byte) 0xFE};
    //APP解除绑定
    private byte[] APPunbind = new byte[]{0x21, 0x20, 0x13, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, (byte) 0xFE};
    //APP寻找设备
    private byte[] searchingDevice = new byte[]{0x21, 0x20, 0x12, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, (byte) 0xFE};
    //预警触发成功
    private byte[] sosSuccessfully = new byte[]{0x21, 0x20, 0x14, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, (byte) 0xFE};
    //预警触发失败
    private byte[] sosUnsuccessfully = new byte[]{0x21, 0x20, 0x15, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, (byte) 0xFE};

    public BLEUtil(Context context) {
        mContext = context;
    }

    public void setOnKeyDownListener(OnKeyDownListener listener){
        mOnKeyDownListner = listener;
    }

    public void setOnBatteryStatusListener(OnBatteryStatusListener listener){
        mOnBatteryStatusListner = listener;
    }

    /**
     * 连接硬件设备
     */
    public void connectDevice() {
        if (mBluetoothGatt == null) {
            checkBluetooth();
            Toast.makeText(mContext, "开始搜索连接设备", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "设备已连接", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 关闭设备
     */
    public void shutdownDevice() {
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
            }, 500);


        }
    }

    /**
     * 断开连接
     */
    public void disconnectDevice() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt = null;
            isStop = true;
            isWriting = true;
            Toast.makeText(mContext, "设备已断开", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 解除当前报警状态
     */
    public void relieveSos() {
        if (mBluetoothGatt != null && mWriteDataCharac != null) {
            Log.d(TAG, "解除当前报警状态");
            isWriting = true;
            mWriteDataCharac.setValue(relieveSosCommand);
            //写入命令时，必须设置writeType，否则将无法获取回调
            mWriteDataCharac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            //之所以延时500ms是为了确保改写入命令能得到执行，500ms时间确保轮询的电池状态线程能够暂时不去
            //读取电池状态，以便写入的命令能得到执行
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothGatt.writeCharacteristic(mWriteDataCharac);
                }
            },500);


        }
    }

    /**
     * 触发预警成功
     */
    public void sosSuccessfully(){
        if (mBluetoothGatt != null && mWriteDataCharac != null) {
            Log.d(TAG, "尝试写入触发预警成功");
            isWriting = true;
            mWriteDataCharac.setValue(sosSuccessfully);
            mWriteDataCharac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothGatt.writeCharacteristic(mWriteDataCharac);
                }
            },500);


        }
    }

    /**
     * 触发预警失败
     */
    public void sosUnsuccessfully(){
        if (mBluetoothGatt != null && mWriteDataCharac != null) {
            Log.d(TAG, "尝试写入触发预警失败");
            isWriting = true;
            mWriteDataCharac.setValue(sosUnsuccessfully);
            mWriteDataCharac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothGatt.writeCharacteristic(mWriteDataCharac);
                }
            },500);


        }
    }

    /**
     * 搜索设备
     */
    public void searchingDevice(){
        if (mBluetoothGatt != null && mWriteDataCharac != null) {
            Log.d(TAG, "尝试写入寻找设备");
            isWriting = true;
            mWriteDataCharac.setValue(searchingDevice);
            mWriteDataCharac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothGatt.writeCharacteristic(mWriteDataCharac);
                }
            },500);


        }
    }
    private void checkBluetooth() {
        //是否支持蓝牙功能
        if (mBluetoothAdapter == null) {
            return;
        }

        //是否支持BLE
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, "不支持BLE功能", Toast.LENGTH_SHORT).show();
            return;
        }

        //是否打开蓝牙开关
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(mContext, "请打开蓝牙开关", Toast.LENGTH_SHORT).show();
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
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, device.getAddress() + " " + device.getName());
                            if (!TextUtils.isEmpty(device.getName()) && device.getName().equals(
                                    "SP-001")) {
                                //找到设备后停止搜索，并取消开始搜索时设置的超时取消搜索
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
            //设备按键回调
            if (characteristic.equals(mReadKeypressCharac)) {
                displayKeyPressStatus(characteristic.getValue()[0]);
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
                    isWriting = true;
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    Log.d(TAG, "写入关机命令成功");
                }else if (characteristic.getValue().equals(relieveSosCommand)){
                    isWriting = false;
                    Log.d(TAG, "写入解除报警命令成功");
                }else if (characteristic.getValue().equals(sosSuccessfully)){
                    isWriting = false;
                    Log.d(TAG, "写入触发预警成功命令成功");
                }else if (characteristic.getValue().equals(sosUnsuccessfully)){
                    isWriting = false;
                    Log.d(TAG, "写入触发预警失败命令成功");
                }else if (characteristic.getValue().equals(searchingDevice)){
                    isWriting = false;
                    Log.d(TAG, "写入搜索设备命令成功");
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
               
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else {
                    //断开连接
                    Log.d(TAG, "断开连接成功");
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    isStop = true;
                    isWriting = true;
                    return;

                }
            } else {
                if (newState == BluetoothProfile.STATE_DISCONNECTED ) {
                    //连接失败，重连
                    isStop = true;
                    isWriting = true;
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            connectDevice();
                        }
                    });

                    Log.d(TAG, "连接失败重连");
                }
            }

        }
    };

    /**
     * 遍历设备所有service和characteristic
     * 保存所需characteristic，启动线程轮询电池状态
     * 设置按键characteristic notification属性
     * @param gattServices
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.d(TAG, Thread.currentThread()+""+"display service and characteristics");
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
                        startListenerBattery();
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

    private Handler mHandler = new Handler(Looper.getMainLooper());
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
                    Toast.makeText(mContext, "搜索超时，请重试", Toast.LENGTH_SHORT).show();
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

        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");

        return true;

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



    private void startListenerBattery() {
        isWriting = false;
        isStop = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
                    while (!isWriting) {
                        getBatteryInfo();
                        Log.d(TAG, "电池监听线程："+Thread.currentThread().getName());
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                Log.d(TAG,"listening thread has stopped!");
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnBatteryStatusListner.onBatteryStatus(s);
            }
        });

    }



    /**
     * 执行回调接口
     * 一些问题：1、在双击状态，单击时无法接收到单击状态
     *          2、在双击状态，长按时无法接收到长按状态
     *          3、双击状态下，只能跳转拉环拔出状态
     * @param status
     */
    private void displayKeyPressStatus(byte status) {
        int action_tmp = -1;
        switch (status) {
            case 1:
                action_tmp = ACTION_TYPE_PRESS;
                break;
            case 2:
                action_tmp = ACTION_TYPE_DOUBLE_PRESS;
                break;
            case 17:
                action_tmp = ACTION_TYPE_LONG_PRESS;
                break;
            case 33:
                action_tmp = ACTION_TYPE_RING_PULL;
                break;
            case 32:
                action_tmp = ACTION_TYPE_RING_RESET;
                break;
        }
        final int action = action_tmp;
        if (mOnKeyDownListner != null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnKeyDownListner.onKeyDown(action);
                }
            });

        }


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

    //按键回调接口
    public interface OnKeyDownListener {
        void onKeyDown(int action);
    }

    //电池状态会调接口
    public interface OnBatteryStatusListener {
        void onBatteryStatus(String string);
    }
}
