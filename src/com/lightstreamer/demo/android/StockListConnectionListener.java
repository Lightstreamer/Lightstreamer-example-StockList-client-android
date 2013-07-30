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

import com.lightstreamer.ls_client.ConnectionListener;
import com.lightstreamer.ls_client.PushConnException;
import com.lightstreamer.ls_client.PushServerException;

class LightstreamerConnectionStatus {
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int STREAMING = 3;
    public static final int POLLING = 4;
    public static final int STALLED = 5;
    public static final int ERROR = 6;
    public static final int CONNECTION_ERROR = 7;
    public static final int SERVER_ERROR = 8;
}

/**
 * this class will receive the notifications on the status of the connection. 
 * Such information will be used to automatically reconnect and to send the status 
 * to a listener
 */
class StockListConnectionListener implements ConnectionListener {

    private LightstreamerListener listener;
    private boolean isPolling;
    private boolean reconnect = false;
    private int phase;

    public StockListConnectionListener(LightstreamerListener listener, int phase) {
        super();
        this.listener = listener;
        this.phase = phase;
    }

    public void automaticReconnect() {
        listener.onReconnectRequest(phase);
    }

    @Override
    public void onActivityWarning(boolean warningOn) {
        if (warningOn) {
            listener.onStatusChange(phase, LightstreamerConnectionStatus.STALLED);
        } else {
            this.onSessionStarted(this.isPolling);
        }
    }

    @Override
    public void onClose() {
        listener.onStatusChange(phase, LightstreamerConnectionStatus.DISCONNECTED);
        if (reconnect) {
            automaticReconnect();
            reconnect = false;
        }
    }

    @Override
    public void onConnectionEstablished() {
        listener.onStatusChange(phase, LightstreamerConnectionStatus.CONNECTED);
    }

    @Override
    public void onDataError(PushServerException e) {
        listener.onStatusChange(phase, LightstreamerConnectionStatus.ERROR);
    }

    @Override
    public void onEnd(int cause) {
        listener.onStatusChange(phase, LightstreamerConnectionStatus.DISCONNECTED);
        reconnect = true;
    }

    @Override
    public void onFailure(PushServerException e) {
        listener.onStatusChange(phase, LightstreamerConnectionStatus.SERVER_ERROR);
        reconnect = true;
    }

    @Override
    public void onFailure(PushConnException e) {
        listener.onStatusChange(phase, LightstreamerConnectionStatus.CONNECTION_ERROR);
        reconnect = true;
    }

    @Override
    public void onNewBytes(long b) { }

    @Override
    public void onSessionStarted(boolean isPolling) {
        this.isPolling = isPolling;
        if (this.isPolling) {
            listener.onStatusChange(phase, LightstreamerConnectionStatus.POLLING);
        } else {
            listener.onStatusChange(phase, LightstreamerConnectionStatus.STREAMING);
        }
    }

}
