package com.example.isaacwarwick.nfcreader;

import android.content.Intent;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(nfcAdapter != null && nfcAdapter.isEnabled()) {
            // Toast.makeText(this, "NFC available!", Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent nfcIntent) {
        Toast.makeText(this, "NFC intent received!", Toast.LENGTH_LONG).show();

        super.onNewIntent(nfcIntent);
    }

    @Override
    protected void onResume() {
        Intent nfcIntent = new Intent(this, MainActivity.class);
        nfcIntent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);

        super.onResume();
    }

    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);

        super.onPause();
    }
}
