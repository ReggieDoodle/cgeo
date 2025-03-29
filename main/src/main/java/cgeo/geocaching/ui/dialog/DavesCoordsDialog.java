package cgeo.geocaching.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

import cgeo.geocaching.R;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.sensors.LocationDataProvider;


// A recreation of the existing coordinate dialog
public class DavesCoordsDialog {
    private final Context context;
    private final DialogCallback callback;

    Spinner spinner;
    private Button bLatitude, bLongitude;
    private EditText longitudeDegree, longitudeMinutes, longitudeSeconds, longitudeFraction;
    private EditText latitudeDegree, latitudeMinutes, latitudeSeconds, latitudeFraction;
    private Geopoint gp;

    public DavesCoordsDialog(Context context, DialogCallback callback) {
        this.context = context;
        this.callback = callback;

    }

    @NonNull
    private static Geopoint currentCoords() {
        return LocationDataProvider.getInstance().currentGeo().getCoords();
    }

    public void show(Geopoint location) {

        gp = new Geopoint(location.getLatitude(), location.getLongitude());

        LayoutInflater inflater = LayoutInflater.from(context);
        final View theView = inflater.inflate(R.layout.daves_coords_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(theView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Populate the spinner with options
        spinner = theView.findViewById(R.id.dialogSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new String[]{"DD MM.MMM", "DD MM SS.SSS", "DD.DDDDD"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                UpdateGui(theView, selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where no item is selected
                Toast.makeText(context, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Populate the text fields
        EditText inputLatitude = theView.findViewById(R.id.latitude);
        EditText inputLongitude = theView.findViewById(R.id.longitude);

        inputLatitude.setText(String.valueOf(gp.getLatitude()));
        inputLongitude.setText(String.valueOf(gp.getLongitude()));

        bLatitude = theView.findViewById(R.id.hemisphereLatitude);
        bLongitude = theView.findViewById(R.id.hemisphereLongitude);

        latitudeDegree = theView.findViewById(R.id.editTextLatDegrees);
        latitudeMinutes = theView.findViewById(R.id.editTextLatMinutes);
        latitudeSeconds = theView.findViewById(R.id.editTextLatSeconds);
        latitudeFraction = theView.findViewById(R.id.editTextLatFraction);

        longitudeDegree = theView.findViewById(R.id.editTextLonDegrees);
        longitudeMinutes = theView.findViewById(R.id.editTextLonMinutes);
        longitudeSeconds = theView.findViewById(R.id.editTextLonSeconds);
        longitudeFraction = theView.findViewById(R.id.editTextLonFraction);

        bLatitude.setOnClickListener(v -> {
            final CharSequence text = bLatitude.getText();
            if (text.equals("N"))
                bLatitude.setText("S");
            else
                bLatitude.setText("N");
        });

        bLongitude.setOnClickListener(v -> {
                    final CharSequence text = bLongitude.getText();
                    if (text.equals("E"))
                        bLongitude.setText("W");
                    else
                        bLongitude.setText("E");
        });

        // Add logic to the button to read the coordinates
        Button button = theView.findViewById(R.id.commitButton);

        button.setOnClickListener(v -> {

            String result = ReadGui();

            if (result.isEmpty()) {
                Toast.makeText(context, "Please enter something!", Toast.LENGTH_SHORT).show();
            } else {
                // Invoke the callback to notify that the dialog is closed
                callback.onDialogClosed(result);
                dialog.dismiss();
            }
        });
    }

    private String ReadGui(){

        String lat = bLatitude.getText().toString();
        String lon = bLongitude.getText().toString();

        lat += String.valueOf(latitudeDegree.getText());
        lon += String.valueOf(longitudeDegree.getText());

        String currentFormat = spinner.getSelectedItem().toString();

        switch (currentFormat) {
            case "DD MM.MMM":
                lat += " " + latitudeMinutes.getText() + "." + latitudeFraction.getText();
                lon += " " + longitudeMinutes.getText() + "." + longitudeFraction.getText();
                break;
            case "DD MM SS.SSS":
                lat += " " + latitudeMinutes.getText() + " " + latitudeSeconds.getText()+ "." + latitudeFraction.getText();
                lon += " " + longitudeMinutes.getText() + " " + longitudeSeconds.getText()+ "." + longitudeFraction.getText();
                break;
            case "DDD.DDDDD":
            default:
                lat += "." + latitudeFraction.getText();
                lon += "." + longitudeFraction.getText();
                break;

        }
        return lat + " " + lon;
    }

    private void UpdateGui(View v, String selectedOption) {

        if (gp != null) {
            bLatitude.setText(String.valueOf(gp.getLatDir()));
            bLongitude.setText(String.valueOf(gp.getLonDir()));
        } else {
            bLatitude.setText(String.valueOf(currentCoords().getLatDir()));
            bLongitude.setText(String.valueOf(currentCoords().getLonDir()));
        }

        switch (selectedOption) {
            case "DD MM.MMM":

                latitudeDegree.setVisibility(View.VISIBLE);
                latitudeDegree.setText(addZeros(gp.getDecMinuteLatDeg(),2));

                latitudeMinutes.setVisibility(View.VISIBLE);
                latitudeMinutes.setText(addZeros(gp.getDecMinuteLatMin(),2));

                latitudeSeconds.setVisibility(View.GONE);

                latitudeFraction.setVisibility(View.VISIBLE);
                latitudeFraction.setText(addZeros(gp.getDecMinuteLatMinFrac(),3));

                longitudeDegree.setVisibility(View.VISIBLE);
                longitudeDegree.setText(addZeros(gp.getDecMinuteLonDeg(),3));

                longitudeMinutes.setVisibility(View.VISIBLE);
                longitudeMinutes.setText(addZeros(gp.getDecMinuteLonMin(),2));

                longitudeSeconds.setVisibility(View.GONE);

                longitudeFraction.setVisibility(View.VISIBLE);
                longitudeFraction.setText(addZeros(gp.getDecMinuteLonMinFrac(),3));
                break;

            case "DD MM SS.SSS":
                latitudeDegree.setVisibility(View.VISIBLE);
                latitudeDegree.setText(addZeros(gp.getDecDegreeLatDeg(), 2));

                latitudeMinutes.setVisibility(View.VISIBLE);
                latitudeMinutes.setText(addZeros(gp.getDMSLatMin(),2));

                latitudeSeconds.setVisibility(View.VISIBLE);
                latitudeSeconds.setText(addZeros(gp.getDMSLatSec(), 2));

                latitudeFraction.setVisibility(View.VISIBLE);
                latitudeFraction.setText(addZeros(gp.getDMSLatSecFrac(),3));

                longitudeDegree.setVisibility(View.VISIBLE);
                longitudeDegree.setText(addZeros(gp.getDMSLonDeg(),3));

                longitudeMinutes.setVisibility(View.VISIBLE);
                longitudeMinutes.setText(addZeros(gp.getDMSLonMin(),2));

                longitudeSeconds.setVisibility(View.VISIBLE);
                longitudeSeconds.setText(addZeros(gp.getDMSLonSec(),2));

                longitudeFraction.setVisibility(View.VISIBLE);
                longitudeFraction.setText(addZeros(gp.getDMSLonSecFrac(),3));
                break;

            case "DD.DDDDD":
            default:
                latitudeDegree.setVisibility(View.VISIBLE);
                latitudeDegree.setText(addZeros(gp.getDecDegreeLatDeg(),2));

                latitudeMinutes.setVisibility(View.GONE);
                latitudeSeconds.setVisibility(View.GONE);

                latitudeFraction.setVisibility(View.VISIBLE);
                latitudeFraction.setText(addZeros(gp.getDecDegreeLatDegFrac(),5));

                longitudeDegree.setVisibility(View.VISIBLE);
                longitudeDegree.setText(addZeros(gp.getDecDegreeLonDeg(),3));

                longitudeMinutes.setVisibility(View.GONE);
                longitudeSeconds.setVisibility(View.GONE);

                longitudeFraction.setVisibility(View.VISIBLE);
                longitudeFraction.setText(addZeros(gp.getDecDegreeLonDegFrac(),5));
                break;
        }

    }

    private static String addZeros(final int value, final int len) {
        return StringUtils.leftPad(Integer.toString(value), len, '0');
    }
}

