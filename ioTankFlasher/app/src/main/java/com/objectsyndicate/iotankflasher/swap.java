package com.objectsyndicate.iotankflasher;


import android.widget.CompoundButton;

public class swap {



}



/*


    sync_frame[36] = { 0x07, 0x07, 0x12,
            0x20,
            0x55, 0x55, 0x55, 0x55, 0x55, 0x55,
            0x55, 0x55,
            0x55, 0x55, 0x55, 0x55, 0x55, 0x55,
            0x55, 0x55,
            0x55, 0x55, 0x55, 0x55, 0x55, 0x55,
            0x55, 0x55,
            0x55, 0x55, 0x55, 0x55, 0x55, 0x55,
            0x55, 0x55 };
}




private fun findSerialPortDevice() {
    // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
    val usbDevices = usbManager.getDeviceList()
    if (!usbDevices.isEmpty()) {
        var keep = true
        for (entry in usbDevices.entrySet()) {
            device = entry.value
            val deviceVID = device.getVendorId()
            val devicePID = device.getProductId()

            if (deviceVID != 0x1d6b && devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003) {
                // There is a device connected to our Android device. Try to open it as a Serial Port.
                requestUserPermission()
                keep = false
            } else {
                connection = null
                device = null
            }

            if (!keep)
                break
        }
        if (!keep) {
            // There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
            val intent = Intent(ACTION_NO_USB)
            sendBroadcast(intent)
        }
    } else {
        // There is no USB devices connected. Send an intent to MainActivity
        val intent = Intent(ACTION_NO_USB)
        sendBroadcast(intent)
    }
}

*/







//    serial.read(mCallback)




/*
//
// reset online
serial.setDTR(false)
serial.setRTS(true)
Thread.sleep(5)
serial.setRTS(false)
//
*/

/*
serial.open();
serial.setBaudRate(115200);
serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
serial.setParity(UsbSerialInterface.PARITY_ODD);
serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_RTS_CTS);
serial.close()
*/

