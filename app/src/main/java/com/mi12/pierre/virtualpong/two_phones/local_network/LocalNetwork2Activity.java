package com.mi12.pierre.virtualpong.two_phones.local_network;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mi12.R;
import com.mi12.pierre.virtualpong.CST;
import com.mi12.pierre.virtualpong.two_phones.DrawActivityClient;
import com.mi12.pierre.virtualpong.two_phones.DrawActivityServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalNetwork2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_network2);

        final Button bt_create = (Button) findViewById(R.id.create);
        bt_create.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(LocalNetwork2Activity.this, DrawActivityServer.class);
                        startActivity(intent);
                    }
                });
        final EditText mEdit   = (EditText)findViewById(R.id.ip);
        Button bt_join = (Button) findViewById(R.id.join);
        bt_join.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (((EditText) findViewById(R.id.ip)).getText().toString().matches("")) {
                            Toast.makeText(LocalNetwork2Activity.this, "Please fill the IP address",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(LocalNetwork2Activity.this, DrawActivityClient.class);
                            Bundle b = new Bundle();
                            try {
                                b.putSerializable("ip", InetAddress.getByName(mEdit.getText().toString()));
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            b.putInt("port", CST.PORT_A);
                            intent.putExtras(b);
                            startActivity(intent);
                        }
                    }
                });

        TextView myIp = (TextView) findViewById(R.id.myIp);
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        myIp.setText(ip);
    }
}
