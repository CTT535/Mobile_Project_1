package hcmus.angtonyvincent.firebaseauthentication.list_room;

import android.app.Application;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import hcmus.angtonyvincent.firebaseauthentication.connection.Connection;
import hcmus.angtonyvincent.firebaseauthentication.room.DeviceInRoom;
import hcmus.angtonyvincent.firebaseauthentication.room.RoomInfo;

/**
 * Created by VUDAI on 5/14/2017.
 */

public class NsdHelper extends Application{
    static Context mContext;

    static NsdManager mNsdManager;
    static NsdManager.ResolveListener mResolveListener;
    static NsdManager.DiscoveryListener mDiscoveryListener;
    static NsdManager.RegistrationListener mRegistrationListener;
    static boolean init = false;
    static boolean registered = false;

    public static final String SERVICE_TYPE = "_http._tcp.";

    public static final String TAG = "NsdHelper";
    public static String mServiceName = "Game Service";


    static NsdServiceInfo mService;

    public NsdHelper(Context context) {
        Log.d(TAG, "move context");
        mContext = context;
    }

    public static void initializeNsd() {
        Log.d(TAG, "initializeNsd");
        if(!init) {
            mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
            initializeResolveListener();
            initializeDiscoveryListener();
            initializeRegistrationListener();
            init = true;
        }
    }

    private static void initializeDiscoveryListener() {
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
                            ((ListRoomActivity)mContext).onRoomFound(serviceInfo.getHost(), serviceInfo.getPort(), serviceName);
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
                    ((ListRoomActivity)mContext).onRoomLost(service.getHost(), service.getPort(), service.getServiceName());
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

    private static void initializeResolveListener() {
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

                if (serviceInfo.getHost().toString().equals(thisIpDevice)) {
                    Log.d(TAG, "Same IP:" + thisIpDevice);
                    return;
                }
                mService = serviceInfo;
                ((ListRoomActivity)mContext).onRoomFound(serviceInfo.getHost(), serviceInfo.getPort(), serviceInfo.getServiceName());

            }
        };
    }

    private static void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                Log.d(TAG, "ServiceRegistered");
                mServiceName = NsdServiceInfo.getServiceName();
                ((ListRoomActivity)mContext).onRoomCreate(
                        new RoomInfo(mServiceName,
                                new DeviceInRoom(NsdServiceInfo.getHost(), NsdServiceInfo.getPort(), "", true)));
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.d(TAG, "RegistrationFailed");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }
        };
    }

    public static void registerService() {
        //register server
        if(!registered) {
            int port = Connection.registreServer(Connection.PORT);
            //int port = mServerSocket.getLocalPort();
            if (port != -1) {
                Log.d(TAG, "register service on port: " + port);
                NsdServiceInfo serviceInfo = new NsdServiceInfo();
                serviceInfo.setPort(port);
                serviceInfo.setServiceName(mServiceName);
                serviceInfo.setServiceType(SERVICE_TYPE);

                mNsdManager.registerService(
                        serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
                registered = true;

            } else {
                Log.d(TAG, "server is bound");
            }
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

    public static void tearDown() {
        Log.d(TAG, "unregistre");
        if(registered) {
            mNsdManager.unregisterService(mRegistrationListener);
            registered = false;
        }
    }
}
