package benchmark;

public interface FSDB extends DB {

    Status create(String path, byte[] data);

    Status delete(String path);

    byte[] read(String path);

    Status write(String path, byte[] data);

}
