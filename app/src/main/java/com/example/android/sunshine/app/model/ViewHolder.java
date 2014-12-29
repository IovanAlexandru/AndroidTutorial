package com.example.android.sunshine.app.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.R;

/**
 * View holder for adapters.
 */
public class ViewHolder {
    public final ImageView iconView;
    public final TextView dateView;
    public final TextView descriptionView;
    public final TextView highTempView;
    public final TextView lowTempView;

    public ViewHolder(View view) {
        iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        dateView = (TextView) view.findViewById(R.id.list_item_date_textView);
        descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textView);
        highTempView = (TextView) view.findViewById(R.id.list_item_high_textView);
        lowTempView = (TextView) view.findViewById(R.id.list_item_low_textView);
    }
}
