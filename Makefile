DEVEL=true

DATOMIC_USERNAME = ivana_2004@mail.ru
DATOMIC_PASSWORD = 325e0e0f-d426-4768-9cbf-bd2cd42c478c


.EXPORT_ALL_VARIABLES:


# http://qaru.site/questions/770124/running-two-processes-in-parallel-from-makefile
# https://github.com/technomancy/leiningen/issues/1173

# So you have to do this manually yourself if you need to run the program in the background, by doing one of the following:

# bash -c "lein run &" works
# lein trampoline run & works (if you dont' read from stdin)
# lein run <&- & works
# lein run </dev/null & works
# All of them seems to work fine, although lein trampoline is running the program in a non-embedded process and may cut memory usage (look at trampoline for more information about how it works).













.PHONY: backend-test
backend-test:
	# make up
	# docker run -it --rm --net cleo-global-net --name wait eremec/wait
	# clj -A:clj:dev -m "cognitect.test-runner" -d "backend/test"
	lein test


# make ui-test -j2

.PHONY: ui-test
ui-test: preparings-for-ci-tests ui-test-core

.PHONY: preparings-for-ci-tests
preparings-for-ci-tests: karma datomic-start start

.PHONY: ui-test-core
ui-test-core:
	# make up
	# docker run -it --rm --net cleo-global-net --name wait eremec/wait
	# clj -A:clj:dev -m "cognitect.test-runner" -d "ui/test"
	# lein run
	lein doo chrome-headless test once

.PHONY: start
start:
	lein run &



.PHONY: transactor-config
transactor-config:
	# cd datomic-pro-0.9.5927
	# ./bin/transactor config/dev-transactor.properties
	cat dev-transactor-template.properties | sed "s/license-key=/license-key=$(DATOMIC_LICENSE_KEY)/" > datomic-pro-0.9.5927/config/dev-transactor.properties


.PHONY: datomic-start
datomic-start: transactor-config
	# cd datomic-pro-0.9.5927
	# ./bin/transactor config/dev-transactor.properties
	./datomic-pro-0.9.5927/bin/transactor config/dev-transactor.properties &



.PHONY: karma
karma:
	npm install -g karma-cli
	npm install karma karma-cljs-test karma-chrome-launcher --save-dev





# target: run_server run_client

# run_server:
#      ./server
# run_client:
#      ./client



# config:
# 	docker-compose config

# up:
# 	docker-compose up -d

# down:
# 	docker-compose down

# build:
# 	docker-compose up -d --build
# 	docker build -t eremec/wait infra/wait/

# cljs:
# 	clj -A:cljs "-m" cljs

# clj:
# 	clj -A:clj -m mach.pack.alpha.capsule "target/app.jar" -A:clj

# figwheel:
# 	clj -A:cljs:figwheel -m "figwheel.main" -b "dev"

# repl:
# 	make up
# 	docker run -it --rm --net cleo-global-net --name wait eremec/wait
# 	clj -A:clj:dev -m "nrepl"

# start:
# 	make up
# 	docker run -it --rm --net cleo-global-net --name wait eremec/wait
# 	clj -A:clj:dev -m "start"

# test:
# 	make up
# 	# docker run -it --rm --net cleo-global-net --name wait eremec/wait
# 	clj -A:clj:dev -m "cognitect.test-runner" -d "backend/test" -d "ui/test"

# backend-test:
# 	make up
# 	docker run -it --rm --net cleo-global-net --name wait eremec/wait
# 	clj -A:clj:dev -m "cognitect.test-runner" -d "backend/test"

# ui-test:
# 	make up
# 	docker run -it --rm --net cleo-global-net --name wait eremec/wait
# 	clj -A:clj:dev -m "cognitect.test-runner" -d "ui/test"



# DOCKER_IMAGE=datomic-pro-starter
# #pointslope/datomic-pro-starter
# DOCKER_TAG?=0.9.5927
# #$(shell ./datomic-version)

# .PHONY: all build run run-bash clean info

# # all: Dockerfile
# # 	docker build -t $(DOCKER_IMAGE):$(DOCKER_TAG) .

# build: Dockerfile
# 	docker build -t $(DOCKER_IMAGE):$(DOCKER_TAG) .

# run:
# 	docker run $(DOCKER_IMAGE):$(DOCKER_TAG)

# run-bash:
# 	docker run --rm -it $(DOCKER_IMAGE):$(DOCKER_TAG) bash

# clean:
# 	docker rmi $(DOCKER_IMAGE):$(DOCKER_TAG)

# info:
# 	@echo "Docker image: $(DOCKER_IMAGE):$(DOCKER_TAG)"