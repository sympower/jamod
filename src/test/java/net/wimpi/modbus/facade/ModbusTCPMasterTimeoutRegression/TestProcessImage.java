package net.wimpi.modbus.facade.ModbusTCPMasterTimeoutRegression;

import net.wimpi.modbus.procimg.DigitalIn;
import net.wimpi.modbus.procimg.DigitalOut;
import net.wimpi.modbus.procimg.IllegalAddressException;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.ProcessImageImplementation;
import net.wimpi.modbus.procimg.Register;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TestProcessImage implements ProcessImageImplementation {

  private final HashMap<Integer, Register> registers = new HashMap<>();

  @Override
  public DigitalOut[] getDigitalOutRange(int offset, int count) throws IllegalAddressException {
    return new DigitalOut[0];
  }

  @Override
  public DigitalOut getDigitalOut(int ref) throws IllegalAddressException {
    return null;
  }

  @Override
  public int getDigitalOutCount() {
    return 0;
  }

  @Override
  public DigitalIn[] getDigitalInRange(int offset, int count) throws IllegalAddressException {
    return new DigitalIn[0];
  }

  @Override
  public DigitalIn getDigitalIn(int ref) throws IllegalAddressException {
    return null;
  }

  @Override
  public int getDigitalInCount() {
    return 0;
  }

  @Override
  public InputRegister[] getInputRegisterRange(int offset, int count) throws IllegalAddressException {
    return new InputRegister[0];
  }

  @Override
  public InputRegister getInputRegister(int ref) throws IllegalAddressException {
    return null;
  }

  @Override
  public int getInputRegisterCount() {
    return 0;
  }

  @Override
  public Register[] getRegisterRange(int offset, int count) throws IllegalAddressException {
    final List<Register> result = new LinkedList<>();
    for (int i = offset; i < (offset + count) && registers.containsKey(i); i++) {
      result.add(registers.get(i));
    }
    return result.toArray(new Register[0]);
  }

  @Override
  public Register getRegister(int ref) throws IllegalAddressException {
    if (registers.containsKey(ref)) {
      return registers.get(ref);
    }
    throw new IllegalAddressException();
  }

  @Override
  public int getRegisterCount() {
    return 0;
  }

  @Override
  public void setDigitalOut(int ref, DigitalOut _do) throws IllegalAddressException {
    throw new NotImplementedException();
  }

  @Override
  public void addDigitalOut(DigitalOut _do) {
    throw new NotImplementedException();
  }

  @Override
  public void removeDigitalOut(DigitalOut _do) {
    throw new NotImplementedException();
  }

  @Override
  public void setDigitalIn(int ref, DigitalIn di) throws IllegalAddressException {
    throw new NotImplementedException();
  }

  @Override
  public void addDigitalIn(DigitalIn di) {
    throw new NotImplementedException();
  }

  @Override
  public void removeDigitalIn(DigitalIn di) {
    throw new NotImplementedException();
  }

  @Override
  public void setInputRegister(int ref, InputRegister reg) throws IllegalAddressException {
    throw new NotImplementedException();
  }

  @Override
  public void addInputRegister(InputRegister reg) {
    throw new NotImplementedException();
  }

  @Override
  public void removeInputRegister(InputRegister reg) {
    throw new NotImplementedException();
  }

  @Override
  public void setRegister(int ref, Register reg) throws IllegalAddressException {
    registers.put(ref, reg);
  }

  @Override
  public void addRegister(Register reg) {
    throw new NotImplementedException();
  }

  @Override
  public void removeRegister(Register reg) {
    throw new NotImplementedException();
  }

}
