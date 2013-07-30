/*
 * Copyright 2013 Weswit Srl
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

import com.lightstreamer.ls_client.HandyTableListener;
import com.lightstreamer.ls_client.UpdateInfo;

/**
 * This is the object that shall be passed to Lightstreamer
 * Client, receiving item-specific events.
 * onUpdate and onRawUpdatesLost events are routed to our
 * higher level LightstreamerListener object.
 */
class StocklistHandyTableListener implements HandyTableListener {

    private LightstreamerListener listener;
    private int phase;
    
    public StocklistHandyTableListener(int phase, LightstreamerListener listener) {
        this.listener = listener;
        this.phase = phase;
    }
    
    @Override
    public void onUpdate(int itemPos, String itemName, UpdateInfo update) {
        listener.onItemUpdate(phase, itemPos, itemName, update);
    }

    @Override
    public void onRawUpdatesLost(int itemPos, String itemName,
            int lostUpdates) {
        listener.onLostUpdate(phase, itemPos, itemName, lostUpdates);
    }

    @Override
    public void onSnapshotEnd(int itemPos, String itemName) {
    }

    @Override
    public void onUnsubscr(int itemPos, String itemName) {
    }

    @Override
    public void onUnsubscrAll() {
    }
    
}
