all: search.html update

search.html: search.php
	rm -rf search.html
	php search.php > search.html
	chmod 444 search.html

FILES=\
	index.html \
	search.html \
	s6.css \
	search.js \
	searchclass.js \
	searchresult.js \
	searchtest.js \
	util.js \
	checkitem.php \
	checkserver.php \
	checktest.php \
	dosearch.php \
	s6common.php \
	search.php \
	showlicense.php \
	
update:
	scp $(FILES) conifer:/vol/web/html/s6web

