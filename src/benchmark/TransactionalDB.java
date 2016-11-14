package benchmark;

public interface TransactionalDB {

    Status start();

    Status commit();

    Status abort();
}
