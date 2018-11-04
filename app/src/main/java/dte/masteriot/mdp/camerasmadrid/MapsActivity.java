package dte.masteriot.mdp.camerasmadrid;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import dte.masteriot.mdp.R;

import static dte.masteriot.mdp.camerasmadrid.MapsActivity.mMap;
import static java.lang.Math.round;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    public static Resources res;
    public static Polyline line;

    String coordinates, cameraName, location;
    Float latitude, longitude, loc_latitude, loc_longitude;
    RadioGroup radGrp;
    String []loc_coord;
    String []coord;
    private static boolean isFabOpen = false;
    private static boolean isFabRouteOpen = false;

    private FloatingActionButton fab_main;
    private FloatingActionButton fab_standard;
    private FloatingActionButton fab_satellite;
    private FloatingActionButton fab_hybrid;

    private FloatingActionButton fab_route;
    private FloatingActionButton fab_car;
    private FloatingActionButton fab_bicycle;
    private FloatingActionButton fab_foot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fab_main = findViewById(R.id.fab_main);
        fab_standard = findViewById(R.id.fab_standard);
        fab_satellite = findViewById(R.id.fab_satellite);
        fab_hybrid = findViewById(R.id.fab_hybrid);
        fab_route = findViewById(R.id.fab_route);
        fab_car = findViewById(R.id.fab_car);
        fab_bicycle = findViewById(R.id.fab_bicycle);
        fab_foot = findViewById(R.id.fab_foot);

        fab_route.setOnClickListener(new RouteOnClickListener());
        fab_car.setOnClickListener(new CarOnClickListener());
        fab_bicycle.setOnClickListener(new BicycleOnClickListener());
        fab_foot.setOnClickListener(new FootOnClickListener());
        fab_main.setOnClickListener(new MainOnClickListener());
        fab_standard.setOnClickListener(new StandardOnClickListener());
        fab_satellite.setOnClickListener(new SatelliteOnClickListener());
        fab_hybrid.setOnClickListener(new HybridOnClickListener());

        //Getting the Intent and its extras:
        res = getResources();
        Intent i = getIntent();
        coordinates = i.getStringExtra("coordinates");
        cameraName = i.getStringExtra("name");
        location = i.getStringExtra("location");

        fab_standard.setSelected(true);

        coord = coordinates.split(","); // coord[0] --> longitude, coord[1] --> latitude
        loc_coord = location.split(","); // coord[0] --> longitude, coord[1] --> latitude
        longitude = Float.parseFloat(coord[0]);
        latitude  = Float.parseFloat(coord[1]);
        loc_longitude = Float.parseFloat(loc_coord[0]);
        loc_latitude  = Float.parseFloat(loc_coord[1]);

        Toast.makeText(this, "Latitude: " + coord[1] + " Longitude: " + coord[0], Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "Location is " + location , Toast.LENGTH_SHORT).show();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    class MainOnClickListener implements View.OnClickListener
    {
        @Override
            public void onClick(View view) {
                if (!isFabOpen) {
                    ShowFabMenu();
                } else {
                    CloseFabMenu();
                }
            }
    }

    private class RouteOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!isFabRouteOpen) {
                ShowFabRouteMenu();
            } else {
                CloseFabRouteMenu();
            }
        }
    }

    private void CloseFabRouteMenu() {
        isFabRouteOpen = false;
        fab_route.animate().rotation(0f);
        fab_route.setImageResource(R.drawable.route);
        fab_car.animate()
                .translationY(0f)
                .rotation(90f);
        fab_bicycle.animate()
                .translationY(0f)
                .rotation(90f);
        fab_foot.animate()
                .translationY(0f)
                .rotation(90f);
        fab_car.setVisibility(View.GONE);
        fab_bicycle.setVisibility(View.GONE);
        fab_foot.setVisibility(View.GONE);

    }

    private void ShowFabRouteMenu() {
        isFabRouteOpen = true;
        fab_car.setVisibility(View.VISIBLE);
        fab_bicycle.setVisibility(View.VISIBLE);
        fab_foot.setVisibility(View.VISIBLE);
        fab_route.setImageResource(R.drawable.ic_plus);
        fab_route.animate().rotation(135f);
        fab_car.animate()
                .translationY((float)-1.1*getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        fab_bicycle.animate()
                .translationY((float)-2.2*getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        fab_foot.animate()
                .translationY((float)-3.3*getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        if (fab_car.isSelected()) {
            fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            fab_foot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
        else if (fab_bicycle.isSelected()) {
            fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            fab_foot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
        else if (fab_foot.isSelected()) {
            fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            fab_foot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        }

    }

    class StandardOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            CloseFabMenu();
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            fab_standard.setSelected(true);
            fab_hybrid.setSelected(false);
            fab_satellite.setSelected(false);
            Snackbar.make(view, "Normal map view", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    class SatelliteOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            CloseFabMenu();
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            fab_standard.setSelected(false);
            fab_hybrid.setSelected(false);
            fab_satellite.setSelected(true);
            Snackbar.make(view, "Satellite map view", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    class HybridOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            CloseFabMenu();
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            fab_standard.setSelected(false);
            fab_hybrid.setSelected(true);
            fab_hybrid.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            fab_satellite.setSelected(false);
            Snackbar.make(view, "Hybrid map view", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private void ShowFabMenu()
    {
        isFabOpen = true;
        fab_standard.setVisibility(View.VISIBLE);
        fab_satellite.setVisibility(View.VISIBLE);
        fab_hybrid.setVisibility(View.VISIBLE);
        fab_main.setImageResource(R.drawable.ic_plus);
        fab_main.animate().rotation(135f);
        fab_standard.animate()
                .translationY((float)1.1*getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        fab_satellite.animate()
                .translationY((float)2.2*getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        fab_hybrid.animate()
                .translationY((float)3.3*getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        if (fab_hybrid.isSelected()) {
            fab_standard.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            fab_hybrid.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            fab_satellite.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
        else if (fab_standard.isSelected()) {
            fab_standard.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            fab_hybrid.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            fab_satellite.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
        else if (fab_satellite.isSelected()) {
            fab_standard.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            fab_hybrid.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            fab_satellite.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        }

    }



    private void CloseFabMenu()
    {
        isFabOpen = false;
        fab_main.animate().rotation(0f);
        fab_main.setImageResource(R.drawable.layers);
        fab_standard.animate()
                .translationY(0f)
                .rotation(90f);
        fab_satellite.animate()
                .translationY(0f)
                .rotation(90f);
        fab_hybrid.animate()
                .translationY(0f)
                .rotation(90f);
        fab_standard.setVisibility(View.GONE);
        fab_satellite.setVisibility(View.GONE);
        fab_hybrid.setVisibility(View.GONE);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng cameraLatLng = new LatLng(latitude, longitude);
        LatLng locLatLng = new LatLng(loc_latitude, loc_longitude);

        Marker mk = mMap.addMarker(new MarkerOptions().position(cameraLatLng).title(cameraName));
        Marker mk_loc = mMap.addMarker(new MarkerOptions().position(locLatLng).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mk.showInfoWindow(); // Shows the name of the camera in the marker
        mk_loc.showInfoWindow(); // Shows the name of the camera in the marker

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mk.getPosition());
        builder.include(mk_loc.getPosition());
        LatLngBounds bounds = builder.build();

        // offset from edges of the map in pixels
        int padding = 100; //comentario

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraLatLng));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        //mMap.animateCamera( CameraUpdateFactory.newLatLngBounds(bounds, padding) );

        mMap.animateCamera( CameraUpdateFactory.zoomTo(200.0f)  );
        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(new LatLng((latitude+loc_latitude)/2,(longitude + loc_longitude)/2), 11.0f ));

        //getKML task = new getKML();
        //task.execute(loc_coord[1], loc_coord[0], coord[1], coord[0], "motorcar", "1" );
        //radGrp.setOnCheckedChangeListener(new radioGroupCheckedChanged() );
    }

    private class CarOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            CloseFabRouteMenu();
            if (line != null)
                line.remove();
            getKML task = new getKML(v);
            task.execute(loc_coord[1], loc_coord[0], coord[1], coord[0], "motorcar", "1" );
            fab_car.setSelected(true);
            fab_bicycle.setSelected(false);
            fab_foot.setSelected(false);

        }
    }

    private class BicycleOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            CloseFabRouteMenu();
            if (line != null)
                line.remove();
            getKML task = new getKML(v);
            task.execute(loc_coord[1], loc_coord[0], coord[1], coord[0], "bicycle", "1" );
            fab_car.setSelected(false);
            fab_bicycle.setSelected(true);
            fab_foot.setSelected(false);
        }
    }

    private class FootOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            CloseFabRouteMenu();
            getKML task = new getKML(v);
            if (line != null)
                line.remove();
            task.execute(loc_coord[1], loc_coord[0], coord[1], coord[0], "foot", "1" );
            fab_car.setSelected(false);
            fab_bicycle.setSelected(false);
            fab_foot.setSelected(true);

        }
    }

}

class getKML extends AsyncTask<String, Void, ArrayList<LatLng>>
{
    private String contentType;
    boolean error = false;
    private String errorMessage;

    private View rootView;

    public getKML(View rootView) {

        this.rootView = rootView;
    }

    @Override
    protected ArrayList<LatLng> doInBackground(String... param) {

        ArrayList<LatLng> ruta = null;
        HttpURLConnection urlConnection;

        try {
            URL url = new URL("http://www.yournavigation.org/api/1.0/gosmore.php"+"?format=kml&flat="
                    +param[0]+"&flon="+param[1]+"&tlat="+param[2]+"&tlon="+param[3]+"&v="+param[4]+"&fast="+param[5]);
            urlConnection = (HttpURLConnection) url.openConnection();

            int responseCode = urlConnection.getResponseCode();
            Log.d("getKML","\nSending 'GET' request to URL : " + url);
            Log.d("getKML","Response Code : " + responseCode);
            if (responseCode == urlConnection.HTTP_OK) {
                InputStream is = urlConnection.getInputStream();
                urlConnection.disconnect();
                ruta = parseKML(is);
            } else {
                error = true;
                errorMessage = "Error in the request. Server response: " + responseCode;
            }
        } catch (Exception e) {
            error = true;
            errorMessage = e.toString();
            e.printStackTrace();
        }

        return ruta;
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> ruta) {
        super.onPostExecute(ruta);
        if (error){
            Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_SHORT)
                    .show();
            Log.d("Error obteniendo ruta. ", errorMessage);
            return;
        } else {
            PolylineOptions polyline = new PolylineOptions();
            for (int i = 0; i < ruta.size() - 1; i++) {
                polyline.add(ruta.get(i), ruta.get(i + 1));
            }

            polyline.color(MapsActivity.res.getColor(R.color.colorPrimaryDark));
            MapsActivity.line = mMap.addPolyline(polyline);
        }
    }
    

    private ArrayList<LatLng> parseKML(InputStream is) {
        Document document = null;
        ArrayList<LatLng> route = new ArrayList<LatLng>();

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false); factory.setIgnoringComments(true); factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Get InputStream for file located in assets folder

            document = builder.parse( is );

            // Process XML document and extract names and image urls
            String []rawRoute = document.getElementsByTagName("coordinates").item(0).getTextContent().split("\n");
            String[] coordenadas;
            for(int i = 0; i < rawRoute.length -1; i++){
                coordenadas = rawRoute[i].split(",");
                route.add(new LatLng(Float.parseFloat(coordenadas[1]), Float.parseFloat(coordenadas[0])));
            }
            String distancia = String.format("%.2f", Float.parseFloat(document.getElementsByTagName("distance").
                    item(0).getTextContent()));
            String  tiempo_str = document.getElementsByTagName("traveltime").item(0).getTextContent();
            String tiempo = String.format("%d", round(Float.parseFloat(tiempo_str)/60));
            Snackbar.make(rootView, "Route calculated in " + tiempo + " min for " + distancia + " km", Snackbar.LENGTH_LONG)
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return route;
    }

    ///////////////////////////////////////////////////////////
    // Floating menu //////////////////////////////////////////
    ///////////////////////////////////////////////////////////


}
