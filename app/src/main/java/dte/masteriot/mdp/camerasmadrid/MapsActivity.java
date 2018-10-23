package dte.masteriot.mdp.camerasmadrid;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dte.masteriot.mdp.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String coordinates, cameraName;
    Float latitude, longitude;
    RadioGroup radGrp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Getting the Intent and its extras:
        Intent i = getIntent();
        coordinates = i.getStringExtra("coordinates");
        cameraName = i.getStringExtra("name");

        radGrp = findViewById(R.id.grupoRadioMapType);

        String []coord = coordinates.split(","); // coord[0] --> longitude, coord[1] --> latitude
        longitude = Float.parseFloat(coord[0]);
        latitude  = Float.parseFloat(coord[1]);

        Toast.makeText(this, "Latitude: " + coord[1] + " Longitude: " + coord[0], Toast.LENGTH_SHORT).show();

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

        Marker mk = mMap.addMarker(new MarkerOptions().position(cameraLatLng).title(cameraName));
        mk.showInfoWindow(); // Shows the name of the camera in the marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraLatLng));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );

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
