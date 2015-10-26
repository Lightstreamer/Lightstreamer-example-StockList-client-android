/*
 * Copyright (c) Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightstreamer.demo.android;

import java.util.HashMap;

import com.lightstreamer.demo.android.R;

import com.lightstreamer.ls_client.UpdateInfo;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.TableRow;

/**
 * This is where StockListDemo application interacts
 * with Android user interface.
 */
class AndroidUi implements LightstreamerListener {

    private Activity activity;
    private TextView statusTextView;
    private ImageView statusImageView;
    private ImageView logoImageView;
    private Handler handler;
    private HashMap<String, TableRow> itemMap;
    private HashMap<View, ColorRunnable> fadeMap;
    private TableLayout tableLayout;
    private String[] fields;
    private String[] fieldNames;
    // set this to true if you want a debugging item reiceved counter
    // in the user interface
    private boolean itemUpdatesCounter = true;
    private Integer itemUpdatesCount = new Integer(0);
    private TextView itemCountTextView;

    public AndroidUi(Activity activity) {
        this.activity = activity;
        this.handler = new Handler();
        this.itemCountTextView = (TextView)this.activity.findViewById(
                R.id.itemCountTextView);
        this.statusTextView = (TextView)this.activity.findViewById(
                R.id.statusTextView);
        this.tableLayout = (TableLayout)this.activity.findViewById(
                R.id.tableLayout);
        this.statusImageView = (ImageView)this.activity.findViewById(
                R.id.statusImageView);
        this.logoImageView = (ImageView)this.activity.findViewById(
                R.id.logoImageView);
        this.logoImageView.setBackgroundResource(R.drawable.lslogo);
        this.itemMap = new HashMap<String, TableRow>();
        this.fadeMap = new HashMap<View, ColorRunnable>();

        /* tweak TableLayout borders in order to avoid glitches with
         * scrollbars */
        this.tableLayout.setPadding(5, 5, 5, 5);
    }

    /**
     * Change the Start/Stop menu item data. This must be called from inside
     * the Activity method onPrepareOptionsMenu(). Don't call it elsewhere.
     * 
     * @param item MenuItem object to work on
     * @param start if true, changes the "Start/Stop" menu item title (and icon
     * accordingly) to "Re(start) connection", otherwise to "Pause connection".
     */
    public void changeStartStopMenuItemState(MenuItem item, boolean start) {
        if (start) {
            item.setTitle("(Re)start connection");
            item.setIcon(android.R.drawable.ic_media_play);
        } else {
            item.setTitle("Pause connection");
            item.setIcon(android.R.drawable.ic_media_pause);
        }
    }

