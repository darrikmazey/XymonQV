
first: release

clean:
	ant clean

release: sign_release

sign_release: ant_release
	mkdir -p bin/release
	cp bin/XymonQV-release-unsigned.apk bin/release/XymonQV.unsigned.apk
	jarsigner -keystore ../keys/release.keystore bin/release/XymonQV.unsigned.apk darmasoft.release
	/home/darrik/android_sdk/tools/zipalign -v 4 bin/release/XymonQV.unsigned.apk bin/release/XymonQV.apk

ant_release: FORCE
	ant release

debug: sign_debug

sign_debug: ant_debug
	mkdir -p bin/debug
	cp bin/XymonQV-debug-unaligned.apk bin/debug/XymonQV.unsigned.apk
	jarsigner -keystore ../keys/release.keystore bin/debug/XymonQV.unsigned.apk darmasoft.release
	/home/darrik/android_sdk/tools/zipalign -v 4 bin/debug/XymonQV.unsigned.apk bin/debug/XymonQV.apk

ant_debug: FORCE
	ant debug

FORCE:
