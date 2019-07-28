
-include .env_secret # load secrets if file exists

DATOMIC_VERSION=0.9.5930
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
start:
	test -n "$(DATOMIC_VERSION)"  # $$DATOMIC_VERSION
	docker start datomic-$(DATOMIC_VERSION)
	lein run

# start:
# 	make up
# 	docker run -it --rm --net cleo-global-net --name wait eremec/wait
# 	clj -A:clj:dev -m "start"


# .PHONY: datomic-start
# datomic-start:
# 	~/pet-projects/datomic/datomic-pro-0.9.5927/bin/transactor config/dev-transactor.properties &


# $ lein with-profile dev run
# Performing task 'run' with profile(s): 'dev'
# active profile :dev

# $ lein with-profile prod run
# Performing task 'run' with profile(s): 'prod'
# active profile :prod



.PHONY: stop
stop:
	# kill -9 %1 %2



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

# .PHONY: zzz
# zzz:
# 	lein doo chrome-headless test once


###################################################################################################################################


.PHONY: install
install: _install-karma _install-datomic-docker


.PHONY: _install-karma
_install-karma:
	npm install -g karma-cli
	npm install karma karma-cljs-test karma-chrome-launcher --save-dev


# without Docker
# .PHONY: datomic-ci-start
# datomic-ci-start:
# 	curl -u $(DATOMIC_USERNAME):$(DATOMIC_PASSWORD) -SL https://my.datomic.com/repo/com/datomic/datomic-pro/$(DATOMIC_VERSION)/datomic-pro-$(DATOMIC_VERSION).zip -o datomic.zip
# 	unzip datomic.zip
# 	# rm -f datomic.zip
# 	# cat test/datomic_template/dev-transactor-template.properties | sed "s/license-key=/license-key=$(DATOMIC_LICENSE_KEY)/" > datomic-pro-$(DATOMIC_VERSION)/config/dev-transactor.properties
# 	cat datomic-pro-$(DATOMIC_VERSION)/config/samples/dev-transactor-template.properties | sed "s/license-key=/license-key=$(DATOMIC_LICENSE_KEY)/" > datomic-pro-$(DATOMIC_VERSION)/config/dev-transactor.properties
# 	./datomic-pro-$(DATOMIC_VERSION)/bin/transactor config/dev-transactor.properties &


.PHONY: _install-datomic-docker
_install-datomic-docker: Dockerfile_datomic
	test -n "$(DATOMIC_VERSION)"  # $$DATOMIC_VERSION
	test -n "$(DATOMIC_USERNAME)"  # $$DATOMIC_USERNAME
	test -n "$(DATOMIC_PASSWORD)"  # $$DATOMIC_PASSWORD
	test -n "$(DATOMIC_LICENSE_KEY)"  # $$DATOMIC_LICENSE_KEY
	test -n "$(DATOMIC_STORAGE_ADMIN_PASSWORD)"  # $$DATOMIC_STORAGE_ADMIN_PASSWORD
	test -n "$(DATOMIC_STORAGE_DATOMIC_PASSWORD)"  # $$DATOMIC_STORAGE_DATOMIC_PASSWORD
	cat config/dev-transactor-template.properties | envsubst > config/dev-transactor.properties
	# cat config/dev-transactor.properties | sed "s/license-key=/license-key=$(DATOMIC_LICENSE_KEY)/" > config/dev-transactor.properties
	# docker rm -f datomic-$(DATOMIC_VERSION)
	# docker rmi datomic-pro-starter:$(DATOMIC_VERSION)
	docker build -f Dockerfile_datomic --build-arg DATOMIC_USERNAME=$(DATOMIC_USERNAME) --build-arg DATOMIC_PASSWORD=$(DATOMIC_PASSWORD) --build-arg DATOMIC_VERSION=$(DATOMIC_VERSION) -t datomic-pro-starter:$(DATOMIC_VERSION) .
	docker run -d -p 4334:4334 -p 4335:4335 -p 4336:4336 --name datomic-$(DATOMIC_VERSION) datomic-pro-starter:$(DATOMIC_VERSION)
