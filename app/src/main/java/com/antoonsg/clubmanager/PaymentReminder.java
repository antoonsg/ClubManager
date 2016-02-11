package com.antoonsg.clubmanager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by gantoons on 31/01/2016.
 */
public class PaymentReminder {
    private static final String TAG = "ClubManager.PayReminder";
    private String REMINDER_TEXT="La Gaillarde asbl:Chère famille %s,\n"
            + "d’après nos relevés, il vous reste à payer %d euros.\n"
            + "Votre total est de %d euros(cours %s)."
            + "Pourriez-vous payer au plus tôt sur\n"
            + "BE96 0012 9191 4405 (communication 'NOM PRENOM COT2015')?\n"
            + "Merci. Le Comité de 'La Gaillarde asbl'";
    private String SENT = "sent";
    private String DELIVERED = "delivered";

    void RemindBySMS(Context ctxt,String gsmnr, String nom,Integer totaldu, Integer solde, String cours) {
        String txtmsg = String.format(REMINDER_TEXT,nom,solde,totaldu,cours);
        SendASMS(ctxt,gsmnr, txtmsg);
    }

    private void SendASMS(final Context ctxt,String phoneNo, String msg) {
        /* Format and check phone number */
        String formattednr = PhoneNumberUtils.formatNumber(phoneNo, "BE");
        /* Create Pending Intents*/
        Intent sentIntent = new Intent(SENT);
        PendingIntent sentPI = PendingIntent.getBroadcast(
                ctxt, 0, sentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent deliveryIntent = new Intent(DELIVERED);
        PendingIntent deliverPI = PendingIntent.getBroadcast(
                ctxt, 0, deliveryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        /* Register for SMS send action */
        ctxt.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String result = "";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        result = "Transmission successful";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        result = "Transmission failed";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        result = "Radio off";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        result = "No PDU defined";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        result = "No service";
                        break;
                }
                Toast.makeText(ctxt, result,
                        Toast.LENGTH_LONG).show();
            }

        }, new IntentFilter(SENT));
        /* Register for Delivery event */
        ctxt.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(ctxt, "Delivered",
                        Toast.LENGTH_LONG).show();
            }
        }, new IntentFilter(DELIVERED));

        /*Send SMS*/
        Log.i(TAG, "Sending text of "+Integer.toString(msg.length())+" char to " + formattednr + "\n" + msg);
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> msgArray = smsManager.divideMessage(msg);
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliverPendingIntents = new ArrayList<PendingIntent>();
        for (int i = 0; i < msgArray.size(); i++) {
            sentPendingIntents.add(i, sentPI);
            deliverPendingIntents.add(i, deliverPI);
        }
//        smsManager.sendMultipartTextMessage(formattednr, null, msgArray, sentPendingIntents, deliverPendingIntents);
    }
}
