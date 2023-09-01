package di.unipi.it.foundevent;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Event's table.
 */
// URL : https://api.jsonbin.io/b/5eb12cc247a2266b14731309
@Entity(tableName = Event.TABLE_NAME)
public class Event implements Serializable {
    static final String TABLE_NAME = "EVENTS";

    @PrimaryKey
    @ColumnInfo
    private int _ID;


    int get_ID() {
        return _ID;
    }

    void set_ID(int _ID) {
        this._ID = _ID;
    }

    @ColumnInfo
    private String eventName;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @ColumnInfo
    private String placeName;

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    @ColumnInfo
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @ColumnInfo
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @ColumnInfo
    private String startTime;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @ColumnInfo
    private String endTime;

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @ColumnInfo
    private String managerName;

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    @ColumnInfo
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ColumnInfo
    private int placesAvailable;

    public int getPlacesAvailable() {
        return placesAvailable;
    }

    public void setPlacesAvailable(int placesAvailable) {
        this.placesAvailable = placesAvailable;
    }

    @ColumnInfo
    private double longitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @ColumnInfo
    private double latitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }



}
