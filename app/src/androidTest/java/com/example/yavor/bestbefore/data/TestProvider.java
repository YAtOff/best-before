package com.example.yavor.bestbefore.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.yavor.bestbefore.data.ProductContract.ProductEntry;

public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                ProductEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                ProductEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                ProductProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: ProductProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + ProductContract.CONTENT_AUTHORITY,
                    providerInfo.authority, ProductContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
         */
    public void testGetType() {
        // content://com.example.yavor.bestbefore/products/1
        String type = mContext.getContentResolver().getType(ProductEntry.CONTENT_URI);
        assertEquals("Error: the ProductEntry CONTENT_URI should return ProductEntry.CONTENT_TYPE",
                ProductEntry.CONTENT_TYPE, type);

        // content://com.example.yavor.bestbefore/products/1
        type = mContext.getContentResolver().getType(ProductEntry.buildProductUri(1));
        assertEquals("Error: the ProductEntry CONTENT_URI with id should return ProductEntry.CONTENT_ITEM_TYPE",
                ProductEntry.CONTENT_ITEM_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicProductQuery() {
        // insert our test records into the database
        ProductDbHelper dbHelper = new ProductDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues productValues = TestUtilities.createProductValues();

        long productRowId = db.insert(ProductEntry.TABLE_NAME, null, productValues);
        assertTrue("Unable to Insert ProductEntry into the Database", productRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor productCursor = mContext.getContentResolver().query(
                ProductEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicWeatherQuery", productCursor, productValues);
    }

    public void testQueryById() {
        // insert our test records into the database
        ProductDbHelper dbHelper = new ProductDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues productValues = TestUtilities.createProductValues();

        long productRowId = db.insert(ProductEntry.TABLE_NAME, null, productValues);
        assertTrue("Unable to Insert ProductEntry into the Database", productRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor productCursor = mContext.getContentResolver().query(
                ProductEntry.buildProductUri(productRowId),
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ProductEntry insert.",
                productCursor, productValues);
    }

    public void testQueryByBarcode() {
        // insert our test records into the database
        ProductDbHelper dbHelper = new ProductDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues productValues = TestUtilities.createProductValues();

        long productRowId = db.insert(ProductEntry.TABLE_NAME, null, productValues);
        assertTrue("Unable to Insert ProductEntry into the Database", productRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor productCursor = mContext.getContentResolver().query(
                ProductEntry.buildProductUriWithBarcode(11111111),
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ProductEntry insert.",
                productCursor, productValues);
    }

    public void testQueryByBestBefore() {
        // insert our test records into the database
        ProductDbHelper dbHelper = new ProductDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues productValues = TestUtilities.createProductValues();

        long productRowId = db.insert(ProductEntry.TABLE_NAME, null, productValues);
        assertTrue("Unable to Insert ProductEntry into the Database", productRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor productCursor = mContext.getContentResolver().query(
                ProductEntry.buildProductUriWithBestBefore(TestUtilities.TEST_DATE + 60 * 60 * 24),
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ProductEntry insert.",
                productCursor, productValues);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testInsertReadProvider() {
        ContentValues productValues = TestUtilities.createProductValues();
        // The TestContentObserver is a one-shot class
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(ProductEntry.CONTENT_URI, true, tco);

        Uri productInsertUri = mContext.getContentResolver()
                .insert(ProductEntry.CONTENT_URI, productValues);
        assertTrue(productInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor productCursor = mContext.getContentResolver().query(
                ProductEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ProductEntry insert.",
                productCursor, productValues);

    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver productObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ProductEntry.CONTENT_URI, true, productObserver);

        deleteAllRecordsFromProvider();

        productObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(productObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertProductValues() {
        long currentTestDate = TestUtilities.TEST_DATE;
        long millisecondsInADay = 1000*60*60*24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate+= millisecondsInADay ) {
            ContentValues weatherValues = new ContentValues();
            weatherValues.put(ProductEntry.COLUMN_PRODUCT_ID, i);
            weatherValues.put(ProductEntry.COLUMN_BARCODE, i);
            weatherValues.put(ProductEntry.COLUMN_NAME, String.format("product-%d", i));
            weatherValues.put(ProductEntry.COLUMN_TYPE, "sometype");
            weatherValues.put(ProductEntry.COLUMN_BEST_BEFORE, currentTestDate);
            returnContentValues[i] = weatherValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertProductValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver productObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ProductEntry.CONTENT_URI, true, productObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(ProductEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        productObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(productObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ProductEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                ProductEntry.COLUMN_BEST_BEFORE + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating ProductEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}

