package cgeo.geocaching.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import cgeo.geocaching.R;

public class DavesCoordsDialog {
    private final Context context;

    public DavesCoordsDialog(Context context) {
        this.context = context;
    }

    public void show() {
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

    }
}
