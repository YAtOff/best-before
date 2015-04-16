package com.example.yavor.bestbefore;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.yavor.bestbefore.data.ProductContract.ProductEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddProductFragment extends Fragment {

    private EditText mBarcodeInput;
    private EditText mNameInput;
    private EditText mTypeInput;
    private EditText mBestBeforeInput;
    private DatePickerDialog mBestBeforeDatePickerDialog;
    private SimpleDateFormat mDateFormatter;

    public AddProductFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_product, container, false);

        assignInputs(rootView);
        setupBestBeforeField(rootView);

        Button addButton = (Button) rootView.findViewById(R.id.add_product_button);
        addButton.setOnClickListener(new AddProductHandler());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void assignInputs(View view) {
        mBarcodeInput = (EditText) view.findViewById(R.id.add_product_field_barcode);
        mNameInput = (EditText) view.findViewById(R.id.add_product_field_name);
        mTypeInput = (EditText) view.findViewById(R.id.add_product_field_type);
        mBestBeforeInput = (EditText) view.findViewById(R.id.add_product_field_best_before);
    }

    private void setupBestBeforeField(View view) {
        mDateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        Calendar newCalendar = Calendar.getInstance();
        mBestBeforeDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                mBestBeforeInput.setText(mDateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        mBestBeforeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBestBeforeDatePickerDialog.show();
            }
        });
    }

    private class AddProductHandler implements View.OnClickListener {
        public AddProductHandler() {}

        public void onClick(View v) {
            addProduct();
        }

        private void addProduct() {
            try {
                ContentValues product = new ContentValues();
                product.put(ProductEntry.COLUMN_PRODUCT_ID, 0);
                product.put(ProductEntry.COLUMN_BARCODE, Long.parseLong(mBarcodeInput.getText().toString()));
                product.put(ProductEntry.COLUMN_NAME, mNameInput.getText().toString());
                product.put(ProductEntry.COLUMN_TYPE, mTypeInput.getText().toString());
                product.put(ProductEntry.COLUMN_BEST_BEFORE, mDateFormatter.parse(mBestBeforeInput.getText().toString()).getTime());

                Uri uri = getActivity().getContentResolver().insert(ProductEntry.CONTENT_URI, product);
                Intent intent = new Intent(getActivity(), DetailActivity.class).setData(uri);
                startActivity(intent);
            } catch (ParseException e) {
            }
        }

    }
}
