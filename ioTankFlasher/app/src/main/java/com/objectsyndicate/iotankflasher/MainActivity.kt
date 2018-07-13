package com.objectsyndicate.iotankflasher

import android.app.Activity
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.*
import android.support.v7.app.AppCompatActivity

import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

import java.io.*

import kotlin.text.Charsets.UTF_8
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.View
import android.widget.*
import kotlin.text.Charsets.US_ASCII


class MainActivity : AppCompatActivity() {
    lateinit var connection: UsbDeviceConnection
    lateinit var usbManager: UsbManager
    var device: UsbDevice? = null
    lateinit var serial: UsbSerialDevice
    var path = ""
    var progress = false
    var boot = false

    var radios = false
    var clouds = false


    var nl = System.getProperty("line.separator")!!

    val ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
    val ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"

    val ACTION_USB_PERMISSION_GRANTED = "com.felhr.usbservice.USB_PERMISSION_GRANTED"
    val ACTION_USB_PERMISSION_NOT_GRANTED = "com.felhr.usbservice.USB_PERMISSION_NOT_GRANTED"
    val ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED"

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    private var serialPortConnected: Boolean = false

    internal var Token: String = ""
    internal var SSID: String = ""
    internal var APPass: String = ""

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1: Intent) {
            if (arg1.action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                val netInfo: NetworkInfo = intent.getParcelableExtra (WifiManager.EXTRA_NETWORK_INFO)
                println(netInfo)
                if (ConnectivityManager.TYPE_WIFI == netInfo.type) {

                }
            }
        }
    }


    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1: Intent) {
            if (arg1.action == ACTION_USB_PERMISSION) {
                val granted = arg1.extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if (granted)
                // User accepted our USB connection. Try to open the device as a serial port
                {
                    val intent = Intent(ACTION_USB_PERMISSION_GRANTED)
                    arg0.sendBroadcast(intent)
                    connection = usbManager.openDevice(device)
                    //ConnectionThread().start()
                    print("ACTION_USB_PERMISSION_GRANTED")

                } else
                // User not accepted our USB connection. Send an Intent to the Main Activity
                {
                    val intent = Intent(ACTION_USB_PERMISSION_NOT_GRANTED)
                    arg0.sendBroadcast(intent)
                }
            } else if (arg1.action == ACTION_USB_ATTACHED) {
                if (!serialPortConnected)
                    print("ACTION_USB_ATTACHED")
                    //findSerialPortDevice() // A USB device has been attached. Try to open it as a Serial port

            } else if (arg1.action != ACTION_USB_ATTACHED) {
                if (!serialPortConnected)
                    print("ACTION_NO_USB")
                //findSerialPortDevice() // A USB device has been attached. Try to open it as a Serial port

            }else if (arg1.action == ACTION_USB_DETACHED) {
                // Usb device was disconnected. send an intent to the Main Activity
                val intent = Intent(ACTION_USB_DISCONNECTED)
                arg0.sendBroadcast(intent)
                if (serialPortConnected) {
                    //serialPort.syncClose()
                    //print("ACTION_USB_DISCONNECTED")
                }
                serialPortConnected = false
            }
        }
    }

    private fun setFilter() {
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(ACTION_USB_DETACHED)
        filter.addAction(ACTION_USB_ATTACHED)
        registerReceiver(usbReceiver, filter)
        registerReceiver(wifiReceiver, filter)

    }