    /**
     * This method creates a table inside a TableLayout object containing
     * Lightstreamer item identifiers and their fields.
     * @param items
     * @param fields
     */
    public void createTable(String[] items, String[] fields, String[] fieldNames) {
        /* clear previous views */
        this.tableLayout.removeAllViews();

        /* store fields locally, it will be reused afterwards */
        if (fields.length != fieldNames.length) {
            throw new RuntimeException(
            "fields and fieldNames must have the same length");
        }
        this.fields = fields;
        this.fieldNames = fieldNames;

        /* add table headers */
        TableRow mainRow = new TableRow(this.activity);
        for (int i = 0; i < this.fieldNames.length; i++) {
            TextView view = new TextView(this.activity);
            Typeface tf = Typeface.create(new String(), Typeface.BOLD);
            view.setText(this.fieldNames[i]);
            view.setTypeface(tf);
            if (i == 0) {
                this.tableLayout.setColumnStretchable(i, true);
            } else {
                this.tableLayout.setColumnShrinkable(i, true);
            }
            mainRow.addView(view);
        }
        this.tableLayout.addView(mainRow);

        /* add table fields */
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            TableRow row = new TableRow(this.activity);
            for (int n = 0; n < this.fields.length; n++) {
                String field = this.fields[n];
                TextView view = new TextView(this.activity);
                view.setHint(field);
                view.setText("N/A");
                row.addView(view);
            }
            this.tableLayout.addView(row);
            this.itemMap.put(item, row);
        }
    }

    /**
     * Throws error message at user.
     * @param title error title
     * @param message error message
     */
    public void alert(String title, String message, boolean fatal) {
        Builder dialog = new AlertDialog.Builder(this.activity);
        if (message != null) {
            dialog.setMessage(message);
        }
        if (title != null) {
            dialog.setTitle(title);
        }
        dialog.setCancelable(false);
        if (fatal) {
            dialog.setNeutralButton("OK", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AndroidUi.this.activity.finish();
                }
            });
        } else {
            dialog.setNeutralButton("OK", null);
        }
        dialog.show();
    }

    private class MessageRunnable implements Runnable {
        private String message;
        private TextView view;
        MessageRunnable(String message, TextView view) {
            super();
            this.message = message;
            this.view = view;
        }

        public void run() {
            view.setText(this.message);
        }
    }

    private class BackgroundRunnable implements Runnable {
        private int resId;
        private ImageView view;
        BackgroundRunnable(int resId, ImageView view) {
            super();
            this.resId = resId;
            this.view = view;
        }

        public void run() {
            view.setBackgroundResource(this.resId);
        }
    }

    /**
     * Update application status message.
     * @param message message text
     * @param resId Android Application resource Id
     */
    private void updateStatus(String message, int resId) {
        this.handler.postDelayed(
                new MessageRunnable(message, this.statusTextView), 0);
        if (resId != -1) {
            this.handler.postDelayed(
                    new BackgroundRunnable(resId, this.statusImageView), 0);
        }
    }

    private class ColorRunnable implements Runnable {
        private int color;
        private View view;
        private boolean valid = true;

        ColorRunnable(int color, View view) {
            super();
            this.color = color;
            this.view = view;
        }

        public void run() {
            if (this.valid) {
                view.setBackgroundColor(this.color);	
            }
        }

        public void invalidate() {
            this.valid = false;
        }
    }

    private void updateCellColor(View view, double upDown) {
        int color = Color.parseColor("#FF7400"); /* RSS orange */
        if (upDown > 0) {
            color = Color.parseColor("#008C00"); /* techcrunch green */
        } else if (upDown < 0) {
            color = Color.parseColor("#CC0000"); /* rollyo red */
        }
        this.handler.postDelayed(
                new ColorRunnable(color, view), 0);

        /* stop old fade action */
        if (this.fadeMap.containsKey(view)) {
            ColorRunnable oldRunnable = this.fadeMap.get(view);
            oldRunnable.invalidate();
        }

        /* reset color after 2 seconds */
        ColorRunnable runnable = new ColorRunnable(Color.TRANSPARENT, view);
        this.fadeMap.put(view, runnable);
        this.handler.postDelayed(runnable, 600);
    }

    private void updateItemCounter() {
        this.itemUpdatesCount += 1;
        this.handler.postDelayed(
                new MessageRunnable(this.itemUpdatesCount.toString(),
                        this.itemCountTextView), 0);
    }

    @Override
    public void onItemUpdate(int phase, int itemPos, String itemName, UpdateInfo update) {
        if (itemUpdatesCounter) {
            updateItemCounter();
        }
        if (fields != null) {
            TableRow row = itemMap.get(itemName);
            boolean snapshot = update.isSnapshot();

            for (int i = 0; i < fields.length; i++) {

                double upDown = 0.0;
                String field = fields[i];
                String value = update.getNewValue(field);

                if (update.isValueChanged(field)) {
                    TextView view = (TextView)row.getChildAt(i);
                    handler.postDelayed(
                            new MessageRunnable(value, view), 0);

                    if (!snapshot) {
                        /* update cell color */
                        String oldValue = update.getOldValue(field);
                        try {
                            double valueInt = Double.parseDouble(value);
                            double oldValueInt = Double.parseDouble(oldValue);
                            upDown = valueInt - oldValueInt;
                        } catch (NumberFormatException nfe) { /* ignore */ }
                        updateCellColor(view, upDown);
                    } else {
                        /* mark entire row as updated */
                        updateCellColor(row, 1.0);
                    }
                }
            }
        }
    }

    @Override
    public void onLostUpdate(int phase, int itemPos, String itemName, int lostUpdates) { }

    @Override
    public void onReconnectRequest(int phase) { }

    @Override
    public void onStatusChange(int phase, int status) {

        if (!StockListDemo.checkPhase(phase)) {
            return;
        }

        String statusTxt = null;
        int icon;
        switch(status) {
            case LightstreamerConnectionStatus.DISCONNECTED:
                statusTxt = "Disconnected";
                icon = R.drawable.status_disconnected;
                StockListDemo.setConnected(false);
                break;
            case LightstreamerConnectionStatus.CONNECTING:
                statusTxt = "Connecting to server " + activity.getResources().getString(R.string.host);
                icon = R.drawable.status_disconnected;
                StockListDemo.setConnected(false);
                break;
            case LightstreamerConnectionStatus.CONNECTED:
                statusTxt = "Connected to server " + activity.getResources().getString(R.string.host);
                icon = R.drawable.status_disconnected;
                StockListDemo.setConnected(true);
                break;
            case LightstreamerConnectionStatus.STREAMING:
                statusTxt = "Session started in streaming";
                icon = R.drawable.status_connected_streaming;
                StockListDemo.setConnected(true);
                break;
            case LightstreamerConnectionStatus.POLLING:
                statusTxt = "Session started in smart polling";
                icon = R.drawable.status_connected_polling;
                StockListDemo.setConnected(true);
                break;
            case LightstreamerConnectionStatus.STALLED:
                statusTxt = "Connection stalled";
                icon = R.drawable.status_stalled;
                StockListDemo.setConnected(false);
                break;
            case LightstreamerConnectionStatus.ERROR:
                statusTxt = "Data error";
                icon = R.drawable.status_disconnected;
                StockListDemo.setConnected(false);
                break;
            case LightstreamerConnectionStatus.CONNECTION_ERROR:
                statusTxt = "Connection error";
                icon = R.drawable.status_disconnected;
                StockListDemo.setConnected(false);
                break;
            case LightstreamerConnectionStatus.SERVER_ERROR:
                statusTxt = "Server error";
                icon = R.drawable.status_disconnected;
                StockListDemo.setConnected(false);
                break;
            default:
                statusTxt = "Disconnected";
                icon = R.drawable.status_disconnected;
                StockListDemo.setConnected(false);
        }
        updateStatus(statusTxt, icon);
    }

}
