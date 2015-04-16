package com.example.yavor.bestbefore.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import com.example.yavor.bestbefore.R;
import com.example.yavor.bestbefore.data.ProductContract.ProductEntry;

public class BestbeforeSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = BestbeforeSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 1;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;


    private static final String[] NOTIFY_PRODUCT_EXPIRE_PROJECTION = new String[] {
            ProductEntry.COLUMN_PRODUCT_ID,
            ProductEntry.COLUMN_BARCODE,
            ProductEntry.COLUMN_NAME,
            ProductEntry.COLUMN_BEST_BEFORE
    };

    // these indices must match the projection
    private static final int INDEX_PRODUCT_ID = 0;
    private static final int INDEX_BARCODE = 1;
    private static final int INDEX_NAME = 2;
    private static final int INDEX_BEST_BEFORE = 3;

    public BestbeforeSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        DataSyncer syncer = new DataSyncer();
        syncer.sync(getContext().getContentResolver());
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        BestbeforeSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static class DataSyncer {

        public final String LOG_TAG = BestbeforeSyncAdapter.class.getSimpleName();

        public void sync(ContentResolver contentResolver) {
            Log.d(LOG_TAG, "Starting sync");

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String productsJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String PRODUCTS_BASE_URL = "http://192.168.181.156:3333/products.json";

                Uri builtUri = Uri.parse(PRODUCTS_BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                productsJsonStr = buffer.toString();
                updateContent(getProductDataFromJson(productsJsonStr), contentResolver);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } catch (ParseException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
         public Vector<ContentValues> getProductDataFromJson(String productsJsonStr)
                throws JSONException, ParseException {

            try {
                JSONArray productsArray = new JSONArray(productsJsonStr);
                Vector<ContentValues> cVVector = new Vector<ContentValues>(productsArray.length());

                for (int i = 0; i < productsArray.length(); i++) {
                    // These are the values that will be collected.
                    int productId;
                    int barcode;
                    String name;
                    String type;
                    long bestBefore;

                    JSONObject product = productsArray.getJSONObject(i);
                    productId = product.getInt("id");
                    barcode = product.getInt("barcode");
                    name = product.getString("name");
                    type = product.getString("type");
                    bestBefore = new SimpleDateFormat("yyyy-MM-dd").parse(product.getString("bestBefore")).getTime();

                    ContentValues productValues = new ContentValues();

                    productValues.put(ProductEntry.COLUMN_PRODUCT_ID, productId);
                    productValues.put(ProductEntry.COLUMN_BARCODE, barcode);
                    productValues.put(ProductEntry.COLUMN_NAME, name);
                    productValues.put(ProductEntry.COLUMN_TYPE, type);
                    productValues.put(ProductEntry.COLUMN_BEST_BEFORE, bestBefore);
                    cVVector.add(productValues);
                }
                return cVVector;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                throw e;
            }
        }

        private void updateContent(Vector<ContentValues> cVVector, ContentResolver contentResolver) {
            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                contentResolver.delete(
                        ProductEntry.CONTENT_URI,
                        null,
                        null
                );

                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                contentResolver.bulkInsert(ProductEntry.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
        }
    }
}