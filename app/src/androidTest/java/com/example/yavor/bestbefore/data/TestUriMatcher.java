package com.example.yavor.bestbefore.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {

    private static final Uri TEST_PRODUCT_DIR = ProductContract.ProductEntry.CONTENT_URI;

    public void testUriMatcher() {
        UriMatcher testMatcher = ProductProvider.buildUriMatcher();

        assertEquals("Error: The PRODUCTS URI was matched incorrectly.",
                testMatcher.match(TEST_PRODUCT_DIR), ProductProvider.PRODUCTS);
    }
}
