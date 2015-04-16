package com.example.yavor.bestbefore;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yavor.bestbefore.data.ProductContract.ProductEntry;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public final String LOG_TAG = DetailFragment.class.getSimpleName();

    static final String DETAIL_URI = "URI";
    private static final int PRODUCT_LOADER = 1;

    private static final String[] PRODUCT_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            ProductEntry.TABLE_NAME + "." + ProductEntry._ID,
            ProductEntry.COLUMN_BARCODE,
            ProductEntry.COLUMN_NAME,
            ProductEntry.COLUMN_TYPE,
            ProductEntry.COLUMN_BEST_BEFORE
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_PRODUCT_ID = 0;
    static final int COL_PRODUCT_BARCODE = 1;
    static final int COL_PRODUCT_NAME = 2;
    static final int COL_PRODUCT_TYPE = 3;
    static final int COL_PRODUCT_BEST_BEFORE = 4;

    private TextView barcodeView;
    private TextView nameView;
    private TextView typeView;
    private TextView bestbeforeView;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        barcodeView = (TextView) rootView.findViewById(R.id.detail_barcode_textview);
        nameView = (TextView) rootView.findViewById(R.id.detail_name_textview);
        typeView = (TextView) rootView.findViewById(R.id.detail_name_textview);
        bestbeforeView = (TextView) rootView.findViewById(R.id.detail_bestbefore_textview);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = ProductEntry.COLUMN_BEST_BEFORE + " ASC";
        Uri uri = null;
        Bundle args = getArguments();
        if (args != null) {
            uri = args.getParcelable(DETAIL_URI);
        }
        if (uri == null) {
            uri = ProductEntry.CONTENT_URI;
            sortOrder = sortOrder + " LIMIT 1";
        }
        return new CursorLoader(getActivity(),
                uri,
                PRODUCT_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int barcode = data.getInt(ProductFragment.COL_PRODUCT_BARCODE);
            barcodeView.setText(String.valueOf(barcode));

            String name = data.getString(ProductFragment.COL_PRODUCT_NAME);
            nameView.setText(name);

            String type = data.getString(ProductFragment.COL_PRODUCT_TYPE);
            typeView.setText(type);

            long bestBeforeDateInMillis = data.getLong(ProductFragment.COL_PRODUCT_BEST_BEFORE);
            bestbeforeView.setText(Utility.getFormattedMonthDay(getActivity(), bestBeforeDateInMillis));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
