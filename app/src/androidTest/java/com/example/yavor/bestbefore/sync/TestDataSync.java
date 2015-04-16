package com.example.yavor.bestbefore.sync;

import android.content.ContentValues;
import android.test.AndroidTestCase;

import org.json.JSONException;

import java.text.ParseException;
import java.util.Vector;

import com.example.yavor.bestbefore.data.ProductContract.ProductEntry;

public class TestDataSync extends AndroidTestCase {

    public void testJsonParsing() throws JSONException, ParseException {
        String json = "[\n" +
                "    {\n" +
                "        \"id\": 1,\n" +
                "        \"barcode\": 11111111,\n" +
                "        \"name\": \"Salmon\",\n" +
                "        \"type\": \"fish\",\n" +
                "        \"bestBefore\": \"2015-04-17\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 2,\n" +
                "        \"barcode\": 22222222,\n" +
                "        \"name\": \"Emental\",\n" +
                "        \"type\": \"diary\",\n" +
                "        \"bestBefore\": \"2015-04-19\"\n" +
                "    }\n" +
                "]\n";
        BestbeforeSyncAdapter.DataSyncer syncer = new BestbeforeSyncAdapter.DataSyncer();
        Vector<ContentValues> cVVector = syncer.getProductDataFromJson(json);
        ContentValues cv = cVVector.elementAt(0);
        assertEquals("id not matches", cv.getAsInteger(ProductEntry.COLUMN_PRODUCT_ID), new Integer(1));
        assertEquals("barcode not matches", cv.getAsInteger(ProductEntry.COLUMN_BARCODE), new Integer(11111111));
        assertEquals("name not matches", cv.getAsString(ProductEntry.COLUMN_NAME), "Salmon");
        assertEquals("type not matches", cv.getAsString(ProductEntry.COLUMN_TYPE), "fish");
        cv = cVVector.elementAt(1);
        assertEquals("id not matches", cv.getAsInteger(ProductEntry.COLUMN_PRODUCT_ID), new Integer(2));
        assertEquals("barcode not matches", cv.getAsInteger(ProductEntry.COLUMN_BARCODE), new Integer(22222222));
        assertEquals("name not matches", cv.getAsString(ProductEntry.COLUMN_NAME), "Emental");
        assertEquals("type not matches", cv.getAsString(ProductEntry.COLUMN_TYPE), "diary");
    }
}
