package dte.masteriot.mdp.camerasmadrid;

import java.util.ArrayList;

class DataModel {
    ArrayList<CameraData> camDataArray;

    // Constructor:
    DataModel() {
        camDataArray = new ArrayList<>();
    }

    // Add the data for a new camera to the ArrayList:
    void addCameraData(String n, String c, String u) {
        CameraData camData = new CameraData(n, c, u);
        camDataArray.add(camData);
    }

    void clearCameraData() {
        camDataArray.clear();
    }

    // Get only the camera names as an ArrayList:
    ArrayList<String> getCameraListNames() {
        ArrayList<String> cameraListNames = new ArrayList<>();
        for (int i = 0; i < camDataArray.size(); i++) {
            cameraListNames.add(camDataArray.get(i).name);
        }
        return cameraListNames;
    }

    // Getters for a camera's data at a specific position:

    String getCameraNameAtPosition(int item) {
        return camDataArray.get(item).name;
    }
    String getCameraCoordinatesAtPosition(int item) {
        return camDataArray.get(item).coordinates;
    }
    String getCameraURLAtPosition(int item) {
        return camDataArray.get(item).URL;
    }
}

class CameraData {
    String name;
    String coordinates;
    String URL;

    // Constructor:
    CameraData(String n, String c, String u) {
        name = n;
        coordinates = c;
        URL = u;
    }
}