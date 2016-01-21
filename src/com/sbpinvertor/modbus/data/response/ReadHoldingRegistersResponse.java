package com.sbpinvertor.modbus.data.response;

import com.sbpinvertor.modbus.ModbusFunction;
import com.sbpinvertor.modbus.data.base.AbstractReadResponse;
import com.sbpinvertor.modbus.exception.ModbusNumberException;
import com.sbpinvertor.modbus.utils.ByteFifo;
import com.sbpinvertor.modbus.utils.DataUtils;

import java.io.IOException;

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
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Authors: Vladislav Y. Kochedykov, software engineer.
 * email: vladislav.kochedykov@gmail.com
 */
public class ReadHoldingRegistersResponse extends AbstractReadResponse {

    private int[] registers;

    public ReadHoldingRegistersResponse(int serverAddress) throws ModbusNumberException {
        super(serverAddress);
    }

    ReadHoldingRegistersResponse(int serverAddress, int[] registers) throws ModbusNumberException {
        super(serverAddress, registers.length * 2);
    }

    public int[] getRegisters() {
        return registers;
    }

    @Override
    protected void readData(ByteFifo fifo) throws IOException {
        byte[] buffer = new byte[getByteCount()];
        fifo.read(buffer);
        registers = DataUtils.toRegistersArray(buffer);
    }

    @Override
    protected void writeData(ByteFifo fifo) throws IOException {
        fifo.write(DataUtils.toByteArray(registers));
    }

    @Override
    public ModbusFunction getFunction() {
        return ModbusFunction.READ_HOLDING_REGISTERS;
    }
}