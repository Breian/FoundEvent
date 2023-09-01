package di.unipi.it.foundevent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Activity that show the google map (where we can draw a zone to search some events in).
 */
public class EventMap extends FragmentActivity implements OnTouchListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    ArrayList<Event> events;
    private static final String TAG = "polygon";
    private GoogleMap mGoogleMap;
    private View mMapShelterView;
    // Detects various gestures and events using the supplied MotionEvents.
    // The OnGestureListener callback will notify users when a particular motion event has occurred.
    private GestureDetector mGestureDetector;
    private ArrayList<LatLng> mLatlngs = new ArrayList<>();
    private PolylineOptions mPolylineOptions;
    private PolygonOptions mPolygonOptions;

    // flag to differentiate whether user is touching to draw or not
    private boolean mDrawFinished = false;
    private final static int GOOGLE_PLAY_SERVICES_RESOLUTION_REQUEST = 9233;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        Intent intent = getIntent();
        events = (ArrayList<Event>) intent.getSerializableExtra("list");
        mMapShelterView = findViewById(R.id.drawer_view);
        TextView foundEvent = findViewById(R.id.found_event);

        //ResourceCompat and Typeface for applying faster_one font dynamically
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.faster_one);
        foundEvent.setTypeface(typeface);

        Toolbar toolbar = findViewById(R.id.toolbar_map);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back, null));
        toolbar.setNavigationOnClickListener(this);
        if(this.getActionBar() != null)
            this.getActionBar().setDisplayShowTitleEnabled(false);

        mGestureDetector = new GestureDetector(this, new GestureListener());
        //add listener to the view
        if(mMapShelterView != null) {
            mMapShelterView.setOnTouchListener(this);
        }
        initializeMap();

    }

    /**
     * @param latitude : latitude of the location
     * @param longitude : longitude of the location
     * @return : the exact address giving by latitude and longitude
     */
    private String getAddressFromLatLng(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 2);
            if (addresses.size() > 0) {

                Address address = addresses.get(0);
                result.append(address.getAddressLine(0));
            }
        } catch (IOException e) {
            Log.e("IO EXCEPTION", e.toString());
        }

        return result.toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mGoogleMap.setBuildingsEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Event event = new Event();
        Log.d("MARKER", marker.getTitle());
        for(int i = 0; i < events.size(); i++){
            if(events.get(i).getEventName().equals(marker.getTitle())){
                event = events.get(i);
                break;
            }
        }
        Intent intent = new Intent(getApplicationContext(), ContentActivity.class);
        intent.putExtra("event", event);
        startActivity(intent);

        return true;
    }

    @Override
    public void onClick(View view) {
        FragmentActivity fragmentActivity = this;
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        fragmentActivity.onBackPressed();
    }


    private static final class GestureListener extends SimpleOnGestureListener {
        /**
         * Notified when a tap occurs with the down MotionEvent that triggered it.
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        /**
         * Notified of a fling event when it occurs with the initial on down MotionEvent and the matching up MotionEvent.
         * The calculated velocity is supplied along the x and y axis in pixels per second.
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return false;
        }
    }

    /**
     * Ontouch event will draw poly line along the touch points
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        int X1 = (int) event.getX();
        int Y1 = (int) event.getY();
        Point point = new Point();
        point.x = X1;
        point.y = Y1;

        LatLng firstGeoPoint = mGoogleMap.getProjection().fromScreenLocation(
                point);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                if (mDrawFinished) {
                    X1 = (int) event.getX();
                    Y1 = (int) event.getY();
                    point = new Point();
                    point.x = X1;
                    point.y = Y1;
                    LatLng geoPoint = mGoogleMap.getProjection()
                            .fromScreenLocation(point);
                    mLatlngs.add(geoPoint);
                    //showing drawn line on map
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.RED);
                    mPolylineOptions.width(3);
                    mPolylineOptions.addAll(mLatlngs);
                    mGoogleMap.addPolyline(mPolylineOptions);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "Points array size " + mLatlngs.size());
                mLatlngs.add(firstGeoPoint);
                mGoogleMap.clear();
                mPolylineOptions = null;
                mMapShelterView.setVisibility(View.GONE);
                mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
                mGoogleMap.getUiSettings().setAllGesturesEnabled(true);

                //showing polygon on map
                mPolygonOptions = new PolygonOptions();
                mPolygonOptions.fillColor(Color.TRANSPARENT);
                mPolygonOptions.strokeColor(Color.RED);
                mPolygonOptions.strokeWidth(5);
                mPolygonOptions.addAll(mLatlngs);
                mGoogleMap.addPolygon(mPolygonOptions);
                mDrawFinished = false;

                //take longitude and latitude of all the events
                for(int i = 0; i < events.size(); i++) {
                    LatLng location = new LatLng(events.get(i).getLatitude(), events.get(i).getLongitude());
                    Log.d("LATLNG", "LAT : " + location.latitude + " , LNG : " + location.longitude);
                    //if the drawn zone contains the event, then a marker is added (to show exactly where is the event)
                    if (containsEvent(location)) {
                        Log.d("FOUND LOCATION", "I have found a location");
                        MarkerOptions markerOptions = new MarkerOptions();
                        mGoogleMap.addMarker(markerOptions.position(location).title(events.get(i).getEventName()));
                        mGoogleMap.setOnMarkerClickListener(this);
                        }
                    }

                break;
        }


        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * Setting up map
     */
    private void initializeMap() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SUCCESS) {
            if (mGoogleMap == null) {

                SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
                if (supportMapFragment != null) {
                    supportMapFragment.getMapAsync(this);
                }

            }

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(status)){
            GoogleApiAvailability.getInstance().getErrorDialog(this, status, GOOGLE_PLAY_SERVICES_RESOLUTION_REQUEST);
        } else {
            Toast.makeText(this, "No Support for Google Play Service",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method gets called on tap of draw button, It prepares the screen to draw
     * the polygon
     *
     * @param view : zone
     */

    public void drawZone(View view) {
        mGoogleMap.clear();
        mLatlngs.clear();
        mPolylineOptions = null;
        mPolygonOptions = null;
        mDrawFinished = true;
        mMapShelterView.setVisibility(View.VISIBLE);
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
    }


    /**
     * @param latLng : location on map
     * @return : true if location is inside de polygon, else false
     */
    public synchronized boolean containsEvent(LatLng latLng) {
        boolean isInside = false;
        if (mLatlngs.size() > 0) {
            LatLng lastPoint = mLatlngs.get(mLatlngs.size() - 1);

            double x = latLng.longitude;

            for (LatLng point : mLatlngs) {
                double x1 = lastPoint.longitude;
                double x2 = point.longitude;
                double dx = x2 - x1;
                Log.d("X1, X2 POINTS", x1 + " " + x2);
                if (Math.abs(dx) > 180.0) {
                    if (x > 0) {
                        while (x1 < 0)
                            x1 += 360;
                        while (x2 < 0)
                            x2 += 360;
                    } else {
                        while (x1 > 0)
                            x1 -= 360;
                        while (x2 > 0)
                            x2 -= 360;
                    }
                    dx = x2 - x1;
                }

                if ((x1 <= x && x2 > x) || (x1 >= x && x2 < x)) {
                    Log.d("IF CONTAINS", "event founded");
                    double grad = (point.latitude - lastPoint.latitude) / dx;
                    double intersectAtLat = lastPoint.latitude
                            + ((x - x1) * grad);

                    if (intersectAtLat > latLng.latitude)
                        isInside = !isInside;
                }
                lastPoint = point;
            }
        }

        return isInside;
    }

}