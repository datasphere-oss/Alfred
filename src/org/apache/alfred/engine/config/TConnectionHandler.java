/*******************************************************************************
 *
 *
 *  Copyright (c) 2022 Alfred
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package org.apache.alfred.engine.config;

import org.apache.alfred.util.collections.generated.IntegerToObjectHashMap;
import org.apache.alfred.util.lang.Util;
import org.apache.alfred.util.tomcat.ConnectionHandler;
import org.apache.alfred.util.tomcat.ConnectionHandshakeHandler;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/*
   Handlers mapped by fist byte
 */
public class TConnectionHandler implements ConnectionHandler {

    private final IntegerToObjectHashMap<ConnectionHandshakeHandler> handlers = new IntegerToObjectHashMap<ConnectionHandshakeHandler>();

    @Override
    public int getMarkLimit() {
        return (1);
    }

    public void addHandler(byte mark, ConnectionHandshakeHandler handler) {
        if (!handlers.put(mark, handler))
            throw new IllegalStateException("Handler for " + mark + " is already registered");
    }

    public boolean removeHandler(byte mark) {
        return handlers.remove(mark);
    }

    @Override
    public boolean handleConnection(Socket socket, BufferedInputStream bis, OutputStream os) throws IOException {
        bis.mark(getMarkLimit());
        int firstByte = bis.read(); // ConnectionInterceptor marked InputStream to allow reset (-1)
        bis.reset();

        ConnectionHandshakeHandler handler = handlers.get(firstByte, null);

        // switch (firstByte) {
        // case 0:
        // handler = framework;
        // break;
        //
        // case 48: // BER.SEQUENCE
        // handler = snmp;
        // break;
        //
        // case 24:
        // handler = rest;
        // break;
        //
        //// case 'F': // FIX.4.?
        //// handler = fix;
        //// break;
        //
        // default:
        // return (false);
        // }

        if (handler != null) {
            if (!handler.handleHandshake(socket, bis, os))
                socket.close();

            return (true);
        }

        return (false);
    }

    @Override
    public void close() {

        for (ConnectionHandshakeHandler handler : handlers) {
            if (handler instanceof Closeable)
                Util.close((Closeable) handler);
        }

        handlers.clear();
    }
}
