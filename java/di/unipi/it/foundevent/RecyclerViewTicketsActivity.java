package di.unipi.it.foundevent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Objects;

/**
 * RecyclerView for tickets list.
 */
public class RecyclerViewTicketsActivity extends AppCompatActivity implements View.OnClickListener {
    //broadcast receiver to listen nfc message
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
                Intent intent1 = new Intent(context, NfcReceiveMessageActivity.class);
                intent.putExtras(intent);
                startActivity(intent1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_list);
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        ArrayList<String> ticketsList = SingletonTicketsAndUsername.getTickets();

        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        if (ticketsList != null) {
            if(ticketsList.size() != 0) {
                Log.d("TICKETS LIST", ticketsList.toString() + ", size: " + ticketsList.size());
                ticketsList = fitterTickets(ticketsList);
            }

        }
        TicketsListAdapter adapter = new TicketsListAdapter(ticketsList);
        rv.setAdapter(adapter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
        toolbar.setNavigationOnClickListener(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

    }

    /**
     * Handle onNewIntent() to inform the fragment manager that the
     * state is not saved.  If you are handling new intents and may be
     * making changes to the fragment state, you want to be sure to call
     * through to the super-class here first.  Otherwise, if your state
     * is saved but the activity is not stopped, you could get an
     * onNewIntent() call which happens before onResume() and trying to
     * perform fragment operations at that point will throw IllegalStateException
     * because the fragment manager thinks the state is still saved.
     *
     * @param intent : nfc intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        Intent intentBack = new Intent(this, FoundEventActivity.class);
        intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentBack);
    }
    /**
     * @param tickets : the original ArrayList of tickets numbers
     * @return : the same ArrayList but with correctly numbered elements (because in the original all elements are seen as one)
     */
    private ArrayList<String> fitterTickets(ArrayList<String> tickets){
        String[] tokenizer = tickets.get(0).split(",");
        ArrayList<String> tickets_new = new ArrayList<>();
        for (String s : tokenizer) {
            tickets_new.add(s);
        }
        return tickets_new;
    }
}
