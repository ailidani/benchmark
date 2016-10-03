OpenBenchmark
=============

Quick overview
--------------
Distributed Benchmark is a micro-benchmark framework similar to YCSB, with more features and fine control over a cluster of clients running on multiple machines.
DB code base is kept as simple and compact as possible, so that anyone can easily extend the system.


|                           | OpenBenchmark |    [YCSB](https://github.com/brianfrankcooper/YCSB)    |[YCSB++](http://www.pdl.cmu.edu/ycsb++/)| [YCSB+T](https://github.com/brianfrankcooper/YCSB/pull/169) | UPB |  [BG](http://bgbenchmark.org/BG/)  |
|---------------------------|:-------------:|:------------:|:------:|:------:|:---:|:------:|
| Multiple Clients          |       ✓       |       ✓      |    ✓   |        |     |        |
| Distributed Clients       |       ✓       |              |    ✓   |        |     |        |
| Distributed Loadings      |       ✓       |              |    ✓   |        |     |        |
| Key Distributions         |       ✓       |       ✓      |    ✓   |        |     |        |
| Key Overlap               |       ✓       |              |        |        |     |        |
| Multiple Key Types        |       ✓       | Only Strings |        |        |     |        |
| Throttled Throughput      |       ✓       |       ✓      |    ✓   |        |     |        |
| Hierarchical Key Space    |       ✓       |              |        |        |     |        |
| Timeseries Granularity    |       ✓       |       ✓      |    ✓   |        |     |        |
| Consistency Test          |               |              |    ✓   |        |     |        |
| Multi-key Transactions    |               |              |        |    ✓   |     |        |
| Real Application Workload |               |              |        |        |     | Social |


Tasks List
----------

- [x] Distributed clients key overlap
- [ ] Hierarchical key space
- [ ] Client location assignment
- [ ] Fix [Coordinated Omission Problem](https://www.youtube.com/watch?v=lJ8ydIuPFeU)


Getting Started
---------------
Start Coordinator

```shell
$ bin/coordinator.sh
```

Start Workers

```shell
$ bin/worker.sh
```

Stop Coordinator and Worker in current location

```shell
$ bin/stop.sh
```

### Configuration
The configuration file is a single properties file includes everything the benchmark needs.

|       Name       |     Default Value    |                                   Info                                   |
|------------------|----------------------|--------------------------------------------------------------------------|
| db               | database.SimulatorDB | User implemented DB interface, extends one of {DB, StringDB, etc}        |
| mode             | CENTRALIZED          | Running mode, CENTRALIZED or DISTRIBUTED                                 |
| recordcount      | 1000                 | Number of keys each client access                                        |
| datasize         | 100                  | Random bytes                                                             |
| getproportion    | 0                    | Get operation                                                            |
| putproportion    | 1                    | Put operation                                                            |
| removeproportion | 0                    | Remove operation                                                         |
| distribution     | Uniform              | Distribution of the keys {Constant, Sequence, Uniform, Hotspot, Zipfian} |
| overlap          | 1.0                  | Overlap fraction between client's accessing keys                         |
| interval         | 1.0                  | Stat result interval time in ms                                          |
| totaltime        | 60                   | Seconds                                                                  |
| clients          | 10                   | Number of clients                                                        |
| throttle         | -1                   | Target throughput throttle operations/second                             |
| address          | 127.0.0.1            | DB addresses                                                             |


