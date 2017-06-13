package com.example.sangameswaran.mapdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Sangameswaran on 13-06-2017.
 */

public class NetworkChangeReciever extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable()){
            Toast.makeText(context,"Network Available",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context,"Network UnavailABLE",Toast.LENGTH_LONG).show();
        }
    }
}
