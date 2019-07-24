DATOMIC_VERSION=0.9.5927

.EXPORT_ALL_VARIABLES:


# http://qaru.site/questions/770124/running-two-processes-in-parallel-from-makefile
# https://github.com/technomancy/leiningen/issues/1173

# So you have to do this manually yourself if you need to run the program in the background, by doing one of the following:

# bash -c "lein run &" works
# lein trampoline run & works (if you dont' read from stdin)
# lein run <&- & works
# lein run </dev/null & works
# All of them seems to work fine, although lein trampoline is running the program in a non-embedded process and may cut memory usage (look at trampoline for more information about how it works).


.PHONY: start
start: datomic-start server-start


.PHONY: datomic-start
datomic-start:
	~/pet-projects/datomic/datomic-pro-0.9.5927/bin/transactor config/dev-transactor.properties &


.PHONY: server-start
server-start:
	# lein run &
	lein run </dev/null &


.PHONY: stop
stop:
	kill -9 %1 %2


.PHONY: test
test: backend-test ui-test


.PHONY: backend-test
backend-test: start
	# make up
	# docker run -it --rm --net cleo-global-net --name wait eremec/wait
	# clj -A:clj:dev -m "cognitect.test-runner" -d "backend/test"
	lein test


# make ui-test -j2

.PHONY: ui-test
ui-test: start
	lein doo chrome-headless test once


# for CI only!!!

.PHONY: preparings-for-ci-tests
preparings-for-ci-tests: install-karma datomic-ci-start server-start


.PHONY: install-karma
install-karma:
	npm install -g karma-cli
	npm install karma karma-cljs-test karma-chrome-launcher --save-dev


.PHONY: datomic-ci-start
datomic-ci-start:
	curl -u $(DATOMIC_USERNAME):$(DATOMIC_PASSWORD) -SL https://my.datomic.com/repo/com/datomic/datomic-pro/$(DATOMIC_VERSION)/datomic-pro-$(DATOMIC_VERSION).zip -o datomic.zip
	unzip datomic.zip
	# rm -f datomic.zip
	# cat test/datomic_template/dev-transactor-template.properties | sed "s/license-key=/license-key=$(DATOMIC_LICENSE_KEY)/" > datomic-pro-$(DATOMIC_VERSION)/config/dev-transactor.properties
	cat datomic-pro-$(DATOMIC_VERSION)/config/samples/dev-transactor-template.properties | sed "s/license-key=/license-key=$(DATOMIC_LICENSE_KEY)/" > datomic-pro-$(DATOMIC_VERSION)/config/dev-transactor.properties
	./datomic-pro-$(DATOMIC_VERSION)/bin/transactor config/dev-transactor.properties &


# for CI datomic-docker!!!

.PHONY: preparings-for-ci-tests-1
preparings-for-ci-tests-1: install-karma datomic-docker-start server-start


.PHONY: datomic-docker-start
datomic-docker-start: Dockerfile_datomic
	cat config/dev-transactor.properties | sed "s/license-key=/license-key=$(DATOMIC_LICENSE_KEY)/" > config/dev-transactor.properties
	# docker build -f Dockerfile_datomic -t $(DOCKER_IMAGE):$(DATOMIC_VERSION) .
	docker build -f Dockerfile_datomic --build-arg DATOMIC_USERNAME=$(DATOMIC_USERNAME) --build-arg DATOMIC_PASSWORD=$(DATOMIC_PASSWORD) --build-arg DATOMIC_VERSION=$(DATOMIC_VERSION) -t datomic-pro-starter:$(DATOMIC_VERSION) .
	docker run -d -p 4334:4334 -p 4335:4335 -p 4336:4336 --name datomic datomic-pro-starter:$(DATOMIC_VERSION)


.PHONY: ttt
ttt: Dockerfile_ttt
	docker build -f Dockerfile_ttt --build-arg DATOMIC_USERNAME=zazaza --build-arg DATOMIC_PASSWORD=zwzwzw --build-arg DATOMIC_VERSION=zezeze -t ttt:1 .
