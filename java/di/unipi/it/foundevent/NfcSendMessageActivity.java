package di.unipi.it.foundevent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Objects;


/**
 * Activity that send the ticket with nfc.
 */
public class NfcSendMessageActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback, View.OnClickListener {
    private String nameAndTicketID;

    /**
     * Send a notification
     * @param contentText : text of notification
     */
    private void createNotificationChannel(@NonNull String contentText) {
        // NotificationChannel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String CHANNEL_ID = "1";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "NFC", importance);
            notificationChannel.setDescription(contentText);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // Register channel
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder builder = new Notification.Builder(this,CHANNEL_ID);
            builder.setContentText("NFC");
            builder.setContentTitle(contentText);
            builder.setSmallIcon(R.drawable.ic_notification);
            notificationManager.notify(112,builder.build());

        }
        else {
            //Notification for API < 26
            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            //Build the notification with Notification.Builder
            Notification.Builder builder = new Notification.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_notification)
                    .setAutoCancel(false)
                    .setVibrate(new long[]{500})
                    .setContentTitle("NFC")
                    .setContentText(contentText);


            //Show the notification
            int NOTIFICATION_ID = 1;
            if (mNotificationManager != null) {
                mNotificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }
    }

    /**
     * @param nfcEvent : nfc event
     * @deprecated
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        NdefRecord ndefRecord = NdefRecord.createMime("text/plain", nameAndTicketID.getBytes());
        Log.d("NDEF RECORD", "ndef record created : " + nameAndTicketID);
        return new NdefMessage(ndefRecord);
    }




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_layout_send_message);
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        Intent intent = getIntent();
        nameAndTicketID = intent.getStringExtra("nameAndTicketID");
        Log.d("NFC SEND ON VIEW CREATE", "I'm in nfc send on view created");
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if(mNfcAdapter == null){
            Log.d("NULL ADAPTER", "ADAPTER NFC IS NULL");
        }
        else {
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
            Log.d("CALLBACK INITIALIZATION", "callbacks initialize successfully");
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
        toolbar.setNavigationOnClickListener(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onClick(View view) {
        Intent intentBack = new Intent(this, RecyclerViewTicketsActivity.class);
        startActivity(intentBack);
    }

    /**
     * @param nfcEvent : sending ticket ID + name event
     * @deprecated
     */
    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        createNotificationChannel("Ticket with ID " + nameAndTicketID + " sent!");
    }
}

