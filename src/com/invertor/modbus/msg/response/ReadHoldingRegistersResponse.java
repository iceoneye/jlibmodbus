package com.invertor.modbus.msg.response;

import com.invertor.modbus.Modbus;
import com.invertor.modbus.exception.ModbusNumberException;
import com.invertor.modbus.msg.base.AbstractReadResponse;
import com.invertor.modbus.net.stream.base.ModbusInputStream;
import com.invertor.modbus.net.stream.base.ModbusOutputStream;
import com.invertor.modbus.utils.DataUtils;
import com.invertor.modbus.utils.ModbusFunctionCode;

import java.io.IOException;
import java.util.Arrays;

/*
 * Copyright (C) 2016 "Invertor" Factory", JSC
 * [http://www.sbp-invertor.ru]
 *
 * This file is part of JLibModbus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Vladislav Y. Kochedykov, software engineer.
 * email: vladislav.kochedykov@gmail.com
 */
public class ReadHoldingRegistersResponse extends AbstractReadResponse {

    private byte[] buffer = new byte[0];

    public ReadHoldingRegistersResponse() {
        super();
    }

    /**
     * returns a copy of the raw byte-buffer
     *
     * @return registers bytes
     */
    synchronized final public byte[] getBytes() {
        return Arrays.copyOf(buffer, buffer.length);
    }

    synchronized final public int[] getRegisters() {
        return DataUtils.toIntArray(buffer);
    }

    synchronized final public void setBuffer(int[] registers) throws ModbusNumberException {
        this.buffer = DataUtils.toByteArray(registers);
        setByteCount(this.buffer.length);
    }

    @Override
    synchronized final protected void readData(ModbusInputStream fifo) throws IOException {
        if (buffer.length != getByteCount())
            buffer = new byte[getByteCount()];
        int size;
        if ((size = fifo.read(buffer, 0, getByteCount())) < getByteCount())
            Modbus.log().warning(getByteCount() + " bytes expected, but " + size + " received.");
    }

    @Override
    synchronized final protected void writeData(ModbusOutputStream fifo) throws IOException {
        fifo.write(buffer, 0, getByteCount());
    }

    @Override
    public int getFunction() {
        return ModbusFunctionCode.READ_HOLDING_REGISTERS.toInt();
    }
}