///////////////////////////////////////////////////////////////////////////////////////////////////


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val th = findViewById(R.id.tabhost) as TabHost
        th.setup()

        val stats = th.newTabSpec("Tab1")
        stats.setContent(R.id.tab1)
        stats.setIndicator("Stats")
        th.addTab(stats)

        val flash = th.newTabSpec("Tab2")
        flash.setContent(R.id.tab2)
        flash.setIndicator("Flash")
        th.addTab(flash)

        val upd = th.newTabSpec("Tab3")
        upd.setContent(R.id.tab3)
        upd.setIndicator("Update")
        th.addTab(upd)

        val radio = findViewById(R.id.radioSwitch) as Switch
        val cloud = findViewById(R.id.cloudSwitch) as Switch
        val sid = findViewById(R.id.SSIDEdit) as EditText
        val ap_pass = findViewById(R.id.APPassEdit) as EditText
        val token = findViewById(R.id.TokenEdit) as EditText

        val sidt = findViewById(R.id.SSIDText) as TextView
        val ap_passt = findViewById(R.id.APPassText) as TextView
        val tokent = findViewById(R.id.tokenText) as TextView


        // We want radio and cloud buttons to hide/show the elements they require, and set their vars.
        radio.setOnClickListener {
            radios = radio.isChecked

            if(radios){
                APPass = ap_pass.text.toString()
                SSID = sid.text.toString()

                sid.visibility = View.VISIBLE
                ap_pass.visibility = View.VISIBLE

                sidt.visibility = View.VISIBLE
                ap_passt.visibility = View.VISIBLE

                if(clouds){
                    token.visibility = View.VISIBLE
                    tokent.visibility = View.VISIBLE
                    Token = token.text.toString()

                }
                else{
                    token.visibility = View.GONE
                    tokent.visibility = View.GONE
                    Token = "TOKEN"

                }



            }else{
                sid.visibility = View.GONE
                ap_pass.visibility = View.GONE
                token.visibility = View.GONE
                sidt.visibility = View.GONE
                ap_passt.visibility = View.GONE
                tokent.visibility = View.GONE
                Token = "TOKEN"
                APPass = "APPASS"
                SSID = "SSID"

            }
        }


        cloud.setOnClickListener {
            clouds = cloud.isChecked
            if (radios){

                if(clouds){
                    Token = token.text.toString()
                    APPass = ap_pass.text.toString()
                    SSID = sid.text.toString()
                    sid.visibility = View.VISIBLE
                    ap_pass.visibility = View.VISIBLE
                    token.visibility = View.VISIBLE
                    sidt.visibility = View.VISIBLE
                    ap_passt.visibility = View.VISIBLE
                    tokent.visibility = View.VISIBLE
                }else{
                    Token = "TOKEN"
                    APPass = ap_pass.text.toString()
                    SSID = sid.text.toString()
                    token.visibility = View.GONE
                    tokent.visibility = View.GONE

                }

            }
        }

        setFilter()

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val ssid = info.ssid.replace("\"", "")

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDevices = usbManager.deviceList
        try {
            device = usbDevices.toList()[0].second
        }catch (e: IndexOutOfBoundsException) {
            System.out.println("File Not Found.")
            e.printStackTrace()
            device = null
        }
        if (device != null) {
            connection = usbManager.openDevice(device)
            serial = UsbSerialDevice.createUsbSerialDevice(device, connection)
            serial.open()
            serial.setBaudRate(115200)
            serial.setDataBits(UsbSerialInterface.DATA_BITS_8)
            serial.setStopBits(UsbSerialInterface.STOP_BITS_1)
            serial.setParity(UsbSerialInterface.PARITY_NONE)
            serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
            serial.read(mCallback)
        }
        // Example of a call to a native method
        val tv = findViewById(R.id.SSIDEdit) as TextView
        tv.text = ssid

// Identify platform for binary execution
        var abi = Build.UNKNOWN

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            abi = Build.SUPPORTED_ABIS[0]
        } else {
            //noinspection deprecation
            abi = Build.CPU_ABI
        }

        var folder: String = ""
        if (abi.contains("armeabi-v7a"))
        {
            folder = "armeabi-v7a"
        } else if (abi.contains("x86_64"))
        {
            folder = "x86_64"
        }else if (abi.contains("arm64-v8a"))
        {
            folder = "arm64-v8a"
        } else if (abi.contains("x86"))
        {
            folder = "x86"
        } else if (abi.contains("armeabi"))
        {
            folder = "armeabi"
        }

// set binary path
        path = folder + "/" + "main"
        println(path)

        }//## oncreate end



// send click///////////////////////////////////////////////////////////////////////
    fun sendOnClick(v: View) {

    if (radios) {
        val ssid = findViewById(R.id.SSIDEdit) as EditText
        SSID = ssid.text.toString()
        val ap_pass = findViewById(R.id.APPassEdit) as EditText
        APPass = ap_pass.text.toString()

        if (TextUtils.isEmpty(SSID)) {
            ssid.error = "Enter Access Point"
            return
        } else if (TextUtils.isEmpty(APPass)) {
            ap_pass.error = "Enter Password"
            return
        }
        if(clouds){
            val token = findViewById(R.id.TokenEdit) as EditText
            Token = token.text.toString()
            if (TextUtils.isEmpty(Token)) {
                token.error = "Enter Authentication Token"
                return
            }
        }
    }
    FlashTask().execute()
} // send end

    // update click///////////////////////////////////////////////////////////////////////
    fun UpdateOnClick(v: View) {
          FactoryTask().execute()
    } // send end


    var s2 = ""
