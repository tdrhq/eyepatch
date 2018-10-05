
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

integration-tests:
	./gradlew :integration:connectedAndroidTest

clean:
	./gradlew :clean :eyepatch:clean :runner:clean :iface:clean

core-tests:
	./gradlew :eyepatch:connectedAndroidTest :eyepatch:test

device-tests: |	env core-tests runner-tests integration-tests

runner-jvm-tests:
	./gradlew :eyepatch:test

jvm-tests: iface-tests runner-jvm-tests
env:
	env | grep -v GPG_AGENT
	adb shell pm list instrumentation

jenkins: jvm-tests device-tests
