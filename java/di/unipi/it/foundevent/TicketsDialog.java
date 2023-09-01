package di.unipi.it.foundevent;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/**
 * TicketsDialog for purchase tickets.
 */
@SuppressLint("StaticFieldLeak")
public class TicketsDialog extends DialogFragment implements View.OnClickListener {
    private ArrayList<Integer> numbers;

    private static Context context;
    private Spinner spinner;

    private static ProgressBar progressBar;
    private static String eName;
    private static final String secretKey = "$2b$10$f185HeZoCAQDhHaA27ExgenP.0ZFx4qtOlo9S2E/FJJ8sKy0eB0/S";
    private static final String jUrl_GET = "https://api.jsonbin.io/b/5eb12cc247a2266b14731309/latest";


    /**
     * @param n : list that contains the number of purchasable tickets
     * @param c : context
     * @param eventName : name event
     */
    TicketsDialog(ArrayList<Integer> n, Context c, String eventName) {
        this.numbers = n;
        context = c;
        eName = eventName;
        this.setCancelable(true);
    }

    /**
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_layout, container, false);
    }


    /**
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinner = view.findViewById(R.id.spinner);

        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, numbers);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        Typeface typeface = ResourcesCompat.getFont(context, R.font.gruppo);
        TextView textUp = view.findViewById(R.id.text_select);

        textUp.setTypeface(typeface);

        TextView textPurchase = view.findViewById(R.id.text_purchase);
        textPurchase.setTypeface(typeface);
        FloatingActionButton purchase = view.findViewById(R.id.purchase_button);
        progressBar = view.findViewById(R.id.progressbar_dialog);
        progressBar.setVisibility(View.INVISIBLE);
        purchase.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        int num = (int) spinner.getSelectedItem();
        this.setCancelable(false);
        new sendRequestOfTickets().execute(num);
    }

    /**
     * Dismiss the fragment and its dialog.  If the fragment was added to the
     * back stack, all back stack state up to and including this entry will
     * be popped.  Otherwise, a new transaction will be committed to remove
     * the fragment.
     */
    @Override
    public void dismiss() {
        super.dismiss();
    }

    /**
     * AsyncTask for manage the request of tickets
     */
    public static class sendRequestOfTickets extends AsyncTask<Integer, Integer, Void> {
        boolean resUpdate;