// Serial read listener.
    private val mCallback = UsbSerialInterface.UsbReadCallback { arg0 ->
        //println(arg0.toHex())
    if(progress){

        val comm = arg0
        for (x in comm) {
            s2 += x
            if (x == 0xC0.toByte()) {
                //print("thisfar")
                }
            }
        }

    if(boot){
        //print(arg0.toHex())
    }
    if(!boot and !progress){
// The stream has to be cached into a string and broken on new line manually
        val comm = String(arg0, UTF_8)
        for (x in comm) {
            s2 += x
            if (x == '\n') {
                val s = s2
                s2 = ""
                try{
                val l = s.split('_')
                val IP = l[0]
                val RSSI = l[1]
                val T1 = l[2]
                val T2 = l[3]
                val H = l[4]
                val UV = l[5]
                val L = l[6]

                runOnUiThread {
                    try {
                        val t1p = findViewById(R.id.T1Prog) as ProgressBar
                        val t1 = findViewById(R.id.T1View) as TextView
                        t1.text = "Temp 1 " + T1 + "C"
                        t1p.progress = (T1.toFloat().toInt())

                        val t2p = findViewById(R.id.T2Prog) as ProgressBar
                        val t2 = findViewById(R.id.T2View) as TextView
                        t2.text = "Temp 2 (internal) " + T2 + "C"
                        t2p.progress = (T2.toFloat().toInt())


                        val hp = findViewById(R.id.Hprog) as ProgressBar
                        val h = findViewById(R.id.HView) as TextView
                        h.text = "Humidity " + H + "%"
                        hp.progress = (H.toFloat().toInt())

                        val uvp = findViewById(R.id.UVprog) as ProgressBar
                        val uvi = findViewById(R.id.UVView) as TextView
                        uvi.text = "UV Index of " + UV
                        uvp.progress = (UV.toFloat() * 10).toInt()

                        val lp = findViewById(R.id.Lprog) as ProgressBar
                        val lux = findViewById(R.id.LView) as TextView
                        lux.text = "Lux " + L
                        lp.progress = (L.toFloat().toInt())


                        val ip = findViewById(R.id.NetView) as TextView
                        ip.text = IP

                        val quality = 2 * (RSSI.toFloat() + 100)

                        val rssip = findViewById(R.id.RSSIprog) as ProgressBar
                        val rssi = findViewById(R.id.RSSIView) as TextView
                        rssi.text = "Wifi Signal " + quality.toString()
                        rssip.progress = (quality.toInt())
                    }catch(e: NumberFormatException){
                        runOnUiThread {
                            val ip = findViewById(R.id.NetView) as TextView
                            ip.text = s
                        }
                        //print(e)
                    }
                }

                }catch(e: IndexOutOfBoundsException){
                    runOnUiThread {
                        val ip = findViewById(R.id.NetView) as TextView
                        ip.text = s
                    }
                    //print(e)
                }
            }
        }
    }
}
//////////////////////////////


///////////////////////////////////////

    fun Activity.runOnUiThread(f: () -> Unit) {
        runOnUiThread { f() }
    }

