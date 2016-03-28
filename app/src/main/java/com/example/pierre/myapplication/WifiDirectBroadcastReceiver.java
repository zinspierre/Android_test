package com.example.pierre.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pierre on 24/03/16.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver{

    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;


    public WifiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity){
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("Broadcast receiver", "onReceive");
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
                Log.w("WIFI P2P STATE CHANGE", "ENABLE");
            } else {
                activity.setIsWifiP2pEnabled(false);
                Log.w("WIFI P2P STATE CHANGE", "DISABLE");
//                activity.resetData();

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, (PeerListListener) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list));
                Log.w("WIFI P2P PEERS CHANGED", "requestPeers()");
            }
//            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.w("WIFI P2P CONNECTION ", "CHANGED");

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP

//                DeviceDetailFragment fragment = (DeviceDetailFragment) activity
//                        .getFragmentManager().findFragmentById(R.id.frag_detail);
//                manager.requestConnectionInfo(channel, fragment);
            } else {
                // It's a disconnect
//                activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.w("WIFI P2P THIS", " DEVICE CHANGED");
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }
    }
}