        @Override
        protected Void doInBackground(Integer... integers) {
            StringBuilder response = new StringBuilder();
            HttpURLConnection urlConnection_GET = null;
            HttpURLConnection urlConnection_read = null;
            HttpURLConnection urlConnection_write = null;
            try {
                URL url = new URL(jUrl_GET);
                urlConnection_GET = (HttpURLConnection) url.openConnection();
                urlConnection_GET.setConnectTimeout(5000); // 5 secondi per collegarsi
                urlConnection_GET.setRequestProperty("secret-key", secretKey);
                urlConnection_GET.setRequestMethod("GET");


                urlConnection_GET.connect();

                Log.d("GET-REQUEST", "connection established");

                // download database from jsonbin.io
                InputStreamReader inputStreamReader = new InputStreamReader(urlConnection_GET.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder builder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                bufferedReader.close();
                inputStreamReader.close();
                Log.d("GET-RESPONSE", builder.toString());
                if(builder.toString().contains("success")){
                    JSONObject jsonObject = new JSONObject(builder.toString());
                    String res = jsonObject.getString("success");
                    if(res.equals("false")){
                        Toast.makeText(context, "Error in download database, please try again", Toast.LENGTH_LONG).show();
                        this.cancel(true);
                    }
                }
                JSONObject jsonObject;
                JSONArray jResult = new JSONArray(builder.toString());
                int i;
                //updating database subtracting the purchased tickets from ticketsAvailable
                for (i = 0; i < jResult.length(); i++) {
                    jsonObject = jResult.getJSONObject(i);
                    if (jsonObject.getString("eventName").equals(eName)) {
                        int newPlacesAvailable = jsonObject.getInt("placesAvailable") - integers[0];
                        jsonObject.put("placesAvailable", newPlacesAvailable);
                        Log.d("JRESULT AFTER DELETING", jResult.getJSONObject(i).toString());
                    }


                }

                urlConnection_GET.disconnect();
                publishProgress(10);



                String jUrl_UPDATE = "https://api.jsonbin.io/b/5eb12cc247a2266b14731309";
                URL url2 = new URL(jUrl_UPDATE);
                HttpURLConnection urlConnection_UPDATE = (HttpURLConnection) url2.openConnection();
                urlConnection_UPDATE.setRequestProperty("Content-Type", "application/json");
                urlConnection_UPDATE.setRequestProperty("secret-key", secretKey);
                urlConnection_UPDATE.setRequestProperty("versioning", "false");
                urlConnection_UPDATE.setConnectTimeout(5000); // 5 secondi per collegarsi
                urlConnection_UPDATE.setRequestMethod("PUT");

                urlConnection_UPDATE.setDoOutput(true);
                urlConnection_UPDATE.connect();

                // send update database
                DataOutputStream wr = new DataOutputStream(urlConnection_UPDATE.getOutputStream());
                wr.writeBytes(jResult.toString());
                wr.flush();
                wr.close();



                InputStream in = urlConnection_UPDATE.getInputStream();
                InputStreamReader inputStreamReader_up = new InputStreamReader(in);

                int inputStreamData = inputStreamReader_up.read();
                while (inputStreamData != -1) {

                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader_up.read();
                    response.append(current);

                }
                Log.d("RESPONSE UPDATE DB", response.toString());
                urlConnection_UPDATE.disconnect();
                in.close();
                inputStreamReader.close();

                publishProgress(30);
                JSONObject jRes = new JSONObject();
                jRes.put("success", "true");
                String res = jRes.getString("success");
                if (res.contains("false")) {
                    resUpdate = false;
                    Toast.makeText(context, "Error in purchasing tickets : please try again", Toast.LENGTH_LONG).show();

                } else {
                    resUpdate = true;
                    ArrayList<String> tickets_IDS = new ArrayList<>(SingletonTicketsAndUsername.getTickets());
                    ArrayList<String> correct = new ArrayList<>();

                    for (int k = 0; k < integers[0]; k++) {
                        //tickets ID element : event name + / + ticket ID (to recognize which event is associated with ticket ID)
                        tickets_IDS.add(eName + "/" + UUID.randomUUID().toString().substring(0, 10));
                    }

                    //removing the "[]" element
                    for (Iterator<String> iterator = tickets_IDS.iterator(); iterator.hasNext(); ) {
                        String element = iterator.next();
                        Log.d("element", element);
                        if (element.equals("[]")) {
                            iterator.remove();
                        }
                    }
                    //taking the elements without [] brakets
                    for (int j = 0; j < tickets_IDS.size(); j++) {
                        String t = tickets_IDS.get(j).replace("[", "").replace("]", "").trim();
                        correct.add(t);
                    }
                    Log.d("CORRECT", correct.toString());

                    SingletonTicketsAndUsername.setTickets(correct);
                    tickets_IDS = correct;

                    Log.d("TICKETS IN DATABASE", tickets_IDS.toString());
                    Log.d("TICKETS IN SINGLETON", SingletonTicketsAndUsername.getTickets().toString());
                    //updating internal database
                    MyDB db = MyDB.getInstance();
                    user_tickets user_tickets = new user_tickets();
                    user_tickets.setUserName(SingletonTicketsAndUsername.getUserName());
                    user_tickets.setTickets_ID(tickets_IDS);
                    db.eventsDao().insert_ticket(user_tickets);
                    publishProgress(50);


                    StringBuilder response_read = new StringBuilder();
                    String READ_url = "https://api.jsonbin.io/b/";

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

                    publishProgress(90);
                    JSONArray jsonArray = new JSONArray(jRead.getJSONArray("tickets").toString());
                    Log.d("TICKETS IN FILE", jsonArray.toString());
                    ArrayList<String> arrayList = new ArrayList<>(tickets_IDS);

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


                    Log.d("TICKETS IDS", tickets_IDS.toString());

                }

            } catch (SocketTimeoutException e) {

                e.printStackTrace();
                Log.e("TIMEOUT-EXCEPTION", e.toString());

            } catch (IOException | JSONException e) {

                e.printStackTrace();
                Log.e("IO EXCEPTION", e.toString());
            } finally {
                if (urlConnection_GET != null) {
                    urlConnection_GET.disconnect();
                }

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
            if(!resUpdate){
                Toast.makeText(context, "Error in purchasing tickets : please try again", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
            else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Tickets successfully purchased!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, FoundEventActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }


    }
}



