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










.PHONY: backend-test
backend-test:
	# make up
	# docker run -it --rm --net cleo-global-net --name wait eremec/wait
	# clj -A:clj:dev -m "cognitect.test-runner" -d "backend/test"
	lein test


# make ui-test -j2

.PHONY: ui-test
ui-test: preparings-for-ci-tests ui-test-core


.PHONY: ui-test-core
ui-test-core:
	# make up
	# docker run -it --rm --net cleo-global-net --name wait eremec/wait
	# clj -A:clj:dev -m "cognitect.test-runner" -d "ui/test"
	# lein run
	lein doo chrome-headless test once




.PHONY: preparings-for-ci-tests
preparings-for-ci-tests: install-karma datomic-ci-start start


.PHONY: install-karma
install-karma:
	npm install -g karma-cli
	npm install karma karma-cljs-test karma-chrome-launcher --save-dev



# ONBUILD RUN curl -u $(cat /tmp/.credentials) -SL https://my.datomic.com/repo/com/datomic/datomic-pro/$DATOMIC_VERSION/datomic-pro-$DATOMIC_VERSION.zip -o /tmp/datomic.zip \
#   && unzip /tmp/datomic.zip -d /opt \
#   && rm -f /tmp/datomic.zip


# ONBUILD RUN curl -u $DATOMIC_CREDS -SL $DATOMIC_URL -o /tmp/datomic.zip \
RUN curl -u ivana_2004@mail.ru:325e0e0f-d426-4768-9cbf-bd2cd42c478c -SL https://my.datomic.com/repo/com/datomic/datomic-pro/0.9.5927/datomic-pro-0.9.5927.zip -o /tmp/datomic.zip
# RUN unzip /tmp/datomic.zip -d /opt


.PHONY: datomic-ci-start
datomic-ci-start:
	curl -u $(DATOMIC_USERNAME):$(DATOMIC_PASSWORD) -SL https://my.datomic.com/repo/com/datomic/datomic-pro/$(DATOMIC_VERSION)/datomic-pro-$(DATOMIC_VERSION).zip -o datomic.zip
	unzip datomic.zip
	# rm -f datomic.zip
	# cat test/datomic_template/dev-transactor-template.properties | sed "s/license-key=/license-key=$(DATOMIC_LICENSE_KEY)/" > datomic-pro-$(DATOMIC_VERSION)/config/dev-transactor.properties
	cat datomic-pro-$(DATOMIC_VERSION)/config/samples/dev-transactor-template.properties | sed "s/license-key=/license-key=$(DATOMIC_LICENSE_KEY)/" > datomic-pro-$(DATOMIC_VERSION)/config/dev-transactor.properties
	./datomic-pro-$(DATOMIC_VERSION)/bin/transactor config/dev-transactor.properties &


.PHONY: start
start:
	lein run &





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
