package di.unipi.it.foundevent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Activity that received the ticket sent by another device with nfc.
 */
@SuppressLint("StaticFieldLeak")
public class NfcReceiveMessageActivity extends AppCompatActivity implements View.OnClickListener {
    private static ArrayList<String> tickets;

    private String nameAndTicketID;
    private static Context context;
    private static ProgressBar progressBar;
    private static final String secretKey = "$2b$10$f185HeZoCAQDhHaA27ExgenP.0ZFx4qtOlo9S2E/FJJ8sKy0eB0/S";
    private TextView textView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_layout_receive_message);
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        textView = findViewById(R.id.name_ticket);
        context = getApplicationContext();
        FloatingActionButton buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(this);
        textView.setText(nameAndTicketID);
        progressBar = findViewById(R.id.progressbar_dialog_nfc);
        progressBar.setVisibility(View.INVISIBLE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
        toolbar.setNavigationOnClickListener(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

    }
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if(intent.getAction() != null)
            Log.d("INTENT", intent.getAction());

        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = null; // only one message transferred
            if (rawMessages != null) {
                Log.d("RAW MESSAGES", Arrays.toString(rawMessages));
                message = (NdefMessage) rawMessages[0];
            }

            if (message != null) {
                nameAndTicketID = new String(message.getRecords()[0].getPayload());
                Log.d("TICKET NAME ID IN REC", nameAndTicketID);
                textView.setText(nameAndTicketID);

            }


        }
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_save) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            tickets = SingletonTicketsAndUsername.getTickets();
            tickets.add(nameAndTicketID);
            new sendRequestOfTickets().execute();
        }
        else{
            Intent intent = new Intent(context, RecyclerViewTicketsActivity.class);
            context.startActivity(intent);
        }
    }
    /**
     * AsyncTask for manage saving tickets
     */
    public static class sendRequestOfTickets extends AsyncTask<Integer, Integer, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            publishProgress(10);
            HttpURLConnection urlConnection_read = null;
            HttpURLConnection urlConnection_write = null;
            try {
                String userName = SingletonTicketsAndUsername.getUserName();
                //updating internal database
                MyDB db = MyDB.getInstance();
                user_tickets user_tickets = new user_tickets();
                user_tickets.setUserName(userName);
                user_tickets.setTickets_ID(tickets);
                db.eventsDao().update_user_tickets(user_tickets);
                List<String> list = db.eventsDao().getTicketsFromUser(userName);
                Log.d("DATABASE UPDATED", list.toString());
                publishProgress(20);

                //update user file with purchased tickets
                //taking the users collection
                String READ_url = "https://api.jsonbin.io/b/";
                StringBuilder response_read = new StringBuilder();
                URL url_read = new URL(READ_url + SingletonTicketsAndUsername.getBinIdUser());
                urlConnection_read = (HttpURLConnection) url_read.openConnection();
                urlConnection_read.setRequestProperty("secret-key", secretKey);
                urlConnection_read.setConnectTimeout(5000); // 5 secondi per collegarsi

                urlConnection_read.setRequestMethod("GET");
                urlConnection_read.connect();

                InputStream inRead = urlConnection_read.getInputStream();
                InputStreamReader inputStreamReader_read = new InputStreamReader(inRead);

                int inputStreamData_read = inputStreamReader_read.read();
                while (inputStreamData_read != -1) {

                    char current = (char) inputStreamData_read;
                    inputStreamData_read = inputStreamReader_read.read();
                    response_read.append(current);

                }
                urlConnection_read.disconnect();
                inRead.close();
                inputStreamReader_read.close();

                JSONObject jRead = new JSONObject(response_read.toString());

                Log.d("JSON-DOWNLOADED", jRead.toString());
                publishProgress(80);
                JSONArray jsonArray = new JSONArray(jRead.getJSONArray("tickets").toString());
                Log.d("TICKETS IN FILE", jsonArray.toString());
                ArrayList<String> arrayList = new ArrayList<>(tickets);
                //taking the tickets already purchased
                for (int h = 0; h < jsonArray.length(); h++) {
                    arrayList.add(jsonArray.getString(h));
                }
                jRead.put("tickets", arrayList);
                //put tickets IDS on user file
                Log.d("TICKETS", arrayList.toString());
                urlConnection_write = (HttpURLConnection) url_read.openConnection();
                urlConnection_write.setRequestProperty("secret-key", secretKey);
                urlConnection_write.setRequestProperty("Content-type", "application/json");
                urlConnection_write.setConnectTimeout(5000); // 5 secondi per collegarsi

                urlConnection_write.setRequestMethod("PUT");
                urlConnection_write.connect();

                DataOutputStream wr_USER = new DataOutputStream(urlConnection_write.getOutputStream());
                wr_USER.writeBytes(jRead.toString());
                wr_USER.flush();
                wr_USER.close();

                publishProgress(90);
                InputStream inRead_USER = urlConnection_write.getInputStream();
                InputStreamReader inputStreamReader_read_USER = new InputStreamReader(inRead_USER);
                StringBuilder response_read_USER = new StringBuilder();
                int inputStreamData_read_USER = inputStreamReader_read_USER.read();
                while (inputStreamData_read_USER != -1) {

                    char current = (char) inputStreamData_read_USER;
                    inputStreamData_read_USER = inputStreamReader_read_USER.read();
                    response_read_USER.append(current);

                }

                Log.d("RESPONSE UPDATE", response_read_USER.toString());
                urlConnection_write.disconnect();
                inRead_USER.close();
                publishProgress(100);


            }
             catch (SocketTimeoutException e) {

                e.printStackTrace();
                Log.e("TIMEOUT-EXCEPTION", e.toString());

            } catch (IOException | JSONException e) {

                e.printStackTrace();
                Log.e("IO EXCEPTION", e.toString());
            } finally {
                if (urlConnection_write != null) {
                    urlConnection_write.disconnect();
                }
                if (urlConnection_read != null) {
                    urlConnection_read.disconnect();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {

            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(context, FoundEventActivity.class);
            Toast.makeText(context, "Ticket successfully saved, go to your tickets to see it!", Toast.LENGTH_LONG).show();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }


    }
}
