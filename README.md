# Simple Datomic CRU

Allows to create, edit, see current state and history of orders entities in database.

List view with filters & history
![alt text](https://user-images.githubusercontent.com/10473034/61061608-386d7500-a405-11e9-9f15-fc60757c42dc.png "List view")

Edit form view
![alt text](https://user-images.githubusercontent.com/10473034/61061423-e6c4ea80-a404-11e9-911b-061c12da9b82.png "Edit order form")

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
