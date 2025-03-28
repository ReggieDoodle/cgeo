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

import cgeo.geocaching.R;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.sensors.LocationDataProvider;


// A recreation of the existing coordinate dialog
public class DavesCoordsDialog {
    private final Context context;
    private final DialogCallback callback;

    private Button bLatitude, bLongitude;
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

        gp = location;

        LayoutInflater inflater = LayoutInflater.from(context);
        final View theView = inflater.inflate(R.layout.daves_coords_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(theView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Populate the spinner with options
        Spinner spinner = theView.findViewById(R.id.dialogSpinner);
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
            String userInputLatitude = inputLatitude.getText().toString();
            String userInputLongitude = inputLongitude.getText().toString();

            if (userInputLatitude.isEmpty() || userInputLongitude.isEmpty()) {
                Toast.makeText(context, "Please enter something!", Toast.LENGTH_SHORT).show();
            } else {
                // Invoke the callback to notify that the dialog is closed
                callback.onDialogClosed(userInputLatitude + " " + userInputLongitude);
                dialog.dismiss();
            }
        });
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
                v.findViewById(R.id.EditTextLatMinutes).setVisibility(View.VISIBLE);
                v.findViewById(R.id.EditTextLatSeconds).setVisibility(View.GONE);
                v.findViewById(R.id.EditTextLatFraction).setVisibility(View.VISIBLE);

                v.findViewById(R.id.EditTextLonMinutes).setVisibility(View.VISIBLE);
                v.findViewById(R.id.EditTextLonSeconds).setVisibility(View.GONE);
                v.findViewById(R.id.EditTextLonFraction).setVisibility(View.VISIBLE);
                break;

            case "DD MM SS.SSS":
                v.findViewById(R.id.EditTextLatMinutes).setVisibility(View.VISIBLE);
                v.findViewById(R.id.EditTextLatSeconds).setVisibility(View.VISIBLE);
                v.findViewById(R.id.EditTextLatFraction).setVisibility(View.VISIBLE);

                v.findViewById(R.id.EditTextLonMinutes).setVisibility(View.VISIBLE);
                v.findViewById(R.id.EditTextLonSeconds).setVisibility(View.VISIBLE);
                v.findViewById(R.id.EditTextLonFraction).setVisibility(View.VISIBLE);
                break;

            case "DD.DDDDD":
            default:
                v.findViewById(R.id.EditTextLatMinutes).setVisibility(View.GONE);
                v.findViewById(R.id.EditTextLatSeconds).setVisibility(View.GONE);
                v.findViewById(R.id.EditTextLatFraction).setVisibility(View.VISIBLE);

                v.findViewById(R.id.EditTextLonMinutes).setVisibility(View.GONE);
                v.findViewById(R.id.EditTextLonSeconds).setVisibility(View.GONE);
                v.findViewById(R.id.EditTextLonFraction).setVisibility(View.VISIBLE);
                break;
        }
    }
}

