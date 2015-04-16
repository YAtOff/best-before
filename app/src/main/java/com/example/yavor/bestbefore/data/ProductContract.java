package com.example.yavor.bestbefore.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

import java.util.Locale;

public class ProductContract {

    public static final String CONTENT_AUTHORITY = "com.example.yavor.bestbefore.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCT = "products";

    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT + "/#";

        public static final String TABLE_NAME = "products";

        public static final String COLUMN_PRODUCT_ID = "product_id";
        public static final String COLUMN_BARCODE = "barcode";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_BEST_BEFORE = "best_before";


        public static Uri buildProductUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildProductUriWithBarcode(int barcode) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_BARCODE, String.valueOf(barcode)).build();
        }

        public static Uri buildProductUriWithBestBefore(long bestBefore) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_BEST_BEFORE, String.valueOf(bestBefore)).build();
        }

        public static long getProductIdFromUri(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

        public static int getProductBarcodeFromUri(Uri uri) {
            String barcodeString = uri.getQueryParameter(COLUMN_BARCODE);
            if (null != barcodeString && barcodeString.length() > 0) {
                return Integer.parseInt(barcodeString);
            } else {
                return 0;
            }
        }

        public static long getProductBestBeforeFromUri(Uri uri) {
            String bestBeforeString = uri.getQueryParameter(COLUMN_BEST_BEFORE);
            if (null != bestBeforeString && bestBeforeString.length() > 0) {
                return Long.parseLong(bestBeforeString);
            } else {
                return 0;
            }
        }

    }

}
