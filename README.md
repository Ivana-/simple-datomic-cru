# Simple Datomic CRU

Allows to create, edit, see current state and history of orders entities in database.

![alt text](https://user-images.githubusercontent.com/10473034/60931920-364fcd00-a2c4-11e9-841a-5970d5562bb9.png "List view")

![alt text](https://user-images.githubusercontent.com/10473034/60931929-3ea80800-a2c4-11e9-9fc1-c149153733a9.png "New order form")

![alt text](https://user-images.githubusercontent.com/10473034/60931932-45cf1600-a2c4-11e9-84a2-09378948c40f.png "Edit order form")

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
