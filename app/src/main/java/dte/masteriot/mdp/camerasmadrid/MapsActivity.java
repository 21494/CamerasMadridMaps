package dte.masteriot.mdp.camerasmadrid;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    String coordinates, cameraName, location;
    Float latitude, longitude, loc_latitude, loc_longitude;
    RadioGroup radGrp;
    String []loc_coord;
    String []coord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Getting the Intent and its extras:
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

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        LatLng cameraLatLng = new LatLng(latitude, longitude);
        LatLng locLatLng = new LatLng(loc_latitude, loc_longitude);

        Marker mk = mMap.addMarker(new MarkerOptions().position(cameraLatLng).title(cameraName));
        Marker mk_loc = mMap.addMarker(new MarkerOptions().position(locLatLng).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        mk.showInfoWindow(); // Shows the name of the camera in the marker
        mk_loc.showInfoWindow(); // Shows the name of the camera in the marker

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mk.getPosition());
        builder.include(mk_loc.getPosition());
        LatLngBounds bounds = builder.build();

        // offset from edges of the map in pixels
        int padding = 100;

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraLatLng));
       // mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        //mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );
       // mMap.animateCamera( CameraUpdateFactory.newLatLngBounds(bounds, padding) );

        getKML task = new getKML();
        task.execute(loc_coord[1], loc_coord[0], coord[1], coord[0], "motorcar", "1" );
        radGrp.setOnCheckedChangeListener(new radioGroupCheckedChanged() );
    }
    /*
    public ArrayList<LatLng> parseKML(InputStream inputStream) {

        Document document = null;
        ArrayList<LatLng> route = new ArrayList<LatLng>();

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false); factory.setIgnoringComments(true); factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Get InputStream for file located in assets folder

            document = builder.parse( inputStream );

            // Process XML document and extract names and image urls
            String []rawRoute = document.getElementsByTagName("coordinates").item(0).getTextContent().split(" ");
            for(int i = 0; i < rawRoute.length; i++){
                String[] coordenadas = rawRoute[i].split(",");
                route.add(new LatLng(Float.parseFloat(coordenadas[1]), Float.parseFloat(coordenadas[0])));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return route;
    }*/

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

class getKML extends AsyncTask<String, Void, InputStream>
{
    @Override
    protected InputStream doInBackground(String... param) {

        InputStream is = null;
        HttpURLConnection urlConnection;
        String contentType;
        try {
            URL url = new URL("http://www.yournavigation.org/api/1.0/gosmore.php"+"?format=kml&flat="
                    +param[0]+"&flon="+param[1]+"&tlat="+param[2]+"&tlon="+param[3]+"&v="+param[4]+"&fast="+param[5]);
            urlConnection = (HttpURLConnection) url.openConnection();

            int responseCode = urlConnection.getResponseCode();
            Log.d("getKML","\nSending 'GET' request to URL : " + url);
            Log.d("getKML","Response Code : " + responseCode);

            is = urlConnection.getInputStream();
            urlConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return is;
    }

    @Override
    protected void onPostExecute(InputStream is) {
        super.onPostExecute(is);

        ArrayList<LatLng> ruta = this.parseKML(is);
        Polyline line;
        for(int i=0; i < ruta.size(); i++) {
            line = MapsActivity.mMap.addPolyline(new PolylineOptions().add(ruta.get(i)).width(5).color(Color.BLUE));
        }
        MapsActivity.mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

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
}
