package com.sbpinvertor.modbus.net;

import com.sbpinvertor.modbus.Modbus;
import com.sbpinvertor.modbus.exception.ModbusTransportException;
import com.sbpinvertor.modbus.utils.ByteFifo;
import com.sbpinvertor.modbus.utils.DataUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Copyright (c) 2015-2016 JSC "Zavod "Invertor"
 * [http://www.sbp-invertor.ru]
 * <p/>
 * This file is part of JLibModbus.
 * <p/>
 * JLibModbus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Authors: Vladislav Y. Kochedykov, software engineer.
 * email: vladislav.kochedykov@gmail.com
 */
final public class ModbusTransportTCP extends ModbusTransport {

    final private String host;
    final private int port;
    final private boolean keepAlive;
    final private AduHeader headerIn = new AduHeader();
    final private AduHeader headerOut = new AduHeader();
    private final byte[] pdu = new byte[Modbus.MAX_PDU_LENGTH];
    private Socket socket;
    private BufferedInputStream is;
    private BufferedOutputStream os;

    public ModbusTransportTCP(String host, int port, boolean keepAlive) {
        this.host = host;
        this.port = port;
        this.keepAlive = keepAlive;

        try {
            if (keepAlive)
                openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ModbusTransportTCP(String host, boolean keepAlive) {
        this(host, Modbus.TCP_PORT, keepAlive);
    }

    public ModbusTransportTCP(String host) {
        this(host, Modbus.TCP_PORT, false);
    }

    @Override
    synchronized public void send(ByteFifo pdu) throws ModbusTransportException {
        if (!keepAlive)
            openConnection();
        try {
            try {
                sendAdu(pdu);
            } catch (Exception e) {
                if (keepAlive) {
                    openConnection();
                    sendAdu(pdu);
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new ModbusTransportException(e);
        }
    }

    private void sendAdu(ByteFifo pdu) throws ModbusTransportException {
        try {
            // modbus tcp adu header
            write(headerOut.update(pdu.size()));
            // modbus pdu
            write(pdu.toByteArray());
            send();
        } catch (Exception e) {
            throw new ModbusTransportException(e);
        }
    }

    private void send() throws IOException {
        os.flush();
    }

    private void write(byte[] update) throws IOException {
        os.write(update);
    }

    @Override
    synchronized public void recv(ByteFifo pdu) throws ModbusTransportException {
        try {
            //read modbus tcp adu header
            read(headerIn.byteArray(), AduHeader.SIZE);
            if (headerIn.getPduSize() > Modbus.MAX_TCP_ADU_LENGTH) {
                throw new ModbusTransportException("Maximum ADU size is reached");
            }
            pdu.write(read(this.pdu, headerIn.getPduSize()));
        } catch (Exception e) {
            throw new ModbusTransportException(e);
        } finally {
            if (!keepAlive)
                closeConnection();
        }
    }

    private byte[] read(byte[] buffer, int size) throws IOException {
        int count = 0;
        while (count < size) {
            count += is.read(buffer, count, size - count);
        }
        return buffer;
    }

    synchronized private void openConnection() throws ModbusTransportException {
        closeConnection();
        try {
            socket = new Socket();
            socket.setKeepAlive(keepAlive);
            socket.setSoTimeout(Modbus.MAX_RESPONSE_TIMEOUT);
            socket.connect(new InetSocketAddress(host, port), Modbus.MAX_CONNECTION_TIMEOUT);
            is = new BufferedInputStream(socket.getInputStream());
            os = new BufferedOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            closeConnection();
            throw new ModbusTransportException(e);
        }
    }

    synchronized private void closeConnection() throws ModbusTransportException {
        try {
            if (socket != null)
                socket.close();
        } catch (Exception e) {
            throw new ModbusTransportException(e);
        } finally {
            is = null;
            os = null;
            socket = null;
        }
    }

    final static private class AduHeader {
        final static public int SIZE = 6;
        final private byte[] buffer;

        public AduHeader() {
            buffer = new byte[6];
            setProtocolId(Modbus.PROTOCOL_ID);
        }

        public void setBufferValue(int value, int offset) {
            buffer[offset++] = DataUtils.byteHigh(value);
            buffer[offset] = DataUtils.byteLow(value);
        }

        public short getPduSize() {
            return DataUtils.toShort(buffer[4], buffer[5]);
        }

        public void setPduSize(int value) {
            setBufferValue(value, 4);
        }

        public short getProtocolId() {
            return DataUtils.toShort(buffer[2], buffer[3]);
        }

        public void setProtocolId(int value) {
            setBufferValue(value, 2);
        }

        public short getTransactionId() {
            return DataUtils.toShort(buffer[0], buffer[1]);
        }

        public void setTransactionId(int value) {
            setBufferValue(value, 0);
        }

        public void update(byte[] header) {
            System.arraycopy(header, 0, buffer, 0, buffer.length);
        }

        public byte[] byteArray() {
            return buffer;
        }

        private byte[] update(int pduSize) {
            //transaction id (2 bytes, BE)
            setTransactionId(getTransactionId() + 1);
            //size of PDU (2 bytes, BE)
            setPduSize(pduSize);
            return buffer;
        }
    }
}