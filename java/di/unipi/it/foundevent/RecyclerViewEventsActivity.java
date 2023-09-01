package di.unipi.it.foundevent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * RecyclerView for the event's list.
 */
public class RecyclerViewEventsActivity extends AppCompatActivity implements View.OnClickListener {
    String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_list);
        Intent intent = getIntent();
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        userName = intent.getStringExtra("userName");
        ArrayList<Event> eventsList = (ArrayList<Event>) intent.getSerializableExtra("list");
        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        EventsListAdapter adapter = new EventsListAdapter(eventsList, userName);
        rv.setAdapter(adapter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
        toolbar.setNavigationOnClickListener(this);


    }

    @Override
    public void onClick(View view) {

        Intent intentBack = new Intent(this, FoundEventActivity.class);
        intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentBack.putExtra("userName", userName);
        startActivity(intentBack);
    }
}
