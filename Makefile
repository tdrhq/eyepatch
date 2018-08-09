
OUTPUT:=$(shell mktemp)
PATH:=$(ANDROID_SDK)/platform-tools:$(ANDROID_HOME)/platform-tools:$(PATH)
INST_ARGS?=""

fast-tests:
    # connectedAndroidTests are soooo slow, and the output sucks.
	./gradlew :eyepatch:installDebugAndroidTest
	PATH=$(PATH) adb shell am instrument   -w $(INST_ARGS) com.tdrhq.eyepatch.test/com.tdrhq.eyepatch.TestRunner | tee $(OUTPUT)
	tail -3 $(OUTPUT) | grep OK > /dev/null

class-builder-tests:
	INST_ARGS="-e class com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilderTest" $(MAKE) fast-tests

iface-tests:
	./gradlew :iface:test

runner-tests:
	./gradlew :runner:connectedAndroidTest

core-tests:
	./gradlew :eyepatch:connectedAndroidTest

jenkins: | iface-tests core-tests runner-tests