//////////////////////////////
    private inner class FlashTask : AsyncTask<Void, Int, Int>() {
        // Do the long-running work in here
    override fun doInBackground(vararg urls: Void): Int? {

            if (device != null) {
                runOnUiThread {
                    val flash = findViewById(R.id.sendButton) as Button
                    flash.visibility = View.INVISIBLE
                }

                Snackbar.make(findViewById(android.R.id.content), "Flash Started", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.WHITE)
                        .show()
// write file for settings
                val f = File(cacheDir.toString() + "/f")

                if(f.exists()){
                    f.delete()
                }
                f.createNewFile()
                f.setReadable(true)
                f.setWritable(true)
/// Write the settings


                f.bufferedWriter(US_ASCII).use { out ->
                    val setttingsString = SSID+nl+APPass+nl+clouds.toString().toUpperCase()+nl+Token+nl+"objectsyndicate.com"+nl+"/api/v1/post"+nl+radios.toString().toUpperCase()+nl
                    //print(setttingsString)
                    out.write(setttingsString)

                }
                //println(cacheDir)
                //println(filesDir)
// assets copy to FS
                val file = File(filesDir.path + "/" + path)
                val dir = file.parentFile
                dir.mkdirs()
                //println(path)
                val inputStream = assets.open(path)
                val bufferedOutputStream = BufferedOutputStream(FileOutputStream(file))
                val buf = ByteArray(10240)
                var num = inputStream.read(buf)
                while (num > 0) {
                    bufferedOutputStream.write(buf, 0, num)
                    num = inputStream.read(buf)
                }
                bufferedOutputStream.close()
                inputStream.close()
// make exec
                val execFile = File(filesDir.path + "/" + path)
                execFile.setExecutable(true)

//delete old out.bin, create the new one, set the perms

                val binFile = File(filesDir.path +"/out.bin")

                if(binFile.exists()){
                    binFile.delete()
                }
                binFile.createNewFile()
                binFile.setReadable(true)
                binFile.setWritable(true)

// once copied then exe
                val myExec = execFile.toString()
                val process = Runtime.getRuntime().exec(myExec+" -c "+cacheDir.toString()+" "+filesDir.path+"/out.bin")
                val os = DataOutputStream(process.outputStream)
                //var osRes = DataInputStream(process.inputStream)
                Thread.sleep(50)

// bin to byte for serial push

                val data = binFile.readBytes()
                val size = binFile.readBytes().size

                //println("size")
                //println(size)
                //println("file hex")
                //println(binFile.readBytes().toHex())

// Hex MUST BE CAPS
                val sync_frame ="C00008240000000000070712205555555555555555555555555555555555555555555555555555555555555555C0".hexSerial()
                //println(sync_frame.toHex())

                //the bootloader overrides all
                val bootheader = "C0000510000000000038080000010000003808000000001040C0".hexSerial()
                //println(bootheader.toHex())
                val bootloader = "C000074808770000003808000000000000000000000000000000000000080000601C000060000000601000006031FCFF71FCFF81FCFFDBDC2000680332D218DBDC20004807404074DCC48608005823DBDC200098081BA5A92392450058031B555903582337350129230B446604DFC6F3FF21EEFFDBDC200069020DF0000000010078480040004A0040B449004012C1F0C921D911E901DD0209312020B4ED033C2C56C2073020B43C3C56420701F5FFDBDC00003C4C569206CD0EEADD860300202C4101F1FFDBDC000056A204C2DCF0DBDC2DDBDCCC6CCAE2D1EAFF0606002030F456D3FD86FBFF00002020F501E8FFDBDC0000EC82D0CCDBDCDBDC2EDBDCC73DEB2ADC460300202C4101E1FFDBDC0000DC42C2DCF0DBDC2DDBDC56BCFEC602003C5C8601003C6C4600003C7C08312D0CD811C821E80112C1100DF0000C180000140010400C0000607418000064180000801800008C18000084180000881800009018000018980040880F0040A80F0040349800404C4A0040740F0040800F0040980F00400099004012C1E091F5FFC961CD0221EFFFE941F9310971D9519011DBDC1A223902E2D1180C02226E1D21E4FF31E9FF2AF11A332D0F42630001EAFFDBDC0000DBDC30B43C2256A31621E1FF1A2228022030B43C3256B31501ADFFDBDC0000DD023C4256ED1431D6FF4D010C52D90E192E126E0101DDFFDBDC000021D2FF32A101DBDC20004802303420DBDC200039022C0201D7FFDBDC0000463300000031CDFF1A333803D023DBDC3199FF27B31ADC7F31CBFF1A3328030198FFDBDC000056C20E2193FF2ADD060E000031C6FF1A3328030191FFDBDC000056820DD2DD10460800000021BEFF1A2228029CE231BCFFDBDC20F51A33290331BBFFDBDC2C411A332903DBDCF0F4222E1D22D204273D9332A3FFDBDC2000280E27B3F721ABFF381E1A2242A40001B5FFDBDC0000381E2D0C42A40001B3FFDBDC000056120801B2FFDBDC0000DBDC2000280EC2DC0422D2FCDBDC2000290E01ADFFDBDC0000222E1D22D204226E1D281E22D204E7B204291E860000126E012198FF32A0042A21C54C003198FF222E1D1A33380337B202C6D6FF2C02019FFFDBDC00002191FF318CFF1A223A31019CFFDBDC0000218DFF1C031A22C549000C02060300003C528601003C624600003C72918BFF9A110871C861D851E841F83112C1200DF00010000068100000581000007010000074100000781000007C100000801000001C4B0040803C004091FDFF12C1E061F7FFC961E941F9310971D9519011DBDC1A66290621F3FFC2D1101A22390231F2FF0C0F1A33590331EAFFF26C1AED045C2247B3028636002D0C016DFFDBDC000021E5FF41EAFF2A611A4469040622000021E4FF1A222802F0D2DBDCD7BE01DD0E31E0FF4D0D1A3328033D0101E2FFDBDC0000561209D03D2010212001DFFFDBDC00004D0D2D0C3D01015DFFDBDC000041D5FFDAFF1A444804D0648041D2FF1A4462640061D1FF106680622600673F1331D0FF10338028030C43853A002642164613000041CAFF222C1A1A444804202FDBDC47328006F6FF222C1A273F3861C2FF222C1A1A6668066732B921BDFF3D0C1022800148FFDBDC000021BAFF1C031A2201BFFFDBDC00000C024603005C3206020000005C424600005C5291B7FF9A110871C861D851E841F83112C1200DF0B0100000DBDC100000D010000012C1E091FEFFC961D951E9410971F931CD039011DBDCED02DD0431A1FF9C1422A06247B302062D0021F4FF1A22490286010021F1FF1A223902219CFF2AF12D0F011FFFDBDC0000461C0022D110011CFFDBDC000021E9FFFD0C1A222802C7B20621E6FF1A22F8022D0E3D014D0F0195FFDBDC00008C5222A063C6180000218BFF3D01102280F04F200111FFDBDC0000AC7D22D1103D014D0F010DFFDBDC000021D6FF32D110102280010EFFDBDC000021D3FF1C031A220185FFDBDC0000FAEEF0CCDBDC56ACF821CDFF317AFF1A223A310105FFDBDC000021C9FF1C031A22017CFFDBDC00002D0C91C8FF9A110871C861D851E841F83112C1200DF0000200600000001040020060FFFFFF0012C1E00C02290131FAFF21FAFF026107C961DBDC2000226300DBDC2000C80320CC10564CFF21F5FFDBDC2000380221F4FF20231029010C432D010163FFDBDC000008712D0CC86112C1200DF00080FE3F8449004012C1D0C9A109B17CFC22C1110C13C51C00261202463000220111C24110B68202462B0031F5FF3022A02802A002002D011C03851A0066820A280132210105A6FF0607003C12C60500000010212032A01085180066A20F2221003811482105B3FF224110861A004C1206FDFF2D011C03C5160066B20E280138114821583185CFFF06F7FF005C1286F5FF0010212032A01085140066A20D2221003811482105E1FF06EFFF0022A06146EDFF45F0FFC6EBFF000001D2FFDBDC000006E9FF000C022241100C1322C110C50F00220111060600000022C1100C13C50E0022011132C2FA303074B6230206C8FF08B1C8A112C1300DF0000000000010404F484149007519031027000000110040A8100040BC0F0040583F0040CC2E00401CE20040D83900408000004021F4FF12C1E0C961C80221F2FF097129010C02D951C91101F4FFDBDC000001F3FFDBDC0000AC2C22A3E801F2FFDBDC000021EAFFDBDC31412A233D0C01EFFFDBDC00003D0222A00001EDFFDBDC0000C1E4FF2D0C01E8FFDBDC00002D0132A004450400C5E7FFDD022D0C01E3FFDBDC0000666D1F4B2131DCFF4600004B22DBDC200048023794F531D9FFDBDC200039023DF08601000001DCFFDBDC00000871C861D85112C1200DF000000012C1F002610301EAFEDBDC0000083112C1100DF000643B004012C1D0E98109B1C9A1D991F97129013911E2A0DBDC01FAFFDBDC0000CD02E792F40C0DE2A0DBDCF2A0DBDD860D00000001F4FFDBDC0000204220E71240F7921C22610201EFFFDBDC000052A0DC482157120952A0DD571205460500004D0C3801DA234242001BDD3811379DC5C6000000000C0DC2A0DBDC01E3FFDBDC0000C792F608B12D0DC8A1D891E881F87112C1300DF00000C0".hexStringToByteArray()
                //println(bootloader.toHex())

                // more büt stuff
                val booth2 = "C000051000000000002000000001000000200000000080FE3FC0".hexSerial()
                val bootl2 = "C0000730009100000020000000000000000000000000000000FE0510401A0610403B0610405A0610407A061040820610408C0610408C061040C0".hexSerial()

                // this is the CPU entry and other flash info
                val entry = "C0000608000000000000000000F4061040C0".hexSerial()
//serial
                boot = true
// baud is changed to read the bootlog
                serial.setBaudRate(76800)
                Thread.sleep(10)
// bootloader enable
                serial.setRTS(true)
                serial.setDTR(false)
                Thread.sleep(5)
                serial.setRTS(false)
                serial.setDTR(true)
                Thread.sleep(50)
                serial.setDTR(false)
// baud set back
                Thread.sleep(10)
                boot = false
                serial.setBaudRate(115200)
                Thread.sleep(25)

// Sync frame sets baud rate for flash, old modem squeek here seems to need 2
                serial.write(sync_frame)
                serial.write(sync_frame)

                // header packet for bootloader stub (5)
                Thread.sleep(20)
                serial.write(bootheader)
                // bootloader stub (7)
                Thread.sleep(20)
                serial.write(bootloader)
                // (5)
                Thread.sleep(20)
                serial.write(booth2)
                // bootloader stub (7)
                Thread.sleep(20)
                serial.write(bootl2)
                Thread.sleep(20)
                serial.write(entry)
                Thread.sleep(500)
                serial.write("C001C0".hexSerial())
                Thread.sleep(20)
                //  44, 33, 22, 11 || 00 00 30 00, 00 b0 0f 00, 01 00 00 00        // <III(addr, size, 1)  (6)
                val flashHead = "C0000030000000010001000000C0".hexSerial()
                //var flashHead = "C00000300000B00F0001000000C0".hexSerial()
                serial.write(flashHead)
                Thread.sleep(500)
                progress = true
                //println("file send start")
                Thread.sleep(10)
//strip C0 as bytes

                var sent = 0

                while(sent < size){
                    val block = data.copyOfRange(sent, sent+1024)
                    serial.write(block)
                    Thread.sleep(100)
                    sent += 1024

                    runOnUiThread {
                        val hp = findViewById(R.id.progressBar) as ProgressBar
                        hp.max = size
                        hp.progress = (sent)
                    }
                    //println(sent)

                }
                Thread.sleep(20)
                serial.write("C006C0".hexSerial())
                Thread.sleep(10)
                //written = 0
                //println("file send done")
                progress = false

                Snackbar.make(findViewById(android.R.id.content), "Flash Finished", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.WHITE)
                        .show()

                runOnUiThread {
                    val flash = findViewById(R.id.sendButton) as Button
                    flash.visibility = View.VISIBLE
                }
            }
            return null
        }
    }

    private inner class FactoryTask : AsyncTask<Void, Int, Int>() {
        // Do the long-running work in here
        override fun doInBackground(vararg urls: Void): Int? {

            if (device != null) {
                runOnUiThread {
                    val flash = findViewById(R.id.sendButton) as Button
                    flash.visibility = View.INVISIBLE
                }

                Snackbar.make(findViewById(android.R.id.content), "Restore Started", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.WHITE)
                        .show()

                val factoryBin = assets.open("bins/ioTank.bin")
                val factorySpiff = assets.open("bins/spiffs.bin")

// bin to byte for serial push
                val factoryData = factoryBin.readBytes()
                val spiffData = factorySpiff.readBytes()

                val factorySize = factoryData.size
                val spiffSize = spiffData.size


// Hex MUST BE CAPS
                val sync_frame ="C00008240000000000070712205555555555555555555555555555555555555555555555555555555555555555C0".hexSerial()
                //println(sync_frame.toHex())

                //the bootloader overrides all
                val bootheader = "C0000510000000000038080000010000003808000000001040C0".hexSerial()
                //println(bootheader.toHex())
                val bootloader = "C000074808770000003808000000000000000000000000000000000000080000601C000060000000601000006031FCFF71FCFF81FCFFDBDC2000680332D218DBDC20004807404074DCC48608005823DBDC200098081BA5A92392450058031B555903582337350129230B446604DFC6F3FF21EEFFDBDC200069020DF0000000010078480040004A0040B449004012C1F0C921D911E901DD0209312020B4ED033C2C56C2073020B43C3C56420701F5FFDBDC00003C4C569206CD0EEADD860300202C4101F1FFDBDC000056A204C2DCF0DBDC2DDBDCCC6CCAE2D1EAFF0606002030F456D3FD86FBFF00002020F501E8FFDBDC0000EC82D0CCDBDCDBDC2EDBDCC73DEB2ADC460300202C4101E1FFDBDC0000DC42C2DCF0DBDC2DDBDC56BCFEC602003C5C8601003C6C4600003C7C08312D0CD811C821E80112C1100DF0000C180000140010400C0000607418000064180000801800008C18000084180000881800009018000018980040880F0040A80F0040349800404C4A0040740F0040800F0040980F00400099004012C1E091F5FFC961CD0221EFFFE941F9310971D9519011DBDC1A223902E2D1180C02226E1D21E4FF31E9FF2AF11A332D0F42630001EAFFDBDC0000DBDC30B43C2256A31621E1FF1A2228022030B43C3256B31501ADFFDBDC0000DD023C4256ED1431D6FF4D010C52D90E192E126E0101DDFFDBDC000021D2FF32A101DBDC20004802303420DBDC200039022C0201D7FFDBDC0000463300000031CDFF1A333803D023DBDC3199FF27B31ADC7F31CBFF1A3328030198FFDBDC000056C20E2193FF2ADD060E000031C6FF1A3328030191FFDBDC000056820DD2DD10460800000021BEFF1A2228029CE231BCFFDBDC20F51A33290331BBFFDBDC2C411A332903DBDCF0F4222E1D22D204273D9332A3FFDBDC2000280E27B3F721ABFF381E1A2242A40001B5FFDBDC0000381E2D0C42A40001B3FFDBDC000056120801B2FFDBDC0000DBDC2000280EC2DC0422D2FCDBDC2000290E01ADFFDBDC0000222E1D22D204226E1D281E22D204E7B204291E860000126E012198FF32A0042A21C54C003198FF222E1D1A33380337B202C6D6FF2C02019FFFDBDC00002191FF318CFF1A223A31019CFFDBDC0000218DFF1C031A22C549000C02060300003C528601003C624600003C72918BFF9A110871C861D851E841F83112C1200DF00010000068100000581000007010000074100000781000007C100000801000001C4B0040803C004091FDFF12C1E061F7FFC961E941F9310971D9519011DBDC1A66290621F3FFC2D1101A22390231F2FF0C0F1A33590331EAFFF26C1AED045C2247B3028636002D0C016DFFDBDC000021E5FF41EAFF2A611A4469040622000021E4FF1A222802F0D2DBDCD7BE01DD0E31E0FF4D0D1A3328033D0101E2FFDBDC0000561209D03D2010212001DFFFDBDC00004D0D2D0C3D01015DFFDBDC000041D5FFDAFF1A444804D0648041D2FF1A4462640061D1FF106680622600673F1331D0FF10338028030C43853A002642164613000041CAFF222C1A1A444804202FDBDC47328006F6FF222C1A273F3861C2FF222C1A1A6668066732B921BDFF3D0C1022800148FFDBDC000021BAFF1C031A2201BFFFDBDC00000C024603005C3206020000005C424600005C5291B7FF9A110871C861D851E841F83112C1200DF0B0100000DBDC100000D010000012C1E091FEFFC961D951E9410971F931CD039011DBDCED02DD0431A1FF9C1422A06247B302062D0021F4FF1A22490286010021F1FF1A223902219CFF2AF12D0F011FFFDBDC0000461C0022D110011CFFDBDC000021E9FFFD0C1A222802C7B20621E6FF1A22F8022D0E3D014D0F0195FFDBDC00008C5222A063C6180000218BFF3D01102280F04F200111FFDBDC0000AC7D22D1103D014D0F010DFFDBDC000021D6FF32D110102280010EFFDBDC000021D3FF1C031A220185FFDBDC0000FAEEF0CCDBDC56ACF821CDFF317AFF1A223A310105FFDBDC000021C9FF1C031A22017CFFDBDC00002D0C91C8FF9A110871C861D851E841F83112C1200DF0000200600000001040020060FFFFFF0012C1E00C02290131FAFF21FAFF026107C961DBDC2000226300DBDC2000C80320CC10564CFF21F5FFDBDC2000380221F4FF20231029010C432D010163FFDBDC000008712D0CC86112C1200DF00080FE3F8449004012C1D0C9A109B17CFC22C1110C13C51C00261202463000220111C24110B68202462B0031F5FF3022A02802A002002D011C03851A0066820A280132210105A6FF0607003C12C60500000010212032A01085180066A20F2221003811482105B3FF224110861A004C1206FDFF2D011C03C5160066B20E280138114821583185CFFF06F7FF005C1286F5FF0010212032A01085140066A20D2221003811482105E1FF06EFFF0022A06146EDFF45F0FFC6EBFF000001D2FFDBDC000006E9FF000C022241100C1322C110C50F00220111060600000022C1100C13C50E0022011132C2FA303074B6230206C8FF08B1C8A112C1300DF0000000000010404F484149007519031027000000110040A8100040BC0F0040583F0040CC2E00401CE20040D83900408000004021F4FF12C1E0C961C80221F2FF097129010C02D951C91101F4FFDBDC000001F3FFDBDC0000AC2C22A3E801F2FFDBDC000021EAFFDBDC31412A233D0C01EFFFDBDC00003D0222A00001EDFFDBDC0000C1E4FF2D0C01E8FFDBDC00002D0132A004450400C5E7FFDD022D0C01E3FFDBDC0000666D1F4B2131DCFF4600004B22DBDC200048023794F531D9FFDBDC200039023DF08601000001DCFFDBDC00000871C861D85112C1200DF000000012C1F002610301EAFEDBDC0000083112C1100DF000643B004012C1D0E98109B1C9A1D991F97129013911E2A0DBDC01FAFFDBDC0000CD02E792F40C0DE2A0DBDCF2A0DBDD860D00000001F4FFDBDC0000204220E71240F7921C22610201EFFFDBDC000052A0DC482157120952A0DD571205460500004D0C3801DA234242001BDD3811379DC5C6000000000C0DC2A0DBDC01E3FFDBDC0000C792F608B12D0DC8A1D891E881F87112C1300DF00000C0".hexStringToByteArray()
                //println(bootloader.toHex())

                // more büt stuff
                val booth2 = "C000051000000000002000000001000000200000000080FE3FC0".hexSerial()
                val bootl2 = "C0000730009100000020000000000000000000000000000000FE0510401A0610403B0610405A0610407A061040820610408C0610408C061040C0".hexSerial()

                // this is the CPU entry and other flash info
                val entry = "C0000608000000000000000000F4061040C0".hexSerial()
//serial
                boot = true
// baud is changed to read the bootlog
                serial.setBaudRate(76800)
                Thread.sleep(10)
// bootloader enable
                serial.setRTS(true)
                serial.setDTR(false)
                Thread.sleep(5)
                serial.setRTS(false)
                serial.setDTR(true)
                Thread.sleep(50)
                serial.setDTR(false)
// baud set back
                Thread.sleep(10)
                boot = false
                Thread.sleep(25)

// Sync frame sets baud rate for flash, old modem squeek here seems to need 2
                serial.write(sync_frame)
                serial.write(sync_frame)

                // header packet for bootloader stub (5)
                Thread.sleep(20)
                serial.write(bootheader)
                // bootloader stub (7)
                Thread.sleep(20)
                serial.write(bootloader)
                // (5)
                Thread.sleep(20)
                serial.write(booth2)
                // bootloader stub (7)
                Thread.sleep(20)
                serial.write(bootl2)
                Thread.sleep(20)
                serial.write(entry)
                Thread.sleep(500)
                serial.write("C001C0".hexSerial())
                Thread.sleep(20)
                //  44, 33, 22, 11 || 00 00 30 00, 00 b0 0f 00, 01 00 00 00        // <III(addr, size, 1)  (6)

                //val spiffHead = "C00000300000B00F0001000000C0".hexSerial()
                var factoryHead = "C00000000000B0070001000000C0".hexSerial()




                serial.write(factoryHead)
                Thread.sleep(500)
                progress = true
                //println("file send start")
                Thread.sleep(10)
//strip C0 as bytes

                var sent = 0

                while(sent < factorySize){
                    val block = factoryData.copyOfRange(sent, sent+1024)
                    serial.write(block)
                    Thread.sleep(100)
                    sent += 1024

                    runOnUiThread {
                        val hp = findViewById(R.id.progressBar2) as ProgressBar
                        hp.max = factorySize
                        hp.progress = (sent)
                    }
                    //println(sent)

                }

///////////////////////////////////////

                serial.write("C006C0".hexSerial())

                Thread.sleep(2500)

                boot = true
// baud is changed to read the bootlog
                Thread.sleep(10)
// bootloader enable
                serial.setRTS(true)
                serial.setDTR(false)
                Thread.sleep(5)
                serial.setRTS(false)
                serial.setDTR(true)
                Thread.sleep(50)
                serial.setDTR(false)
// baud set back
                Thread.sleep(10)
                boot = false
                Thread.sleep(25)

// Sync frame sets baud rate for flash, old modem squeek here seems to need 2
                serial.write(sync_frame)
                serial.write(sync_frame)

                // header packet for bootloader stub (5)
                Thread.sleep(20)
                serial.write(bootheader)
                // bootloader stub (7)
                Thread.sleep(20)
                serial.write(bootloader)
                // (5)
                Thread.sleep(20)
                serial.write(booth2)
                // bootloader stub (7)
                Thread.sleep(20)
                serial.write(bootl2)
                Thread.sleep(20)
                serial.write(entry)
                Thread.sleep(500)
                serial.write("C001C0".hexSerial())
                Thread.sleep(20)
                //  44, 33, 22, 11 || 00 00 30 00, 00 b0 0f 00, 01 00 00 00        // <III(addr, size, 1)  (6)

                //val spiffHead = "C00000300000B00F0001000000C0".hexSerial()
                val spiffHead = "C0000030000000010001000000C0".hexSerial()




                Thread.sleep(20)

                serial.write(spiffHead)
                Thread.sleep(500)

                sent = 0

                while(sent < spiffSize){
                    val block = spiffData.copyOfRange(sent, sent+1024)
                    serial.write(block)
                    Thread.sleep(100)
                    sent += 1024

                    runOnUiThread {
                        val hp = findViewById(R.id.progressBar2) as ProgressBar
                        hp.max = spiffSize
                        hp.progress = (sent)
                    }
                    //println(sent)

                }
                Thread.sleep(20)
                serial.write("C006C0".hexSerial())
                Thread.sleep(10)
                //written = 0
                //println("file send done")
                progress = false

                serial.setBaudRate(115200)

                Snackbar.make(findViewById(android.R.id.content), "Restore Finished", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.WHITE)
                        .show()

                runOnUiThread {
                    val flash = findViewById(R.id.sendButton) as Button
                    flash.visibility = View.VISIBLE
                }
            }
            return null
        }
    }
} // main end