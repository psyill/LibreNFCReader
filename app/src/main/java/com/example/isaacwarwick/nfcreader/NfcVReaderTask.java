package com.example.isaacwarwick.nfcreader;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;
import java.io.IOException;
import java.util.Arrays;

public class NfcVReaderTask extends AsyncTask<Tag, Void, String> {

    private Context mCon;

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private final int MAX_SENSOR_BYTES = 1952;
    private final int MAX_SENSOR_BLOCKS = MAX_SENSOR_BYTES / 8;

    private byte[] tag_data_raw = new byte[MAX_SENSOR_BYTES];

    public NfcVReaderTask(Context con) {
        mCon = con;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String byteToHex(byte byte_) {
        char[] hexChars = new char[2];

        int v = byte_ & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];

        return new String(hexChars);
    }

    @Override
    protected String doInBackground(Tag... params) {
        Vibrator vibrator = (Vibrator) mCon.getSystemService(mCon.VIBRATOR_SERVICE);
        vibrator.vibrate(500);

        byte[] cmd;
        int offset = 2;
        byte[] oneBlock = new byte[9];

        Tag tag = params[0];
        final byte[] uid = tag.getId();
        NfcV nfcvTag = NfcV.get(tag);

        try {
            nfcvTag.connect();

            for (int i = 0; i < MAX_SENSOR_BLOCKS; i++) {

                cmd = new byte[]{
                        (byte) 0x60, (byte) 0x20, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) (i & 0x0ff), (byte) (0x00)
                };

                System.arraycopy(uid, 0, cmd, 2, 8);

                while (true) {
                    try {
                        oneBlock = nfcvTag.transceive(cmd);
                        break;
                    } catch (IOException e) {
                        return null;
                    }
                }

                oneBlock = Arrays.copyOfRange(oneBlock, offset, oneBlock.length);

                tag_data_raw[i * 8 + 0] = oneBlock[0];
                tag_data_raw[i * 8 + 1] = oneBlock[1];
                tag_data_raw[i * 8 + 2] = oneBlock[2];
                tag_data_raw[i * 8 + 3] = oneBlock[3];
                tag_data_raw[i * 8 + 4] = oneBlock[4];
                tag_data_raw[i * 8 + 5] = oneBlock[5];
                tag_data_raw[i * 8 + 6] = oneBlock[6];
                tag_data_raw[i * 8 + 7] = oneBlock[7];
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                nfcvTag.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < MAX_SENSOR_BYTES; i++) {
            Log.i("MainActivity", i + " : " + byteToHex(tag_data_raw[i]));
        }

        return null;
    }
}
