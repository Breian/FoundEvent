package di.unipi.it.foundevent;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import android.view.View;

import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;

import androidx.appcompat.widget.Toolbar;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


/**
 * Activity that shows informations about the selected event.
 */
public class ContentActivity extends FragmentActivity implements View.OnClickListener{

    FloatingActionButton purchaseButton;
    Event event;



    /**
     * Show the dialog with spinner to choose the tickets number that user want to purchase
     * @param tickets : list of purchasable tickets showed in spinner
     */
    private void showTicketsDialog(ArrayList<Integer> tickets) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TicketsDialog ticketsDialog = new TicketsDialog(tickets, getApplicationContext(), event.getEventName());
        ticketsDialog.show(fragmentManager, "PURCHASE");
    }

    /**
     * Perform initialization of all fragments.
     *
     * @param savedInstanceState : bundle
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_full_content);
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        Intent intent = getIntent();
        event = (Event) intent.getSerializableExtra("event");

        purchaseButton = findViewById(R.id.purchase);
        purchaseButton.setOnClickListener(this);
        TextView foundEvent = findViewById(R.id.found_event);

        //ResourceCompat and Typeface for applying fonts dynamically
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.faster_one);
        foundEvent.setTypeface(typeface);

        TextView bookTickets = findViewById(R.id.book_tickets);
        Typeface typefaceBook = ResourcesCompat.getFont(getApplicationContext(), R.font.gruppo);
        Typeface typefaceTextViews = ResourcesCompat.getFont(getApplicationContext(), R.font.megrim);
        Typeface typefaceTextViews_info = ResourcesCompat.getFont(getApplicationContext(), R.font.graduate);
        Typeface typefaceTextView_nameEvent = ResourcesCompat.getFont(getApplicationContext(), R.font.marcellus_sc);
        bookTickets.setTypeface(typefaceBook);

        TextView eventName = findViewById(R.id.event_name_text_item);
        eventName.setText(event.getEventName());
        eventName.setTypeface(typefaceTextView_nameEvent);



        TextView date = findViewById(R.id.date_item);
        date.setText(event.getDate());
        date.setTypeface(typefaceTextViews);

        TextView start = findViewById(R.id.start_time_item);
        start.setText(event.getStartTime());
        start.setTypeface(typefaceTextViews);

        TextView end = findViewById(R.id.end_time_item);
        end.setText(event.getEndTime());
        end.setTypeface(typefaceTextViews);

        TextView description = findViewById(R.id.description_item);
        description.setText(event.getDescription());
        description.setTypeface(typefaceTextViews);

        Log.d("NAME EVENT", eventName.toString());

        TextView textView_eventsName = findViewById(R.id.textView_eventsname);
        textView_eventsName.setTypeface(typefaceTextViews_info);

        TextView textView_date = findViewById(R.id.textView_date);
        textView_date.setTypeface(typefaceTextViews_info);

        TextView textView_startTime = findViewById(R.id.textView_starttime);
        textView_startTime.setTypeface(typefaceTextViews_info);

        TextView textView_endTime = findViewById(R.id.textView_endtime);
        textView_endTime.setTypeface(typefaceTextViews_info);

        TextView textView_description = findViewById(R.id.textView_descr);
        textView_description.setTypeface(typefaceTextViews_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
        toolbar.setNavigationOnClickListener(this);
        if(this.getActionBar() != null)
            this.getActionBar().setDisplayShowTitleEnabled(false);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.purchase) {
            if (event.getPlacesAvailable() == 0) {
                Toast.makeText(getApplicationContext(), "No tickets available for this event, sorry!", Toast.LENGTH_LONG).show();

            } else {
                ArrayList<Integer> tickets = new ArrayList<>();
                int ticketsAvailable = event.getPlacesAvailable();
                int count = 1;
                for (int i = 0; i < ticketsAvailable; i++) {
                    tickets.add(count);
                    count++;
                }
                showTicketsDialog(tickets);

            }
        } else {
            FragmentActivity fragmentActivity = this;
            fragmentActivity.finish();
            fragmentActivity.onBackPressed();
        }
    }


}
