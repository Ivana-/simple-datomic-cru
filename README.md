[![Build Status](https://travis-ci.org/Ivana-/simple-datomic-cru.svg?branch=master)](https://travis-ci.org/Ivana-/simple-datomic-cru)

# Simple Datomic CRU

Allows to create, edit, see current state and history of orders entities in database.

List view with filters & history

![alt text](https://user-images.githubusercontent.com/10473034/61061608-386d7500-a405-11e9-9f15-fc60757c42dc.png "List view")

Edit form view

![alt text](https://user-images.githubusercontent.com/10473034/61061423-e6c4ea80-a404-11e9-911b-061c12da9b82.png "Edit order form")

## Join Datomic registration (if not yet)

<https://www.datomic.com/get-datomic.html> (Starter - free)

Current Datomic version, used in this project, is `0.9.5930` but you can change it in both `project.clj` and `Makefile`

## Installation

### Set environment variables

Create file `.env_secret` with following entry:

```
export DATOMIC_USERNAME=<Technical Contact from https://my.datomic.com/account>
export DATOMIC_PASSWORD=<Download Key from https://my.datomic.com/account>
export DATOMIC_LICENSE_KEY=<Your personal license key, sended you by email>
export DATOMIC_STORAGE_ADMIN_PASSWORD=<Storage admin password - any non-empty string without whitespaces>
export DATOMIC_STORAGE_DATOMIC_PASSWORD=<Storage user password - any non-empty string without whitespaces>
```

### And run install script

```
make install
```

## Development Mode

### Run server:

```
make start
```

or open any built-in REPL in your editor/IDE and evaluate `(-main)` from `core.clj`. Server will run on port 3000 by default.

### Run client:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Testing

### Backend tests:

```
lein test
```

### Frontend tests:

```
lein doo chrome-headless test once
```