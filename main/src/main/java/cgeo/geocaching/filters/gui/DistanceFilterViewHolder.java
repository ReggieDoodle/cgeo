package cgeo.geocaching.filters.gui;

import cgeo.geocaching.R;
import cgeo.geocaching.databinding.EditwaypointActivityBinding;
import cgeo.geocaching.filters.core.DistanceGeocacheFilter;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.location.GeopointFormatter;
import cgeo.geocaching.location.GeopointParser;
import cgeo.geocaching.location.IConversion;
import cgeo.geocaching.models.CalculatedCoordinate;
import cgeo.geocaching.models.CoordinateInputData;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.ui.ContinuousRangeSlider;
import cgeo.geocaching.ui.TextParam;
import cgeo.geocaching.ui.ViewUtils;
import cgeo.geocaching.ui.dialog.CoordinatesInputDialog;
import static cgeo.geocaching.ui.ViewUtils.dpToPixel;

import android.app.Activity;
import android.app.FragmentManager;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Objects;

public class DistanceFilterViewHolder extends BaseFilterViewHolder<DistanceGeocacheFilter> implements CoordinatesInputDialog.CoordinateUpdate {


    private final int maxDistance = Settings.useImperialUnits() ? Math.round(500f / IConversion.MILES_TO_KILOMETER) : 500;
    private final float conversion = Settings.useImperialUnits() ? IConversion.MILES_TO_KILOMETER : 1f;

    private ContinuousRangeSlider slider;
    private CheckBox useCurrentPosition;
    private EditText coordinate;

    private Geopoint coords;

    @Override
    public View createView() {



        final LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);

        useCurrentPosition = ViewUtils.addCheckboxItem(getActivity(), ll, TextParam.id(R.string.cache_filter_distance_use_current_position), R.drawable.ic_menu_mylocation, null);
        useCurrentPosition.setChecked(true);

        final Pair<View, EditText> coordField = ViewUtils.createTextField(getActivity(), null, TextParam.id(R.string.cache_filter_distance_coordinates), null, -1, 1, 1);
        coordinate = coordField.second;
        ll.addView(coordField.first);

        slider = new ContinuousRangeSlider(getActivity());
        slider.setScale(-0.2f, maxDistance + 0.2f, f -> {
            if (f <= 0) {
                return "0";
            }
            if (f > maxDistance) {
                return ">" + maxDistance;
            }
            return "" + Math.round(f);
        }, 6, 1);
        slider.setRange(-0.2f, maxDistance + 0.2f);

        final LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, dpToPixel(5), 0, dpToPixel(20));
        ll.addView(slider, llp);

        return ll;
    }

    @Override
    public void setViewFromFilter(final DistanceGeocacheFilter filter) {
        useCurrentPosition.setChecked(filter.isUseCurrentPosition());
        coordinate.setText(filter.getCoordinate() == null ? "" : GeopointFormatter.format(GeopointFormatter.Format.LAT_LON_DECMINUTE, filter.getCoordinate()));
        slider.setRange(
                filter.getMinRangeValue() == null ? -10f : filter.getMinRangeValue() / conversion,
                filter.getMaxRangeValue() == null ? maxDistance + 500f : filter.getMaxRangeValue() / conversion);
    }

    @Override
    public DistanceGeocacheFilter createFilterFromView() {
        final DistanceGeocacheFilter filter = createFilter();
        filter.setUseCurrentPosition(useCurrentPosition.isChecked());
        filter.setCoordinate(GeopointParser.parse(coordinate.getText().toString(), null));
        final ImmutablePair<Float, Float> range = slider.getRange();
        filter.setMinMaxRange(range.left, range.right , 0f, (float) maxDistance, value -> (float) Math.round(value * conversion));
        return filter;
    }

    private void setCoordinates() {

        final Activity activity = this.getActivity();
        final FragmentManager fm = activity.getFragmentManager();

        final CoordinateInputData cid = new CoordinateInputData();
        cid.setGeopoint(this.coords);

        CoordinatesInputDialog.show(fm, cid);

        /*CoordinatesInputDialog.show(fm, null, coords);*/
    }

    // Implementation of CoordinateUpdate

    @Override
    public void updateCoordinates(@Nullable final Geopoint gp) {
        this.coords = gp;
    }
    @Override
    public void updateCoordinates(CoordinateInputData coordinateInputData) {

    }
    @Override
    public boolean supportsCalculatedCoordinates() { return false; }
    @Override
    public boolean supportsNullCoordinates() { return true; }

}
