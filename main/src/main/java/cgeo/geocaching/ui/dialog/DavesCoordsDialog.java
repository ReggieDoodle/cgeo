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

import cgeo.geocaching.R;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.ui.ViewUtils;

// A recreation of the existing coordinate dialog
public class DavesCoordsDialog {
    private final Context context;
    private final DialogCallback callback;

    public DavesCoordsDialog(Context context, DialogCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void show(Geopoint location) {

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

        inputLatitude.setText(String.valueOf(location.getLatitude()));
        inputLongitude.setText(String.valueOf(location.getLongitude()));


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

