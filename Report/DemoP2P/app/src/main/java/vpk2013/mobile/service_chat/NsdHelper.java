package vpk2013.mobile.service_chat;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by VUDAI on 5/10/2017.
 */

public class NsdHelper {
    Context mContext;

    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";

    public static final String TAG = "NsdHelper";
    public String mServiceName = "ServiceChat";


    NsdServiceInfo mService;
    ServerSocket mServerSocket = null;
    Thread mThread = null;

    public NsdHelper(Context context) {
        Log.d(TAG, "move context");
        mContext = context;

        Log.d(TAG, "get nsd manager");
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        Log.d(TAG, "fini get nds manager ");
    }

    public void initializeNsd() {
        Log.d(TAG, "initializeNsd");
        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();

        //mNsdManager.init(mContext.getMainLooper(), this);

    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().contains(mServiceName)){
                    final String serviceName = service.getServiceName();

                    mResolveListener = new NsdManager.ResolveListener() {
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            Log.e(TAG, "Resolve failed" + errorCode);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                            WifiManager wm = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
                            String thisIpDevice = "/" + Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                            Log.d(TAG, "this device ip: " + thisIpDevice);
                            Log.d(TAG, "service ip: " + serviceInfo.getHost().toString());

                            if (serviceInfo.getHost().toString().equals(thisIpDevice)) {
                                Log.d(TAG, "Same IP:" + thisIpDevice);
                                return;
                            }
                            mService = serviceInfo;
                            ((MainActivity)mContext).onServiceFound(serviceInfo.getHost(), serviceInfo.getPort(), serviceName);

                        }
                    };
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (mService == service) {
                    mService = null;
                }
                else{
                    ((MainActivity)mContext).onServiceLost(service.getHost(), service.getPort(), service.getServiceName());
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                WifiManager wm = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
                String thisIpDevice = "/" + Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                Log.d(TAG, "this device ip: " + thisIpDevice);
                Log.d(TAG, "service ip: " + serviceInfo.getHost().toString());

                if (serviceInfo.getHost().toString().equals(thisIpDevice)) {
                    Log.d(TAG, "Same IP:" + thisIpDevice);
                    return;
                }
                mService = serviceInfo;
                ((MainActivity)mContext).onServiceFound(serviceInfo.getHost(), serviceInfo.getPort(), serviceInfo.getServiceName());

            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Toast.makeText(mContext, "service name of this device: " + mServiceName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }

        };
    }

    public void registerService() {
        //register server
        int port = 43210;
        try {
            Log.d(TAG, "register server");
            mServerSocket = new ServerSocket(port);
            if (mServerSocket == null){
                Log.d(TAG, "socket server null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mThread = new Thread(new ServerThread());
        mThread.start();

        //register service on this server
        Log.d(TAG, "register service");
        //int port = mServerSocket.getLocalPort();

        if (port != -1) {
            NsdServiceInfo serviceInfo = new NsdServiceInfo();
            Log.d(TAG, "Port: " + port);
            serviceInfo.setPort(port);
            serviceInfo.setServiceName(mServiceName);
            serviceInfo.setServiceType(SERVICE_TYPE);

            mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        }
        else{
            Log.d(TAG, "server is bound");
        }
    }

    public void discoverServices() {
        initializeDiscoveryListener();
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }

    public void tearDown() {
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mNsdManager.unregisterService(mRegistrationListener);
    }

    class ServerThread implements Runnable {

        @Override
        public void run() {

            try {
                // Since discovery will happen via Nsd, we don't need to care which port is
                // used.  Just grab an available one  and advertise it via Nsd.
                while (!Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "ServerSocket Created, awaiting connection");
                    ((MainActivity)mContext).receiveConnectionRequest(mServerSocket.accept());
                    Log.d(TAG, "Connected.");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating ServerSocket: ", e);
                e.printStackTrace();
            }
        }
    }

}
