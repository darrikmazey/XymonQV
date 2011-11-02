
first: release

clean:
	ant clean

release: sign_release

sign_release: ant_release
	/bin/cp bin/XymonQV-release.apk bin/XymonQV-release-`git curtag | sed -e "s/v//"`.apk

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
