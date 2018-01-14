package com.example.isaacwarwick.nfcreader;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class NfcVReaderTask extends AsyncTask<Tag, Void, byte[]> {

    private Context mCon;

    final protected static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private final int MAX_SENSOR_BYTES = 1952;
    private final int MAX_BLOCK_SIZE = 8;
    private final int MAX_SENSOR_BLOCKS = MAX_SENSOR_BYTES / MAX_BLOCK_SIZE;

    private byte[] tag_data_raw = new byte[MAX_SENSOR_BYTES];

    public NfcVReaderTask(Context con) {
        mCon = con;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String byteToHex(byte byte_) {
        char[] hexChars = new char[2];

        int v = byte_ & 0xFF;
        hexChars[0] = HEX_ARRAY[v >>> 4];
        hexChars[1] = HEX_ARRAY[v & 0x0F];

        return new String(hexChars);
    }

    @Override
    protected byte[] doInBackground(Tag... params) {
        Vibrator vibrator = (Vibrator) mCon.getSystemService(mCon.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        byte[] cmd;
        int offset = 2;
        byte[] oneBlock = new byte[MAX_BLOCK_SIZE];

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

                for (int j = 0; j < MAX_BLOCK_SIZE; j++) {
                    tag_data_raw[i * MAX_BLOCK_SIZE + j] = oneBlock[j];
                }

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

        vibrator.vibrate(500);

        return tag_data_raw;
    }

    @Override
    protected void onPostExecute(byte[] tag_data_raw) {
        super.onPostExecute(tag_data_raw);

        /*for (int i = 0; i < MAX_SENSOR_BYTES; i += MAX_BLOCK_SIZE) {
            Log.i("MainActivity", "[" + Integer.toString(i / MAX_BLOCK_SIZE, 16) + "] : "
                    + byteToHex(tag_data_raw[i]) + byteToHex(tag_data_raw[i+1])
                    + byteToHex(tag_data_raw[i+2]) + byteToHex(tag_data_raw[i+3])
                    + byteToHex(tag_data_raw[i+4]) + byteToHex(tag_data_raw[i+5])
                    + byteToHex(tag_data_raw[i+6]) + byteToHex(tag_data_raw[i+7])
            );
        }*/

        String temp = byteToHex(tag_data_raw[26]);
        int index_trend = Integer.parseInt(temp,16);
        temp = byteToHex(tag_data_raw[27]);
        int index_history = Integer.parseInt(temp,16);
        temp = bytesToHex(new byte[]{tag_data_raw[317], tag_data_raw[316]});
        final int sensor_time = Integer.parseInt(temp, 16);
        final int tag_lifetime_min_left = 14 * 24 * 60 - sensor_time;

        Log.i("MainActivity", Integer.toString(index_trend));
        Log.i("MainActivity", Integer.toString(index_history));
        Log.i("MainActivity", Integer.toString(sensor_time / 60 / 24));
        Log.i("MainActivity", Integer.toString(tag_lifetime_min_left / 60 / 24));
    }
}
