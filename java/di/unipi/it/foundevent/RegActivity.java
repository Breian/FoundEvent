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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

/**
 * Activity to register a new user.
 */
public class RegActivity extends AppCompatActivity implements View.OnClickListener{
    EditText username;
    EditText password;
    FloatingActionButton send;
    @SuppressLint("StaticFieldLeak")
    static ProgressBar mProgressBar;
    static CoordinatorLayout coordinatorLayout;

    private static final String secretKey = "$2b$10$f185HeZoCAQDhHaA27ExgenP.0ZFx4qtOlo9S2E/FJJ8sKy0eB0/S";
    private static final String jUrl = "https://api.jsonbin.io/b/";
    private static final String collectionID = "5ed0e4687741ef56a563fc9c";
    private static final String jUrl_COLLECTION = "https://api.jsonbin.io/e/collection/5ed0e4687741ef56a563fc9c/all-bins";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_layout);
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        send = findViewById(R.id.send_button_reglogin);
        username = findViewById(R.id.editText_username);
        password = findViewById(R.id.editText_password);
        coordinatorLayout = findViewById(R.id.coordinator_reglogin);
        mProgressBar = findViewById(R.id.progressbar_reg);
        send.setOnClickListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
        toolbar.setNavigationOnClickListener(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_button_reglogin) {
            String user = username.getText().toString();
            String pw = password.getText().toString();
            if (user.length() == 0 || pw.length() == 0) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error : empty username or password. TRY AGAIN", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", user);
                    jsonObject.put("password", pw);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(0);
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (inputManager != null) {
                        inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    new sendObject().execute(jsonObject.toString());
                } catch (JSONException e) {
                    Log.e("JSON-EXCEPTION", e.toString());
                }

                Log.d("USERNAME-PASSWORD", user + " " + pw);
                return;
            }
        }
        overridePendingTransition(R.anim.scale_right_to_left, R.anim.scale_left_to_right);
        Intent intentBack = new Intent(this, MainActivity.class);
        intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentBack);
    }

    /**
     * AsyncTask for create a new file json on jsonbin.io (in a collection)
     */
    @SuppressLint("StaticFieldLeak")
    public class sendObject extends AsyncTask<String,Integer,String>{
        private boolean found;
        private boolean controlUserNamePassword(String username, String password){
            HttpURLConnection urlConnection = null;
            StringBuilder response = new StringBuilder();


            try {
                URL url = new URL(jUrl_COLLECTION);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("secret-key", secretKey);
                urlConnection.setConnectTimeout(5000); // 5 secondi per collegarsi
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
                    System.out.println("ho letto " + response_read.toString());
                    urlConnection.disconnect();
                    inRead.close();
                    inputStreamReader_read.close();

                    JSONObject jRead = new JSONObject(response_read.toString());

                    Log.d("JSON-DOWNLOADED", jRead.toString());
                    if (jRead.getString("username").equals(username) && jRead.getString("password").equals(password)) {
                        return false;
                    }

                    Log.d("REG-READ-OP", "Not found yet");
                    j++;
                }
                if (j == IDS.size()) {
                    return true;
                }

            } catch (SocketTimeoutException e){
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error : unable to connect to database.", Snackbar.LENGTH_LONG);
                snackbar.show();
                e.printStackTrace();
                Log.e("TIMEOUT-EXCEPTION", e.toString());

            } catch (IOException | JSONException e) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error : try again", Snackbar.LENGTH_LONG);
                snackbar.show();
                e.printStackTrace();
                Log.e("IO EXCEPTION", e.toString());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return false;
        }




        @Override
        protected void onPreExecute() {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Request has been sent, please wait..", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        @Override
        protected String doInBackground(String... params) {
            publishProgress(20);
            try {
                JSONObject object = new JSONObject(params[0]);
                found = controlUserNamePassword(object.getString("username"), object.getString("password"));
                if(!found){
                    publishProgress(100);
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error : username already present, try with another one", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else{
                    HttpURLConnection urlConnection;
                    StringBuilder response = new StringBuilder();


                    try {
                        publishProgress(40);
                        URL url = new URL(jUrl);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setConnectTimeout(5000); // 5 secondi per collegarsi
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("Content-Type", "application/json");
                        //Private key
                        urlConnection.setRequestProperty("secret-key", secretKey);
                        //Collection ID
                        urlConnection.setRequestProperty("collection-id", collectionID);

                        JSONObject jUsernamePassword = new JSONObject(params[0]);
                        JSONArray tickets = new JSONArray();
                        jUsernamePassword.put("tickets", tickets);
                        String name = jUsernamePassword.getString("username");
                        urlConnection.setRequestProperty("name", name);
                        urlConnection.setDoOutput(true);
                        urlConnection.connect();

                        publishProgress(60);
                        DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                        wr.writeBytes(jUsernamePassword.toString());
                        wr.flush();
                        wr.close();
                        publishProgress(80);
                        Log.d("DATA-WRITE", "Data write successfully");

                        InputStream in = urlConnection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(in);

                        int inputStreamData = inputStreamReader.read();
                        while (inputStreamData != -1) {

                            char current = (char) inputStreamData;
                            inputStreamData = inputStreamReader.read();
                            response.append(current);

                        }
                        inputStreamReader.close();
                        in.close();
                        publishProgress(100);
                        urlConnection.disconnect();

                        Log.d("RESPONSE", response.toString());


                    } catch (IOException | JSONException e) {
                        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error : try again", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        Log.e("IO EXCEPTION", e.toString());
                    }
                }

            } catch (JSONException e) {
                Log.e("JSON-OBJECT", "Error to read JSON file");
                e.printStackTrace();
            }


            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(found) {
                overridePendingTransition(R.anim.scale_right_to_left, R.anim.scale_left_to_right);
                Intent intent = new Intent(RegActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Registration succesfully done!", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
            else{
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressBar.setProgress(values[0]);
        }
    }
}
