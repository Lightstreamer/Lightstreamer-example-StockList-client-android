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

import com.lightstreamer.ls_client.UpdateInfo;

/**
 * This is the interface that shall be passed to
 * StockListConnectionListener and HandyTableListener
 * in order to respectively receive Lightstreamer
 * connection and data events.
 * In this project, this interface is implemented
 * by AndroidUi, which receives both connection
 * and data infos.
 */
public interface LightstreamerListener
{
    // Lightstreamer Client connection status changes arrive here
    void onStatusChange(int phase, int status);
    // Lightstreamer Client data updates arrive here
    void onItemUpdate(int phase, int itemPos, String itemName, UpdateInfo update);
    // Lightstreamer Client lost updates info arrives here
    void onLostUpdate(int phase, int itemPos, String itemName, int lostUpdates);
    // Connection Listener is requesting a reconnection
    void onReconnectRequest(int phase);
}
