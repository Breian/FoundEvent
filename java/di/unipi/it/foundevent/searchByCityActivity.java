package di.unipi.it.foundevent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Activity that search the events in the database, based on location name.
 */
public class searchByCityActivity extends AppCompatActivity implements View.OnClickListener {
     CoordinatorLayout coordinatorlayout;
     FloatingActionButton search;
     EditText editText;
     ArrayList<Event> eventsList;
     ArrayList<Event> eventsFounded;


     @Override
     protected void onCreate(Bundle saveInstanceState) {
         super.onCreate(saveInstanceState);
         setContentView(R.layout.search_by_city_layout);
         search = findViewById(R.id.send_button);
         search.setOnClickListener(this);
         editText = findViewById(R.id.edittext_city);
         coordinatorlayout = findViewById(R.id.coordinator_city);
         overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);

         Toolbar toolbar = findViewById(R.id.toolbar);
         setSupportActionBar(toolbar);
         toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
         toolbar.setNavigationOnClickListener(this);
         Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
         Intent intent = getIntent();

         eventsFounded = new ArrayList<>();
         eventsList = (ArrayList<Event>) intent.getSerializableExtra("list");
         if (eventsList != null) {
             Log.d("EVENTS-LIST", "list is not null");
         } else {
             Log.d("EVENTS-LIST", "list is null");
         }
     }

     @Override
     public void onClick(View view) {
         if (view.getId() == R.id.send_button) {
             String city = editText.getText().toString().toLowerCase();
             if (city.length() == 0) {
                 Snackbar snackbar = Snackbar.make(coordinatorlayout, "Error : empty location's name. Try again!", Snackbar.LENGTH_LONG);
                 snackbar.show();
             } else {
                 for (int i = 0; i < eventsList.size(); i++) {
                     if (city.equals(eventsList.get(i).getPlaceName())) {
                         eventsFounded.add(eventsList.get(i));
                     }
                 }
             }
             if (eventsFounded.isEmpty()) {
                 Snackbar snackbar = Snackbar.make(coordinatorlayout, "No such event in this location.", Snackbar.LENGTH_LONG);
                 snackbar.show();
             } else {

                 Intent intent = new Intent(this, RecyclerViewEventsActivity.class);
                 intent.putExtra("list", eventsFounded);

                 startActivity(intent);
             }
         } else {
             Intent intentBack = new Intent(this, FoundEventActivity.class);
             intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intentBack);
         }
     }
 }
