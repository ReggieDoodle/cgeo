package cgeo.geocaching.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import cgeo.geocaching.R;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.ui.ViewUtils;

public class DavesCoordsDialog {
    private final Context context;
    private final DialogCallback callback;

    public DavesCoordsDialog(Context context, DialogCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void show(Geopoint location) {

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.daves_coords_dialog, null);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);



        AlertDialog dialog = builder.create();
        dialog.show();

        // Populate the spinner with options
        Spinner spinner = dialogView.findViewById(R.id.dialogSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new String[]{"DD MM.MMM", "DD MM SS.SSS", "DD.DDDDD"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Populate the text fields
        EditText inputLatitude = dialogView.findViewById(R.id.latitude);
        EditText inputLongitude = dialogView.findViewById(R.id.longitude);


        inputLatitude.setText(String.valueOf(location.getLatitude()));
        inputLongitude.setText(String.valueOf(location.getLongitude()));


        // Add logic to the button to read the coordinates
        Button button = dialogView.findViewById(R.id.commitButton);

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
}

