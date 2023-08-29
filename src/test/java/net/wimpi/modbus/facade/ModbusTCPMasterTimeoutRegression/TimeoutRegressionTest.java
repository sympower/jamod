package net.wimpi.modbus.facade.ModbusTCPMasterTimeoutRegression;

import junit.framework.TestCase;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleRegister;

import java.net.InetAddress;
import java.util.Arrays;

public class TimeoutRegressionTest extends TestCase {
  private static final int TEST_REGISTER_ADDRESS = 10000;
  private static final int TEST_CLIENT_PORT = 12345;

  public void testTimeout() throws Exception {
    TestProcessImage spi = new TestProcessImage();
    SlowRegister slowRegister = new SlowRegister(Arrays.asList(5000L));

    spi.setRegister(TEST_REGISTER_ADDRESS, slowRegister);

    ModbusCoupler.getReference().setProcessImage(spi);
    ModbusCoupler.getReference().setMaster(false);
    ModbusCoupler.getReference().setUnitID(0);

    final ModbusTCPListener listener = new ModbusTCPListener(1, InetAddress.getLoopbackAddress());
    listener.setPort(TEST_CLIENT_PORT);
    listener.start();

    final ModbusTCPMaster modbusMaster = new ModbusTCPMaster("127.0.0.1", TEST_CLIENT_PORT);

    modbusMaster.connect();
    SimpleRegister register = new SimpleRegister(123);
    modbusMaster.writeSingleRegister(TEST_REGISTER_ADDRESS, register);
    // This fails without the fix with following exception:
    // java.lang.ClassCastException: net.wimpi.modbus.msg.WriteSingleRegisterResponse cannot be cast to net.wimpi.modbus.msg.ReadMultipleRegistersResponse
    modbusMaster.readMultipleRegisters(TEST_REGISTER_ADDRESS, 1);
  }
}
