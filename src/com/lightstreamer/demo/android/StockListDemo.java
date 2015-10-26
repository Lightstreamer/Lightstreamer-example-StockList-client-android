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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.lightstreamer.demo.android.R;
import com.lightstreamer.ls_client.PushConnException;
import com.lightstreamer.ls_client.PushServerException;
import com.lightstreamer.ls_client.PushUserException;
import com.lightstreamer.ls_client.SubscrException;

import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class StockListDemo extends Activity {

    /*
     * List of items available on the Lightsteamer server.
     */
    private final static String[] items = {"item1", "item2", "item3",
        "item4", "item5", "item6", "item7", "item8", "item9", "item10",
        "item11", "item12", "item13", "item14", "item15"};

    /*
     * Lightstreamer field names available on the Lightstreamer server for given
     * items.
     */
    private final static String[] fields = {"stock_name", "last_price", "time"};

    /*
     * UI names associated with field names.
     */
    private final static String[] fieldNames = {"Name", "Last", "Time"};
    
    private AndroidUi ui;
    private LightstreamerClient ls;
    private boolean userDisconnect = false;

    private static AtomicBoolean connected = new AtomicBoolean(false);
    
    private static AtomicInteger phase = new AtomicInteger(0);

    public static boolean checkPhase(int ph) {
        return ph == phase.get();
    }

    public static boolean isConnected() {
        return connected.get();
    }

    public static void setConnected(boolean status) {
        connected.set(status);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // setup logger
        /*
        InputStream logfile = this.getResources().openRawResource(R.raw.logcfg);
        try {
            LogManager.getLogManager().readConfiguration(logfile);
        } catch (IOException e1) {
        }
        */

        // Android UI parts 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // this will hand our messages to Android UI 
        ui = new AndroidUi(this);
        // let's create a graphic table on the UI
        ui.createTable(items, fields, fieldNames);
        //show the status of the connection
        ui.onStatusChange(phase.get(), LightstreamerConnectionStatus.DISCONNECTED);
        
        // this will handle the connection with Lightstreamer
        ls = new LightstreamerClient(items, fields);
    }

    private void start(int ph) throws NotFoundException {
        // Asynchronously start
        Thread th = new Thread(new Runnable() {
            AndroidUi ui;
            LightstreamerClient ls;
            int ph;

            @Override
            public void run() {
                try {
                    if (!checkPhase(ph)) {
                        return;
                    }
                    ph = phase.incrementAndGet();
                    ls.start(ph, getResources().getString(R.string.host), ui);
                    if (!checkPhase(ph)) {
                        return;
                    }
                    ls.subscribe(ph, ui);

                } catch (PushConnException pce) {
                    ui.onStatusChange(ph,
                            LightstreamerConnectionStatus.CONNECTION_ERROR);
                } catch (PushServerException pse) {
                    ui.onStatusChange(ph,
                            LightstreamerConnectionStatus.SERVER_ERROR);
                } catch (PushUserException pue) {
                    ui.onStatusChange(ph,
                            LightstreamerConnectionStatus.ERROR);
                } catch (SubscrException e) {
                    e.printStackTrace();
                    ui.onStatusChange(ph,
                            LightstreamerConnectionStatus.ERROR);
                }
            }
            
            public Runnable setData(int ph, AndroidUi ui, LightstreamerClient ls) {
                this.ph = ph;
                this.ui = ui;
                this.ls = ls;
                return this;
            }

        }.setData(ph, ui, ls));
        th.setName("StockListDemo.Lightstreamer.start-" + ph);
        th.start();
    }

    private void stop(int ph) {
        // Asynchronously stop
        Thread th = new Thread(new Runnable() {
            LightstreamerClient ls;
            int ph;

            @Override
            public void run() {
                if (!checkPhase(ph)) {
                    return;
                }
                ls.stop();
            }
            
            public Runnable setData(int ph, LightstreamerClient ls) {
                this.ph = ph;
                this.ls = ls;
                return this;
            }

        }.setData(ph, ls));
        th.setName("StockListDemo.Lightstreamer.stop-" + ph);
        th.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
         * if user explicitly chose to disconnect the application
         * before hiding it, do not start it back once it's back
         * visible.
         */
        if (!userDisconnect) {
            start(phase.get());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // disconnect when application is paused
        stop(phase.get());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ui.changeStartStopMenuItemState(
            menu.findItem(R.id.startStopItem),
            !isConnected());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.startStopItem) {
            // Start/Stop Menu options
            if (isConnected()) {
                // disconnect
                userDisconnect = true;
                stop(phase.get());
            } else {
                // reconnect
                userDisconnect = false;
                stop(phase.get());
                start(phase.get());
            }
        }
        return true;
    }
    
}
