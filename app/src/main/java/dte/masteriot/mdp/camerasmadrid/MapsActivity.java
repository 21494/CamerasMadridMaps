package dte.masteriot.mdp.camerasmadrid;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    public static Resources res;
    String coordinates, cameraName, location;
    Float latitude, longitude, loc_latitude, loc_longitude;
    RadioGroup radGrp;
    String []loc_coord;
    String []coord;
    private static boolean isFabOpen = false;
    private FloatingActionButton fab_main;
    private ImageView button_standard;
    private ImageView button_satellite;
    private ImageView button_hybrid;
    private TextView text_standard;
    private TextView text_satellite;
    private TextView text_hybrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fab_main = findViewById(R.id.fab_main);
        button_standard = findViewById(R.id.button_standard);
        button_satellite = findViewById(R.id.button_satellite);
        button_hybrid = findViewById(R.id.button_hybrid);
        text_standard = findViewById(R.id.text_standard);
        text_satellite = findViewById(R.id.text_satellite);
        text_hybrid = findViewById(R.id.text_hybrid);

        fab_main.setOnClickListener(new MainOnClickListener());
        button_standard.setOnClickListener(new StandardOnClickListener());
        button_satellite.setOnClickListener(new SatelliteOnClickListener());
        button_hybrid.setOnClickListener(new HybridOnClickListener());

        //Getting the Intent and its extras:
        res = getResources();
        Intent i = getIntent();
        coordinates = i.getStringExtra("coordinates");
        cameraName = i.getStringExtra("name");
        location = i.getStringExtra("location");

        radGrp = findViewById(R.id.grupoRadioMapType);

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

    class StandardOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            CloseFabMenu();
            Toast.makeText(MapsActivity.this, "Metodo para pasar a mapa standard ", Toast.LENGTH_SHORT).show();
        }
    }

    class SatelliteOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            CloseFabMenu();
            Toast.makeText(MapsActivity.this, "Metodo para pasar a mapa satelite ", Toast.LENGTH_SHORT).show();
        }
    }

    class HybridOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            CloseFabMenu();
            Toast.makeText(MapsActivity.this, "Metodo para pasar a mapa hibrido ", Toast.LENGTH_SHORT).show();
        }
    }

    private void ShowFabMenu()
    {
        isFabOpen = true;
        button_standard.setVisibility(View.VISIBLE);
        button_satellite.setVisibility(View.VISIBLE);
        button_hybrid.setVisibility(View.VISIBLE);
        text_standard.setVisibility(View.VISIBLE);
        text_satellite.setVisibility(View.VISIBLE);
        text_hybrid.setVisibility(View.VISIBLE);
        fab_main.animate().rotation(135f);
        button_standard.animate()
                .translationY(1*getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        button_satellite.animate()
                .translationY(2*getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        button_hybrid.animate()
                .translationY(3*getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
    }

    private void CloseFabMenu()
    {
        isFabOpen = false;
        fab_main.animate().rotation(0f);
        button_standard.animate()
                .translationY(0f)
                .rotation(90f);
        button_satellite.animate()
                .translationY(0f)
                .rotation(90f);
        button_hybrid.animate()
                .translationY(0f)
                .rotation(90f);
        button_standard.setVisibility(View.GONE);
        button_satellite.setVisibility(View.GONE);
        button_hybrid.setVisibility(View.GONE);
        text_standard.setVisibility(View.GONE);
        text_satellite.setVisibility(View.GONE);
        text_hybrid.setVisibility(View.GONE);
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

        getKML task = new getKML();
        task.execute(loc_coord[1], loc_coord[0], coord[1], coord[0], "motorcar", "1" );
        radGrp.setOnCheckedChangeListener(new radioGroupCheckedChanged() );
    }


    // Listener related to the user choosing a different map type (through the radio buttons)
    class radioGroupCheckedChanged implements RadioGroup.OnCheckedChangeListener {
        public void onCheckedChanged(RadioGroup arg0, int id) {
            switch (id)
            {
                case R.id.typeMap:
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case R.id.typeSatellite:
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case R.id.typeHybrid:
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
            }
        }
    }
}

class getKML extends AsyncTask<String, Void, ArrayList<LatLng>>
{
    @Override
    protected ArrayList<LatLng> doInBackground(String... param) {

        ArrayList<LatLng> ruta = null;
        HttpURLConnection urlConnection;
        String contentType;
        try {
            URL url = new URL("http://www.yournavigation.org/api/1.0/gosmore.php"+"?format=kml&flat="
                    +param[0]+"&flon="+param[1]+"&tlat="+param[2]+"&tlon="+param[3]+"&v="+param[4]+"&fast="+param[5]);
            urlConnection = (HttpURLConnection) url.openConnection();

            int responseCode = urlConnection.getResponseCode();
            Log.d("getKML","\nSending 'GET' request to URL : " + url);
            Log.d("getKML","Response Code : " + responseCode);

            InputStream is = urlConnection.getInputStream();
            urlConnection.disconnect();
            ruta = parseKML(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ruta;
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> ruta) {
        super.onPostExecute(ruta);

        PolylineOptions polyline = new PolylineOptions();
        for(int i=0; i < ruta.size()-1; i++) {
            polyline.add(ruta.get(i), ruta.get(i+1));
        }

        polyline.color(MapsActivity.res.getColor(R.color.colorPrimaryDark));
        Polyline line = mMap.addPolyline(polyline);

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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return route;
    }

    ///////////////////////////////////////////////////////////
    // Floating menu //////////////////////////////////////////
    ///////////////////////////////////////////////////////////


}
