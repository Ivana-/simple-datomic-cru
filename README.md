# Simple Datomic CRU

Allows to create, edit, see current state and history of orders entities in database. 

## Install Datomic

<https://www.datomic.com/get-datomic.html> (Starter - free)

## Development Mode

### Start Datomic transactor:

```
bin/transactor <your-config-file>
```

By default, it starts at `datomic:dev://localhost:4334/`

### Run server:

Set environment variables (getting from your Datomic registration)

```
DATOMIC_USERNAME
DATOMIC_PASSWORD
```

Start REPL

```
lein repl
```

or use any built-in REPL in your editor/IDE.
Then evaluate `(-main)` from `server.clj`. Server will run on port 3000 by default.

### Run client:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).
