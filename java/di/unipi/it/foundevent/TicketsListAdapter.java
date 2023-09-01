package di.unipi.it.foundevent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Tickets list adapter class.
 */
public class TicketsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {





    @SuppressLint("StaticFieldLeak")
    public static class eventViewHolder extends RecyclerView.ViewHolder{

        static TextView eventName;
        static TextView ticketsNum;
        static FloatingActionButton buttonBluetooth;
        eventViewHolder(View itemView){
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            ticketsNum = itemView.findViewById(R.id.num_ticket_purchased);
            buttonBluetooth = itemView.findViewById(R.id.bluetooth_button);

        }
    }

    private ArrayList<String> tickets;

    TicketsListAdapter(ArrayList<String> t){
        this.tickets = t;

    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tickets_purchased_item, parent, false);
        return new TicketsListAdapter.eventViewHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        //clearing the string nameplace/tickets ID from parenthesis and question marks

        String[] tokenizer = tickets.get(position).split("/");
        final String nameEvent = tokenizer[0].replace("[", "").replace("\\", "");
        final String ticketID = tokenizer[1].replace("]", "").replace("\\", "");
        String name = "NAME EVENT : " + nameEvent;
        String ID = "TICKET ID : " + ticketID;
        Log.d("TOKENIZER", nameEvent + " " + ticketID);
        eventViewHolder.eventName.setText(name);
        eventViewHolder.ticketsNum.setText(ID);
        eventViewHolder.buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = view.getContext();
                final String nameAndTicketID = nameEvent + "/" + ticketID;

                Intent intent = new Intent(context.getApplicationContext(), NfcSendMessageActivity.class);
                intent.putExtra("nameAndTicketID", nameAndTicketID);
                context.startActivity(intent);

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());
                alertDialog.setTitle("Delete ticket");
                alertDialog.setMessage("Do you want to delete this ticket?");
                alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int pos = holder.getAdapterPosition();
                        tickets.remove(pos);
                        ArrayList<String> arrayList = new ArrayList<>();
                        for(int j = 0; j < tickets.size(); j++){
                            String element = tickets.get(j).replace("[", "").replace("]", "");
                            arrayList.add(element);
                            Log.i("TICKETS ELEMENT", element);
                        }
                        SingletonTicketsAndUsername.setTickets(arrayList);
                        notifyItemRemoved(pos);
                        new updateDatabaseTickets().execute();
                        Toast.makeText(view.getContext(), "Tickets delete succesfully", Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.show();
                return true;
            }
        });
    }








    @Override
    public int getItemCount() {
        Log.d("TICKETS LIST", tickets.toString());
        if(tickets == null){
            Log.d("TICKETS NULL", "tickets is null");
            this.tickets = SingletonTicketsAndUsername.getTickets();

            if(tickets == null)
                return 0;
        }
        else {
            if (tickets.toString().equals("[]")) {
                Log.d("tickets in else", tickets.toString());
                return 0;
            }
            if (tickets.get(0).equals("[]")) {
                Log.d("tickets in equals", tickets.toString());
                return 0;
            }
        }
        return this.tickets.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private static class updateDatabaseTickets extends AsyncTask<Void,Void,Void>{
        private static final String secretKey = "$2b$10$f185HeZoCAQDhHaA27ExgenP.0ZFx4qtOlo9S2E/FJJ8sKy0eB0/S";
        @Override
        protected Void doInBackground(Void... voids) {
            //updating internal database
            MyDB db = MyDB.getInstance();
            user_tickets userTickets = new user_tickets();
            userTickets.setTickets_ID(SingletonTicketsAndUsername.getTickets());
            userTickets.setUserName(SingletonTicketsAndUsername.getUserName());
            db.eventsDao().update_user_tickets(userTickets);
            Log.d("UPDATED DATABASE", userTickets.getTickets_ID().toString());
            //updating user json file on jsonbin.io
            StringBuilder response_read = new StringBuilder();
            String READ_url = "https://api.jsonbin.io/b/";
            HttpURLConnection urlConnection_read = null;
            HttpURLConnection urlConnection_write = null;
            try {
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


                JSONArray jsonArray = new JSONArray(jRead.getJSONArray("tickets").toString());
                Log.d("TICKETS IN FILE", jsonArray.toString());
                ArrayList<String> arrayList = new ArrayList<>(SingletonTicketsAndUsername.getTickets());
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
                inputStreamReader_read_USER.close();
                inRead_USER.close();

                return null;
            } catch (IOException | JSONException e) {
                Log.e("EXCEPTION", e.toString());
                e.printStackTrace();
            } finally {
                if (urlConnection_read != null) {
                    urlConnection_read.disconnect();
                }
                if (urlConnection_write != null) {
                    urlConnection_write.disconnect();
                }
            }
            return null;
        }
    }

}


