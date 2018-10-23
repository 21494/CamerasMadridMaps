package dte.masteriot.mdp.camerasmadrid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dte.masteriot.mdp.R;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {

    private static final String URL_CAMERAS = "http://informo.madrid.es/informo/tmadrid/CCTV.kml";
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private ListView listView;
    private ImageView imageView;
    private ArrayAdapter adapter;

    // This cbject contains the list of cameras together with their info:
    private DataModel dataModel = new DataModel();

    // Values to be preserved in configuration changes:
    int selected_position = -1; // Selected position in the list of cameras
    Bitmap loaded_cam_image = null; // Bitmap of the camera image

    // Shared preferences key Strings related to the above variables:
    private final String POSITION_KEY = "sel_pos";
    private final String BITMAP_FILENAME_KEY = "loaded_bmp";

    // File name (for saving and restoring the loaded image to/from internal storage):
    private final String CAMERA_IMAGE_FILENAME = "camImg.jpg";


    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_cameras_madrid);

        checkLocationPermission();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Get references to UI elements:
        imageView = findViewById(R.id.imageView);
        listView = findViewById(R.id.listView);

        // Recover shared preferences values if they exist:
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        selected_position = sharedPref.getInt(POSITION_KEY, -1);
        if (sharedPref.contains(BITMAP_FILENAME_KEY)) {
            loaded_cam_image = retrieveBitmapFromFile(
                    sharedPref.getString(BITMAP_FILENAME_KEY, CAMERA_IMAGE_FILENAME));
        }

        dataModel.addCameraData("Getting cameras list... Please wait!!!", "", "");

        listView.setOnItemClickListener(this);

        // Create and set the adapter for the list:
        adapter = new ArrayAdapter(this, R.layout.my_simple_list_item_checked, dataModel.getCameraListNames());
        listView.setAdapter(adapter);
        listView.setChoiceMode( ListView.CHOICE_MODE_SINGLE ); // This configuration can be done in XML

        listView.setEnabled(false); // Disable the list until the real camera list has been populated

        // // update cameraListNames and cameraListURLS:
        DownloadKMLTask task = new DownloadKMLTask();
        task.execute( URL_CAMERAS );
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save the values to be preserved in shared preferences:
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(POSITION_KEY, selected_position);
        if (loaded_cam_image != null) {
            editor.putString(BITMAP_FILENAME_KEY, CAMERA_IMAGE_FILENAME);
            saveBitmapToFile(loaded_cam_image, CAMERA_IMAGE_FILENAME);
        }
        editor.commit();
    }

    ///////////////////////////////////////////////////////////
    // Listeners //////////////////////////////////////////////
    ///////////////////////////////////////////////////////////

    // Listener related to the user clicking on a camera of the list (ListView.OnItemClickListener interface):
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selected_position = position;
        DownloadImageTask task = new DownloadImageTask();
        task.execute( dataModel.getCameraURLAtPosition(position) );
    }

    // Listener related to the user clicking on a camera image:
    public void imgOnClick( View v ) {
        //Intent For Navigating to Second Activity
        Intent i = new Intent(MainActivity.this,MapsActivity.class);
        // Put as extras the coordinates string and the camera name corresponding to the currently
        // selected camera:
        i.putExtra("coordinates", dataModel.getCameraCoordinatesAtPosition( selected_position ) );
        i.putExtra("name", dataModel.getCameraNameAtPosition(selected_position));
        startActivity(i);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Methods for saving and restoring the bitmap image to local storage ////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    void saveBitmapToFile (Bitmap bmpToSave, String fileName) {
        try {
            File f = new File(getFilesDir (), fileName);
            if (f.exists()) { // Delete the file if it exists
                f.delete();
            }
            FileOutputStream fos = openFileOutput (fileName, 0);
            bmpToSave.compress(Bitmap.CompressFormat.PNG, 85, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Bitmap retrieveBitmapFromFile (String fileName) {
        File f = new File(getFilesDir (), fileName);
        try {
            FileInputStream fis = openFileInput(fileName);
            Bitmap readBmp = BitmapFactory.decodeStream( fis );
            fis.close();
            return readBmp;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    ///////////////////////////////////////////////////////////
    // AsyncTasks /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////

    private class DownloadKMLTask extends AsyncTask<String, Void, Void> {
        private String contentType = "";
        private boolean error = false;
        private String errorMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL( urls[0] );
                urlConnection = (HttpURLConnection) url.openConnection();
                contentType = urlConnection.getContentType();
                InputStream is = urlConnection.getInputStream();

                // Content type should be "application/vnd.google-earth.kml+xml"
                if ( contentType.contentEquals("application/vnd.google-earth.kml+xml") )
                {
                    Document document = null;
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

                        factory.setExpandEntityReferences(false);
                        factory.setIgnoringComments(true);
                        factory.setIgnoringElementContentWhitespace(true);

                        DocumentBuilder builder = factory.newDocumentBuilder();
                        document = builder.parse( is );

                    } catch (ParserConfigurationException e) {
                        dataModel.addCameraData(e.toString(), "", "");
                    } catch (SAXException e) {
                        dataModel.addCameraData(e.toString(), "", "");
                    } catch (IOException e) {
                        dataModel.addCameraData(e.toString(), "", "");
                    }

                    // Process XML document and extract names and image urls
                    NodeList placemarkList = document.getElementsByTagName("Placemark");
                    for (int i = 0; i < placemarkList.getLength(); ++i)
                    {
                        Element placemark = (Element) placemarkList.item(i);

                        // A) Camera URL -> cameraURL

                        NodeList descriptionList = placemark.getElementsByTagName("description");
                        Element description = (Element) descriptionList.item(0); // There is just one description item
                        String cameraURL = description.getTextContent();

                        // Description is a HTML img TAG, it must be parsed to get url
                        // Example:
                        // <description><div align=center><img src=http://informo.munimadrid.es/informo/Camaras/Camara06303.jpg?v=67030
                        // width=300 height=220/><br>PLAZA DE CASTILLA (NORTE)</div></description>
                        cameraURL = cameraURL.substring(cameraURL.indexOf("http:"));
                        cameraURL = cameraURL.substring(0, cameraURL.indexOf(".jpg") + 4); // 4 = length of ".jpg"

                        // B) Camera Coordinates -> coordString

                        NodeList pointList = placemark.getElementsByTagName("Point");
                        Element point = (Element) pointList.item(0); // There is only one element
                        NodeList coordinates = point.getElementsByTagName("coordinates"); // The name is inside value tag
                        String coordString = coordinates.item(0).getTextContent();

                        // C) Camera Name -> cameraName

                        NodeList extendedDataList = placemark.getElementsByTagName("ExtendedData");
                        Element extendedData = (Element) extendedDataList.item(0); // There is just one extendedData item
                        NodeList dataList = extendedData.getElementsByTagName("Data");
                        Element data = (Element) dataList.item(1); // The second element contains name
                        NodeList values = data.getElementsByTagName("Value"); // The name is inside value tag
                        String cameraName = values.item(0).getTextContent();

                        // Add the camera to the data model with the three informations A, B and C:
                        dataModel.addCameraData(cameraName, coordString, cameraURL);
                    }
                }
                else {
                    error = true;
                    errorMessage = contentType + " not processed";
                }

                urlConnection.disconnect();

            } catch (Exception e) {
                error = true;
                errorMessage = e.toString();
            }

            return null;
        }

        @Override
        protected void onPostExecute( Void unused) {
            if ( error ) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
            else {
                // Update adapter data to refresh the list:
                adapter.clear();
                adapter.addAll(dataModel.getCameraListNames());
                adapter.notifyDataSetChanged();

                // Recover the information on the previously selected item and image.
                if (  selected_position != -1 ) { // There was an item selected
                    listView.setSelection(selected_position); // "check" the item
                    listView.setItemChecked(selected_position, true);
                    listView.smoothScrollToPosition(selected_position);
                    if (loaded_cam_image != null) {
                        imageView.setImageBitmap(loaded_cam_image);
                        // Note we do not download the image again.
                        // This is faster but if the image is outdated it will remain outdated
                        // unless the user clicks again on the same list item.
                    }
                }
                listView.setEnabled(true); // enable the list again since it has been already populated.
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private String contentType = "";
        private boolean error = false;
        private String errorMessage;

        @Override
        protected  void onPreExecute()
        {
            listView.setEnabled(false); // disable selection while the image is being downloaded.
            imageView.setImageResource(R.drawable.wait); // show the "wait" sign
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;

            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL( urls[0] );
                urlConnection = (HttpURLConnection) url.openConnection();
                contentType = urlConnection.getContentType();
                InputStream is = urlConnection.getInputStream();

                // Content type should be "image/*"
                if ( contentType.indexOf( "image" ) != -1 ) { // If it is an image
                    bitmap = BitmapFactory.decodeStream( is );
                } else {
                    error = true;
                    errorMessage = contentType + " not processed";
                }

                urlConnection.disconnect();

            } catch (Exception e) {
                error = true;
                errorMessage = e.toString();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if ( error ) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            } else {
                loaded_cam_image = bitmap;
                imageView.setImageBitmap(loaded_cam_image);
                listView.setEnabled(true); // Enable again the list.
            }
        }



    }

    private void checkLocationPermission() {
        //If Android version is M (6.0 API 23) or newer, check if it has Location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //If Location permissions are not granted for the app, ask user for it! Request response will be received in the onRequestPermissionsResult.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
            }
            else {
                //init Bluetooth adapter


            }
        }
    }


    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        //Check if permission request response is from Location
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //User granted permissions. Setup the scan settings
                    Log.i( "MainActivity", "/+++++++++++++++++++++");
                    Log.d("TAG", "fine location permission granted");
                    Log.i( "MainActivity", "/+++++++++++++++++++++");


                } else {
                    //User denied Location permissions. Here you could warn the user that without
                    //Location permissions the app is not able to scan for BLE devices and eventually
                    //In this case we just close the app
                    finish();
                }
                return;
            }
        }
    }

}
