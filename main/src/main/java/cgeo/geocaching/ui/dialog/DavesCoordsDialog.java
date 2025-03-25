package cgeo.geocaching.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

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
    }
}
