package net.wimpi.modbus.facade.ModbusTCPMasterTimeoutRegression;

import net.wimpi.modbus.procimg.Register;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SlowRegister implements Register {

  private final Object lock = new Object();
  private int lockedValue = 0;

  private final Queue<Long> delaysInMillisQueue = new ConcurrentLinkedQueue<>();

  public SlowRegister(final Collection<Long> delaysInMillis) {
    delaysInMillisQueue.addAll(delaysInMillis);
  }

  private void delay() {
    Long sleepTimeInMillis = delaysInMillisQueue.poll();
    if (sleepTimeInMillis != null) {
      try {
        Thread.sleep(sleepTimeInMillis);
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public int getValue() {
    delay();
    synchronized (lock) {
      return lockedValue;
    }
  }

  @Override
  public int toUnsignedShort() {
    return getValue() & 0xFFFF;
  }

  @Override
  public short toShort() {
    return (short) getValue();
  }

  @Override
  public byte[] toBytes() {
    int value = getValue();
    // big-endian
    return new byte[] { (byte) ((value >> 8) & 0xFF), (byte) ((value) & 0xFF) };
  }

  @Override
  public void setValue(int v) {
    delay();
    synchronized (lock) {
      lockedValue = v;
    }
  }

  @Override
  public void setValue(short s) {
    setValue((int) s);
  }

  @Override
  public void setValue(byte[] bytes) {
    // big-endian
    setValue(((int) bytes[0]) << 8 | ((int) bytes[1]));
  }

}
