package di.unipi.it.foundevent;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * Table that contains, for each user, the tickets list associated with it.
 */
@Entity(tableName = user_tickets.TABLE_NAME)
public class user_tickets implements Serializable {
    protected static final String TABLE_NAME = "USER_TICKETS";
    @PrimaryKey
    @ColumnInfo
    @NonNull
    private String userName;

    @NonNull
    String getUserName() {
        return userName;
    }

    public void setUserName(@NonNull String userName) {
        this.userName = userName;
    }

    @ColumnInfo
    private ArrayList<String> tickets_ID;

    public ArrayList<String> getTickets_ID() {
        return tickets_ID;
    }

    public void setTickets_ID(ArrayList<String> tickets_ID) {
        this.tickets_ID = tickets_ID;
    }
}
