
fast-tests:
    # connectedAndroidTests are soooo slow, and the output sucks.
	./gradlew :eyepatch:installDebugAndroidTest
	adb shell am instrument -w com.tdrhq.eyepatch.test/com.tdrhq.eyepatch.TestRunner
