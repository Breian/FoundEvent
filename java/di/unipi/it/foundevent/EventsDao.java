package di.unipi.it.foundevent;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Interface of database.
 */
@Dao
public interface EventsDao {
    @Query("SELECT * FROM EVENTS WHERE placeName LIKE :search")
    List<Event> findEventWithCityName(String search);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert_event(Event event);

    @Query("SELECT * FROM EVENTS")
    List<Event> getAllEvents();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert_ticket(user_tickets uT);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update_user_tickets(user_tickets uT);

    @Query("SELECT tickets_ID FROM user_tickets WHERE userName LIKE :name")
    List<String> getTicketsFromUser(String name);
}
