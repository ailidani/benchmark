Distributed Benchmark
=====================

Quick overview of DB
--------------------
Distributed Benchmark is a micro-benchmark framework similar to YCSB, with more features and fine control over a cluster of clients running on multiple machines.
DB code base is kept as simple and compact as possible, so that anyone can easily extend the system.


|                        | Distributed Benchmark |     YCSB     |
|------------------------|:---------------------:|:------------:|
| Multiple Clients       |           ✓           |       ✓      |
| Distributed Clients    |           ✓           |              |
| Distributed Loadings   |           ✓           |              |
| Key Distributions      |           ✓           |       ✓      |
| Key Overlap            |           ✓           |              |
| Multiple Key Types     |           ✓           | Only Strings |
| Throttled Throughput   |           ✓           |       ✓      |
| Hierarchical Key Space |           ✓           |              |
| Timeseries Granularity |           ✓           |       ✓      |


Tasks List
----------

- [x] Distributed clients key overlap
- [ ] Hierarchical key space
- [ ] Client location assignment
