package com.example.qianyiwang.nfcreader;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MainApp extends Activity {

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(nfcAdapter==null){
            Toast.makeText(this, "this device doesn't support NFC", 0).show();
            finish();
            return;
        }
        if(!nfcAdapter.isEnabled()){
            Toast.makeText(this, "NFC is disabled", 0).show();
        }

    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String>{

        @Override
        protected String doInBackground(Tag... tags) {
            Tag tag = tags[0];
            Ndef ndef = Ndef.get(tag);

            if(ndef==null){
                return null;
            }
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            for(NdefRecord ndefRecord: records){
                if(ndefRecord.getTnf()==NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)){
                    try {
                        String msg = readText(ndefRecord);
                        Log.v("NDEF msg", msg);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {

            byte[] payload = record.getPayload();
            String textEncoding = ((payload[0]&128)==0) ? "UTF-8":"UTF-16";
            int languageCodeLength = payload[0] & 0063;

            return new String(payload, languageCodeLength+1, payload.length-languageCodeLength-1, textEncoding);
        }
    }
}
