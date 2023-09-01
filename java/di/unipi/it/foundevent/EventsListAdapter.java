package di.unipi.it.foundevent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

/**
 * Adapter for the tickets list.
 */
public class EventsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    /**
     * Class that contains the main fields of a ticket.
     */
    @SuppressLint("StaticFieldLeak")
    public static class eventViewHolder extends RecyclerView.ViewHolder{

        static TextView eventName;
        static TextView eventManager;
        static TextView eventAddress;

        eventViewHolder(View itemView){
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventManager = itemView.findViewById(R.id.event_manager);
            eventAddress = itemView.findViewById(R.id.event_address);


        }
    }

    private ArrayList<Event> events;
    private String userName;

    EventsListAdapter(ArrayList<Event> list, String user){
        this.events = list;
        this.userName = user;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_item, parent, false);
        return new eventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        String name = "NAME : " + events.get(position).getEventName();
        String manager = "MANAGER : " + events.get(position).getManagerName();
        String address = "ADDRESS : " + events.get(position).getAddress();
        eventViewHolder.eventName.setText(name);
        eventViewHolder.eventManager.setText(manager);
        eventViewHolder.eventAddress.setText(address);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, ContentActivity.class);
                intent.putExtra("event", events.get(position));
                intent.putExtra("userName", userName);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.events.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
