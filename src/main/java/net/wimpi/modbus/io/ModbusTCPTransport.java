/**
 * Copyright 2002-2010 jamod development team
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
 ***/

package net.wimpi.modbus.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import net.wimpi.modbus.ModbusMessageSerializationException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.msg.ModbusMessage;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.util.ModbusUtil;

/**
 * Class that implements the Modbus transport
 * flavor.
 *
 * @author Dieter Wimberger
 * @version @version@ (@date@)
 */
public class ModbusTCPTransport implements ModbusTransport {

    private static final Logger logger = LoggerFactory.getLogger(ModbusTCPTransport.class);
    // instance attributes
    private DataInputStream m_Input; // input stream
    private DataOutputStream m_Output; // output stream
    private BytesInputStream m_ByteIn;
    private Socket m_Socket;

    /**
     * Constructs a new <tt>ModbusTransport</tt> instance,
     * for a given <tt>Socket</tt>.
     * <p>
     *
     * @param socket the <tt>Socket</tt> used for message transport.
     */
    public ModbusTCPTransport(Socket socket) {
        try {
            setSocket(socket);
        } catch (IOException ex) {
            final String errMsg = "Socket invalid";
            logger.error(errMsg, ex);
            // @commentstart@
            throw new IllegalStateException(errMsg);
            // @commentend@
        }
    }// constructor

    /**
     * Sets the <tt>Socket</tt> used for message transport and
     * prepares the streams used for the actual I/O.
     *
     * @param socket the <tt>Socket</tt> used for message transport.
     * @throws IOException if an I/O related error occurs.
     */
    public void setSocket(Socket socket) throws IOException {
        // Close existing socket and streams, if any
        close();
        m_Socket = socket;
        prepareStreams(socket);
    }// setSocket

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(m_Input);
        IOUtils.closeQuietly(m_Output);
        IOUtils.closeQuietly(m_Socket);
    }// close

    @Override
    public void writeMessage(ModbusMessage msg) throws ModbusIOException {
        try {
            byte[] serializedMessage = serialize(msg);
            // Drop any stale data from responses that were not handled (e.g. due to timeout)
            m_Input.skip(m_Input.available());
            m_Output.write(serializedMessage);
            m_Output.flush();
            // write more sophisticated exception handling
        } catch (IOException ex) {
            final String errMsg = "I/O exception - failed to write";
            logger.error(errMsg, ex);
            throw new ModbusIOException(String.format("%s: %s", errMsg, ex.getMessage()));
        }
    }// write

    private byte[] serialize(ModbusMessage msg) throws IOException {
        try (
                ByteArrayOutputStream buffer = new ByteArrayOutputStream(msg.getOutputLength());
                DataOutputStream bufferOutputStream = new DataOutputStream(buffer)
        ) {
            msg.writeTo(bufferOutputStream);
            bufferOutputStream.flush();
            return buffer.toByteArray();
        } catch (RuntimeException e) {
            throw new ModbusMessageSerializationException(e);
        }
    }

    @Override
    public ModbusRequest readRequest() throws ModbusIOException {

        // System.out.println("readRequest()");
        try {

            ModbusRequest req = null;
            synchronized (m_ByteIn) {
                // use same buffer
                byte[] buffer = m_ByteIn.getBuffer();

                // read to byte length of message
                m_Input.readFully(buffer, 0, 6);
                // extract length of bytes following in message
                int bf = ModbusUtil.registerToShort(buffer, 4);
                // read rest
                m_Input.readFully(buffer, 6, bf);
                m_ByteIn.reset(buffer, (6 + bf));
                m_ByteIn.skip(7);
                int functionCode = m_ByteIn.readUnsignedByte();
                m_ByteIn.reset();
                req = ModbusRequest.createModbusRequest(functionCode);
                req.readFrom(m_ByteIn);
            }
            return req;
            /*
             * int transactionID = m_Input.readUnsignedShort();
             * int protocolID = m_Input.readUnsignedShort();
             * int dataLength = m_Input.readUnsignedShort();
             * if (protocolID != Modbus.DEFAULT_PROTOCOL_ID || dataLength > 256) {
             * throw new ModbusIOException();
             * }
             * int unitID = m_Input.readUnsignedByte();
             * int functionCode = m_Input.readUnsignedByte();
             * ModbusRequest request =
             * ModbusRequest.createModbusRequest(functionCode, m_Input, false);
             * if (request instanceof IllegalFunctionRequest) {
             * //skip rest of bytes
             * for (int i = 0; i < dataLength - 2; i++) {
             * m_Input.readByte();
             * }
             * }
             * //set read parameters
             * request.setTransactionID(transactionID);
             * request.setProtocolID(protocolID);
             * request.setUnitID(unitID);
             * return request;
             *
             */
        } catch (EOFException eoex) {
            logger.trace("readRequest", eoex);
            throw new ModbusIOException(true);
        } catch (SocketException sockex) {
            // connection reset by peer, also EOF
            logger.trace("readRequest", sockex);
            throw new ModbusIOException(true);
        } catch (Exception ex) {
            final String errMsg = "I/O exception - failed to read request";
            logger.error(errMsg, ex);
            throw new ModbusIOException(errMsg);
        }
    }// readRequest

    @Override
    public ModbusResponse readResponse() throws ModbusIOException {
        // System.out.println("readResponse()");

        try {

            ModbusResponse res = null;
            synchronized (m_ByteIn) {
                // use same buffer
                byte[] buffer = m_ByteIn.getBuffer();

                // read to byte length of message
                m_Input.readFully(buffer, 0, 6);
                // extract length of bytes following in message
                int bf = ModbusUtil.registerToShort(buffer, 4);
                // read rest
                m_Input.readFully(buffer, 6, bf);
                m_ByteIn.reset(buffer, (6 + bf));
                m_ByteIn.skip(7);
                int functionCode = m_ByteIn.readUnsignedByte();
                m_ByteIn.reset();
                res = ModbusResponse.createModbusResponse(functionCode);
                res.readFrom(m_ByteIn);
            }
            return res;
            /*
             * try {
             * int transactionID = m_Input.readUnsignedShort();
             * //System.out.println("Read tid="+transactionID);
             * int protocolID = m_Input.readUnsignedShort();
             * //System.out.println("Read pid="+protocolID);
             * int dataLength = m_Input.readUnsignedShort();
             * //System.out.println("Read length="+dataLength);
             * int unitID = m_Input.readUnsignedByte();
             * //System.out.println("Read uid="+unitID);
             * int functionCode = m_Input.readUnsignedByte();
             * //System.out.println("Read fc="+functionCode);
             * ModbusResponse response =
             * ModbusResponse.createModbusResponse(functionCode, m_Input, false);
             * if (response instanceof ExceptionResponse) {
             * //skip rest of bytes
             * for (int i = 0; i < dataLength - 2; i++) {
             * m_Input.readByte();
             * }
             * }
             * //set read parameters
             * response.setTransactionID(transactionID);
             * response.setProtocolID(protocolID);
             * response.setUnitID(unitID);
             * return response;
             */
        } catch (Exception ex) {
            final String errMsg = "I/O exception - failed to read response";
            logger.error(errMsg, ex);
            throw new ModbusIOException(errMsg);
        }
    }// readResponse

    /**
     * Prepares the input and output streams of this
     * <tt>ModbusTCPTransport</tt> instance based on the given
     * socket.
     *
     * @param socket the socket used for communications.
     * @throws IOException if an I/O related error occurs.
     */
    private void prepareStreams(Socket socket) throws IOException {

        m_Input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        m_Output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        m_ByteIn = new BytesInputStream(Modbus.MAX_MESSAGE_LENGTH);
    }// prepareStreams

}// class ModbusTCPTransport
