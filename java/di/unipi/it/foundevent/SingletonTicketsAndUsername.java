package di.unipi.it.foundevent;

import java.util.ArrayList;

/**
 * Class that contains the username, the tickets list associated with username, and the users's BIN ID.
 */
public class SingletonTicketsAndUsername {

    public static String getUserName() {
        return userName;
    }

    public synchronized static void setUserName(String userN) {
        userName = userN;
    }

    public static ArrayList<String> getTickets() {
        return tickets;
    }

    public synchronized static void setTickets(ArrayList<String> t) {
        tickets = t;
    }

    public static String getBinIdUser() {
        return BIN_ID_USER;
    }

    public synchronized static void setBinIdUser(String binIdUser) {
        BIN_ID_USER = binIdUser;
    }

    protected static String userName;
    protected static ArrayList<String> tickets;
    protected static String BIN_ID_USER;

}
