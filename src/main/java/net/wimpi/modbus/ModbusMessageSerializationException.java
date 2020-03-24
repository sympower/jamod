package net.wimpi.modbus;

public class ModbusMessageSerializationException extends RuntimeException {

    public ModbusMessageSerializationException(Throwable cause) {
        super("Failed to serialize MODBUS message", cause);
    }

}
