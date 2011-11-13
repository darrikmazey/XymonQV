
first: release

clean:
	ant clean

release: sign_release

sign_release: ant_release
	/bin/cp bin/XymonQV-release.apk bin/XymonQV-release-`git curtag | sed -e "s/v//"`.apk

ant_release: FORCE
	ant release

debug: ant_debug

ant_debug: FORCE
	ant debug

FORCE:
