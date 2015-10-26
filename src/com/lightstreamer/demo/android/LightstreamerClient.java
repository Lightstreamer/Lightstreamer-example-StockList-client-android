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

import com.lightstreamer.ls_client.ConnectionInfo;
import com.lightstreamer.ls_client.ExtendedTableInfo;
import com.lightstreamer.ls_client.LSClient;
import com.lightstreamer.ls_client.PushConnException;
import com.lightstreamer.ls_client.PushServerException;
import com.lightstreamer.ls_client.PushUserException;
import com.lightstreamer.ls_client.SimpleTableInfo;
import com.lightstreamer.ls_client.SubscrException;

/**
 * This class wraps the real Lightstreamer Client object,
 * exposing start/stop methods for general consumption.
 * This class can be accessed concurrently.
 */
public class LightstreamerClient {

    private final String[] items;
    private final String[] fields;
    private final LSClient client;

    public LightstreamerClient(String[] items, String[] fields) {
        this.items = items;
        this.fields = fields;
        this.client = new LSClient();
    }

    public void stop() {
        this.client.closeConnection();
    }

    public void start(int phase, String pushServerUrl, LightstreamerListener listener)
            throws PushConnException, PushServerException, PushUserException {
        StockListConnectionListener ls = new StockListConnectionListener(listener, phase);
        ConnectionInfo connInfo = new ConnectionInfo();
        connInfo.pushServerUrl = pushServerUrl;
        connInfo.adapter = "DEMO";
        client.openConnection(connInfo, ls);
    }
    
    public void subscribe(int phase, LightstreamerListener listener)
            throws SubscrException, PushServerException, PushUserException, PushConnException {
        StocklistHandyTableListener hl = new StocklistHandyTableListener(phase, listener);
        SimpleTableInfo tableInfo = new ExtendedTableInfo(
                items, "MERGE", fields, true);
        tableInfo.setDataAdapter("QUOTE_ADAPTER");
        client.subscribeTable(tableInfo, hl, false);
    }

}


