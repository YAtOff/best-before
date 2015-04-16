package com.example.yavor.bestbefore;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ProductAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_PRODUCT = 0;
    private static final int VIEW_TYPE_COUNT = 1;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final TextView barcodeView;
        public final TextView nameView;
        public final TextView typeView;
        public final TextView bestbeforeView;

        public ViewHolder(View view) {
            barcodeView = (TextView) view.findViewById(R.id.list_item_barcode_textview);
            nameView = (TextView) view.findViewById(R.id.list_item_name_textview);
            typeView = (TextView) view.findViewById(R.id.list_item_type_textview);
            bestbeforeView = (TextView) view.findViewById(R.id.list_item_bestbefore_textview);
        }
    }

    public ProductAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_PRODUCT: {
                layoutId = R.layout.list_item_product;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int barcode = cursor.getInt(ProductFragment.COL_PRODUCT_BARCODE);
        viewHolder.barcodeView.setText(String.valueOf(barcode));

        String name = cursor.getString(ProductFragment.COL_PRODUCT_NAME);
        viewHolder.nameView.setText(name);

        String type = cursor.getString(ProductFragment.COL_PRODUCT_TYPE);
        viewHolder.typeView.setText(type);

        long bestBeforeDateInMillis = cursor.getLong(ProductFragment.COL_PRODUCT_BEST_BEFORE);
        viewHolder.bestbeforeView.setText(Utility.getFormattedMonthDay(context, bestBeforeDateInMillis));
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_PRODUCT;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
