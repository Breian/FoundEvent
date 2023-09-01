package di.unipi.it.foundevent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Activity that contains the button "search by city", "search by map", "view all events", "tickets purchased".
 */
public class FoundEventActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, DialogInterface.OnClickListener {
    private boolean connError = false;
    private long timePassed;
    private static MyDB db;
    CoordinatorLayout coordinatorlayout;
    TextView textView_city;
    EditText editText_city;
    FloatingActionButton googleMaps;
    FloatingActionButton searchByName;
    FloatingActionButton allEvents;
    FloatingActionButton ticketsPurchased;
    SwipeRefreshLayout swipeRefreshLayout;
    static List<Event> eventsList;
    private static final int REQUEST_CODE = 5673;
    private static final String secretKey = "$2b$10$f185HeZoCAQDhHaA27ExgenP.0ZFx4qtOlo9S2E/FJJ8sKy0eB0/S";
    private static final String jUrl = "https://api.jsonbin.io/b/5eb12cc247a2266b14731309/latest";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.found_event_layout);
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        textView_city = findViewById(R.id.textview_city);
        editText_city = findViewById(R.id.edittext_city);
        db = MyDB.getInstance();

        coordinatorlayout = findViewById(R.id.coordinator);
        allEvents = findViewById(R.id.cerca_tutti);
        allEvents.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
        toolbar.setNavigationOnClickListener(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        googleMaps = findViewById(R.id.cerca_google);
        swipeRefreshLayout = findViewById(R.id.swiperefresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        searchByName = findViewById(R.id.cerca_nome);
        searchByName.setOnClickListener(this);
        googleMaps.setOnClickListener(this);
        ticketsPurchased = findViewById(R.id.tickets_purchased);
        ticketsPurchased.setOnClickListener(this);
        Log.d("username", SingletonTicketsAndUsername.getUserName());
        new getDatabase().execute(jUrl);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cerca_nome:
                if(eventsList != null) {
                    ArrayList<Event> arrayList = new ArrayList<>(eventsList);
                    Intent intent = new Intent(FoundEventActivity.this, searchByCityActivity.class);
                    // put the event's list
                    intent.putExtra("list", arrayList);
                    startActivity(intent);
                }
                else{
                    Snackbar snackbar = Snackbar.make(coordinatorlayout, "Please wait, database started to update!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
            case R.id.cerca_google:
                if(eventsList!= null) {
                    int permissionCheck = ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        // ask permissions here using below code
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_CODE);
                    }
                    if(permissionCheck == PackageManager.PERMISSION_GRANTED) {

                        ArrayList<Event> arrayList = new ArrayList<>(eventsList);
                        Intent intent = new Intent(this, EventMap.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("list", arrayList);

                        startActivity(intent);
                        break;
                    }
                    else{
                        Snackbar snackbar = Snackbar.make(coordinatorlayout, "Error : can't open the map because of no permission", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
                else{
                    Snackbar snackbar = Snackbar.make(coordinatorlayout, "Please wait, database started to update!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
            case R.id.cerca_tutti :
                if (eventsList != null) {
                    ArrayList<Event> eventArrayList = new ArrayList<>(eventsList);
                    Intent intent = new Intent(this, RecyclerViewEventsActivity.class);
                    intent.putExtra("list", eventArrayList);

                    startActivity(intent);
                }
                else {
                    Snackbar snackbar = Snackbar.make(coordinatorlayout, "Please wait, database started to update!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
            case R.id.tickets_purchased :


                if(SingletonTicketsAndUsername.getUserName() != null){
                    Intent intent = new Intent(this, RecyclerViewTicketsActivity.class);
                    startActivity(intent);
                    break;
                }
                else{
                    Snackbar snackbar = Snackbar.make(coordinatorlayout, "Error on showing tickets, please login again", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    break;
                }
            default:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setMessage("Are you sure to go back? Then you have to login again!").setPositiveButton("Yes", this).setNegativeButton("No", this).show();

        }
    }

    /**
     * Function that prints the events list
     * @param list : event's list
     */
    protected static void printList(List<Event> list){
        if(list!= null) {
            for (int i = 0; i < list.size(); i++) {
                Log.d("LIST-ELEMENTS", "\nID : " + list.get(i).get_ID() + ";\n EventName : " + list.get(i).getEventName() + ";\nPlaceName : " + list.get(i).getPlaceName() + ";\nAddress : " + list.get(i).getAddress() + ";\nDate : "
                        + list.get(i).getDate() + ";\nStartTime : " + list.get(i).getStartTime() + ";\nEndTime : " + list.get(i).getEndTime() + ";\nManagerName : " + list.get(i).getManagerName()
                        + ";\nDescription : " + list.get(i).getDescription() + ";\nplaceAvailable : " + list.get(i).getPlacesAvailable() + ";\nLatitude : " + list.get(i).getLatitude() + ";\nLongitude : " + list.get(i).getLongitude());
            }
        }
    }


    /**
     * When refreshing, if the database was updated more than 20 seconds ago, then it will be updated, otherwise not
     */
    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);

        if(!connError && System.currentTimeMillis() - timePassed < 20000) {
            Snackbar snackbar = Snackbar.make(coordinatorlayout, "Database already updated!", Snackbar.LENGTH_LONG);
            snackbar.show();

        }
        else {
            swipeRefreshLayout.setRefreshing(true);
            Log.d("REFRESH", "refreshing...");
            new getDatabase().execute(jUrl);
        }

    }

    /**
     * Dialog which is shown when you press the arrow to go back
     * @param dialogInterface : dialog interface
     * @param i : button "yes" or "no"
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i){
            case DialogInterface.BUTTON_POSITIVE:
                //Yes button clicked
                Intent intentBack = new Intent(this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentBack);
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                //No button clicked
                break;
        }
    }

    /**
     * AsyncTask for update internal database.
     */
    @SuppressLint("StaticFieldLeak")
    public class getDatabase extends AsyncTask<String, Void, Void> {



        @Override
        protected Void doInBackground(String... jUrl) {
            HttpURLConnection urlConnection;
            JSONObject jsonObject;
            try {
                URL url = new URL(jUrl[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("secret-key", secretKey);
                urlConnection.setConnectTimeout(5000); // 5 secondi per collegarsi
                urlConnection.setRequestMethod("GET");


                urlConnection.connect();

                Log.d("GET-REQUEST", "connection established");
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }

                    bufferedReader.close();

                    Log.d("GET-RESPONSE", builder.toString());
                    JSONArray jsonArray = new JSONArray(builder.toString());
                    int i;

                    for (i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        Log.d("PLACES", String.valueOf(jsonObject.get("placesAvailable")));
                        Event event = new Event();

                        event.set_ID(jsonObject.getInt("id"));
                        event.setEventName(jsonObject.getString("eventName"));
                        event.setPlaceName(jsonObject.getString("placeName"));
                        event.setAddress(jsonObject.getString("address"));
                        event.setDate(jsonObject.getString("date"));
                        event.setStartTime(jsonObject.getString("startTime"));
                        event.setEndTime(jsonObject.getString("endTime"));
                        event.setManagerName(jsonObject.getString("managerName"));
                        event.setDescription(jsonObject.getString("description"));
                        event.setPlacesAvailable(jsonObject.getInt("placesAvailable"));
                        event.setLatitude(jsonObject.getDouble("latitude"));
                        event.setLongitude(jsonObject.getDouble("longitude"));

                        if(db.eventsDao() == null)
                            db = MyDB.getInstance();
                        db.eventsDao().insert_event(event);

                    }

                    eventsList = db.eventsDao().getAllEvents();
                    printList(eventsList);
                    SingletonTicketsAndUsername.setTickets(new ArrayList<>(db.eventsDao().getTicketsFromUser(SingletonTicketsAndUsername.getUserName())));
                    Log.d("TICKETS LIST SINGLETON", SingletonTicketsAndUsername.getTickets().toString());
                    if(SingletonTicketsAndUsername.getTickets().contains("[]")){
                        for (Iterator<String> iterator = SingletonTicketsAndUsername.getTickets().iterator(); iterator.hasNext(); ) {
                            String element = iterator.next();
                            Log.d("element", element);
                            if (element.equals("[]")) {
                                iterator.remove();
                            }
                        }
                    }

                }  finally {
                    urlConnection.disconnect();
                }


            } catch (SocketTimeoutException | NullPointerException e) {
                Snackbar snackbar = Snackbar.make(coordinatorlayout, "Error : connection error. Please verify your connection and swipe for trying again!", Snackbar.LENGTH_LONG);
                snackbar.show();
                Log.e("CONNECTION ERROR", "Error to connect to database");
                e.printStackTrace();
                connError = true;
            } catch (IOException | JSONException e) {
                Log.e("ERROR", e.toString());
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void s) {
            timePassed = System.currentTimeMillis();
            Log.d("POST-EXECUTE", "Post execute");
            swipeRefreshLayout.setRefreshing(false);
        }

    }

}