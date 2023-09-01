
package di.unipi.it.foundevent;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Class of room database (with singleton for it).
 */
@Database(entities = {Event.class, user_tickets.class}, version = 1)
@TypeConverters(Converters.class)
public abstract class MyDB extends RoomDatabase {
    public abstract EventsDao eventsDao();
    private static MyDB instance;

    public static MyDB getInstance(){
        return instance;
    }

    //Singleton
    public static MyDB setInstance(Context context){
        if(instance == null){
            synchronized (MyDB.class){
                if (instance == null) {
                    String DB_NAME = "Events";
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            MyDB.class, DB_NAME).build();
                }
            }
        }
        return instance;
    }
}
