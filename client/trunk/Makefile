ANT=ant

BUILD_OUT=${PWD}/build-output
DEPLOY_DIR=${PWD}/deploy
JAVA_ADD=-Djava.library.path=/usr/local/BerkeleyDB.4.6/lib/
JAVA_HEAP=1024M

LIB_MODULE_LIST=shared/libraries/db_col-4.6.21.jar
SHARED_PROJECTS_LIST=config messages utilities ModuleAPI
LIBRARY_FILES_LIST=bsh-classgen-2.0b4.jar bsh-classpath-2.0b4.jar bsh-commands-2.0b4.jar bsh-core-2.0b4.jar bsh-util-2.0b4.jar je-3.2.74.jar db-4.6.21.jar log4j-1.2.14.jar
VEYRON_PROJECTS_LIST=Veyron/IMS_Boot Veyron/IMS_Core Veyron/IMS_CCChannels Veyron/IMS_DNSCollect Veyron/IMS_DNSFlow Veyron/IMS_Reporting Veyron/IMS_NETCollect NEOFlow NABFlowV2

all: 
	@echo "Choose target: veyron, sieve"

prepare:
	${ANT} -version
	mkdir ${DEPLOY_DIR}

sieve: prepare
	@echo "Making Sieve"
	
	mkdir ${DEPLOY_DIR}/sieve
	cd middleware && ${ANT} clean jar >> ${BUILD_OUT}

	# copy core jars
	echo -n 'java -server -Xmx${JAVA_HEAP} -cp ./sieve/NabsReceptor.jar:' >> ${DEPLOY_DIR}/run_sieve.sh

	cp middleware/dist/*.jar ${DEPLOY_DIR}/sieve;
	for i in ${SHARED_PROJECTS_LIST}; do \
		for j in shared/$$i/dist/*.jar; do \
			cp $$j ${DEPLOY_DIR}/sieve; \
			echo -n "./sieve/`basename $$j`:" >> ${DEPLOY_DIR}/run_sieve.sh; \
		done; \
	done;
	for i in ${LIBRARY_FILES_LIST}; do \
		cp shared/libraries/$$i ${DEPLOY_DIR}/sieve; \
		echo -n "./sieve/$$i:" >> ${DEPLOY_DIR}/run_sieve.sh; \
	done;

	echo -n '$$CLASSPATH ${JAVA_ADD} $$1 eunomia.Main' >> ${DEPLOY_DIR}/run_sieve.sh
	chmod 755 ${DEPLOY_DIR}/run_sieve.sh

	# make and copy required modules.
	mkdir ${DEPLOY_DIR}/modules
	echo "Making streamStatus" >> ${BUILD_OUT}
	cd modules/streamStatus && ${ANT} clean jar >> ${BUILD_OUT}
	cp modules/streamStatus/dist/streamStatus.jar ${DEPLOY_DIR}/modules
	echo ./modules/streamStatus.jar >> ${DEPLOY_DIR}/modules.nab

libb:
	@echo "Copying Third-party modules"

	for i in ${LIB_MODULE_LIST}; do \
		cp $$i ${DEPLOY_DIR}/modules; \
		echo ./modules/`basename $$i` >> ${DEPLOY_DIR}/modules.nab; \
	done;

veyron: sieve libb
	@echo "Making Veyron Modules"

	mkdir ${DEPLOY_DIR}/veyron
	for i in ${VEYRON_PROJECTS_LIST}; do \
		echo "   Building Module: $$i"; \
		cd modules/$$i; \
		echo "Making $$i" >> ${BUILD_OUT}; \
		ant clean jar >> ${BUILD_OUT}; \
		cd -; \
		cp modules/$$i/dist/*.jar ${DEPLOY_DIR}/veyron; \
	done;

	cd ${DEPLOY_DIR} && for i in veyron/*.jar; do \
		echo ./$$i >> modules.nab; \
	done;

clean:
	rm -rf ${DEPLOY_DIR} ${BUILD_OUT}
