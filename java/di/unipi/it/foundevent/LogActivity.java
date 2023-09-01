package di.unipi.it.foundevent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Activity that perform login.
 */
public class LogActivity extends AppCompatActivity implements View.OnClickListener{
    EditText username;
    EditText password;
    FloatingActionButton send;
    GetObjects getObjects;
    static CoordinatorLayout coordinatorLayout;
    @SuppressLint("StaticFieldLeak")
    static ProgressBar mProgressBar;
    private static final String secretKey = "$2b$10$f185HeZoCAQDhHaA27ExgenP.0ZFx4qtOlo9S2E/FJJ8sKy0eB0/S";
    private static final String jUrl = "https://api.jsonbin.io/e/collection/5ed0e4687741ef56a563fc9c/all-bins";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_layout);
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        
        username = findViewById(R.id.editText_username);
        password = findViewById(R.id.editText_password);
        send = findViewById(R.id.send_button_reglogin);
        coordinatorLayout = findViewById(R.id.coordinator_reglogin);
        mProgressBar = findViewById(R.id.progressbar_login);
        send.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
        toolbar.setNavigationOnClickListener(this);
        if(getSupportActionBar() != null)
            (getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_button_reglogin) {
            String name = username.getText().toString();
            String pw = password.getText().toString();

            if (name.length() == 0 || pw.length() == 0) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error : username or password empty. Try again", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.VISIBLE);

                //taking off keyboard
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                if (inputManager != null) {
                    if(getCurrentFocus() != null) {
                        inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
                if(getObjects != null && getObjects.getStatus() == AsyncTask.Status.RUNNING){
                    mProgressBar.setVisibility(View.VISIBLE);
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Please wait, your request is already been sent!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else {
                    getObjects = new GetObjects();
                    getObjects.execute(name, pw);
                }
            }
        } else {
            Intent intentBack = new Intent(this, MainActivity.class);
            intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentBack);
        }
    }

    /**
     * AsyncTask that take the users collection and search the JSON file that have the username and password given.
     */
    @SuppressLint("StaticFieldLeak")
    private class GetObjects extends AsyncTask<String,Integer,String> {



        @Override
        protected void onPreExecute() {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Request has been sent, please wait..", Snackbar.LENGTH_LONG);
            snackbar.show();
        }


        @Override

        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            StringBuilder response = new StringBuilder();


            try {
                publishProgress(20);
                URL url = new URL(jUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000); // 5 secondi per collegarsi
                urlConnection.setRequestProperty("secret-key", secretKey);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {

                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    response.append(current);

                }

                urlConnection.disconnect();
                in.close();
                inputStreamReader.close();
                JSONObject res = new JSONObject(response.toString());
                Log.d("RESPONSE", res.toString());
                JSONArray jResult = res.getJSONArray("records");
                JSONObject jsonObject;
                int i;
                publishProgress(40);
                HashMap<String, String> IDS = new HashMap<>();

                int count = 1;

                for (i = 0; i < jResult.length(); i++) {
                    jsonObject = jResult.getJSONObject(i);
                    IDS.put("BIN-ID_" + count, jsonObject.getString("id"));
                    count++;
                }
                Log.d("HASHMAP", IDS.toString());

                HttpURLConnection urlConnection_read;

                StringBuilder response_read = new StringBuilder();
                int j = 0;
                count = 1;

                while (j < IDS.size()) {
                    String READ_url = "https://api.jsonbin.io/b/";
                    URL url_read = new URL(READ_url + IDS.get("BIN-ID_" + count));


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
                    publishProgress(60);

                    urlConnection.disconnect();
                    inRead.close();
                    inputStreamReader_read.close();

                    JSONObject jRead = new JSONObject(response_read.toString());

                    Log.d("JSON-DOWNLOADED", jRead.toString());
                    if (jRead.getString("username").equals(params[0]) && jRead.getString("password").equals(params[1])) {
                        SingletonTicketsAndUsername.setBinIdUser(IDS.get("BIN-ID_" + count));

                        Log.d("BIN ID USER", SingletonTicketsAndUsername.getBinIdUser());
                        String userName = params[0];
                        publishProgress(80);
                        //put tickets IDS of username on database
                        JSONArray jsonArray = new JSONArray(jRead.getJSONArray("tickets").toString());
                        ArrayList<String> arrayList= new ArrayList<>();

                        for(int k = 0; k < jsonArray.length(); k++){
                            arrayList.add(jsonArray.getString(k));
                        }



                        MyDB db = MyDB.setInstance(getApplicationContext());
                        ArrayList<String> ticketsAlreadyPurchased = new ArrayList<>(db.eventsDao().getTicketsFromUser(SingletonTicketsAndUsername.getUserName()));

                        Log.d("TICKETS PURCHASED", ticketsAlreadyPurchased.toString());
                        arrayList.addAll(ticketsAlreadyPurchased);
                        user_tickets user_ticket = new user_tickets();
                        user_ticket.setUserName(params[0]);
                        user_ticket.setTickets_ID(arrayList);
                        db.eventsDao().insert_ticket(user_ticket);

                        SingletonTicketsAndUsername.setUserName(userName);
                        publishProgress(100);
                        Log.d("READ-SUCCESS", "Operation successfully done");
                        break;
                    }
                    response_read.setLength(0);
                    Log.d("READ-OP", "Not found yet");
                    j++;
                    count++;
                }
                if (j == IDS.size()) {

                    Log.e("READ-FAIL", "Wrong username or password");

                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error : username or password wrong. Try again", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    publishProgress(0);

                    this.cancel(true);
                }

            } catch (SocketTimeoutException e){
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error : unable to connect to database.", Snackbar.LENGTH_LONG);
                snackbar.show();
                e.printStackTrace();
                Log.e("TIMEOUT-EXCEPTION", e.toString());

            } catch(FileNotFoundException e){
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error with server, please try again", Snackbar.LENGTH_LONG);
                snackbar.show();
                publishProgress(0);
                e.printStackTrace();
                this.cancel(true);
            } catch (IOException | JSONException e) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error : try again", Snackbar.LENGTH_LONG);
                snackbar.show();
                publishProgress(0);
                e.printStackTrace();
                Log.e("IO EXCEPTION", e.toString());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressBar.setVisibility(View.GONE);
            Intent intent = new Intent(LogActivity.this, FoundEventActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Toast.makeText(getApplicationContext(), "Login successfully done!", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressBar.setProgress(values[0]);
        }
    }
}

