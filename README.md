# datomic-riemann-reporter

A tiny clojure library that reports datomic metrics to riemann

## Usage

Drop an uberjar in $DATOMIC_DIR/lib, then add this to your transactor's `properties` file:

```ini
metrics-callback=datomic-riemann-reporter/report-datomic-metrics-to-riemann
```

Then you need to set these two environment variables:

```
RIEMANN_HOST=your_riemann_host
RIEMANN_PORT=port
```

Note you explicitly have to set port - this library doesn't have any defaults built into it.

Then restart your transactor, and you'll see events showing up in riemann. All
events will be tagged "datomic", and start with "datomic". Event names come
from the metrics available on http://docs.datomic.com/monitoring.html.

## License

Copyright © 2014 Tom Crayford

Distributed under the Eclipse Public License version 1.0
