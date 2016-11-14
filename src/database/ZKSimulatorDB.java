package database;

import benchmark.FSDB;
import benchmark.Status;

import java.util.Properties;

public class ZKSimulatorDB implements FSDB {

    @Override
    public void init(String address, Properties properties) {
    }

    @Override
    public Status create(String path, byte[] data) {
        return null;
    }

    @Override
    public Status delete(String path) {
        return null;
    }

    @Override
    public byte[] read(String path) {
        return new byte[0];
    }

    @Override
    public Status write(String path, byte[] data) {
        return null;
    }

    @Override
    public void cleanup() {

    }
}
