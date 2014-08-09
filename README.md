NovelDroid
==========
An android app for extracting novel contents from forum posts

##Latest Version
1.4.1

##Changelog
###1.4.2
* added bookmark action icons from [Android-Action-Bar-Icons](https://github.com/svenkapudija/Android-Action-Bar-Icons/)
* introduced jsoup to parse html

###1.4.1
* fix: cklol html structure changes
* acquire wakelock during post-processing
* introduced [android-process-button](https://github.com/dmytrodanylyk/android-process-button)
* substituted DirectoryChooserDialog with [ExFilePicker](https://github.com/bartwell/ExFilePicker)

###1.4.0
* change structure of downloaders
* support q1d1an, z0ngheng, l7k, chuangsh1
* implement simple and faster string replacement
* fix cklol analyzer
* update mechanism

###1.3.1
* change behaviour of up navigation to popping back stack in settings page
* change method of dynamic class creating to Class.forName() with class name array

###1.3.0
* minor bug fix: when directory chooser dialog popup, if download folder is not existed then it'll be created.
* adopt ActionBarCompat (android support library v7)
* adopt Navigation Drawer pattern to get rid of dependency on slidingmenu library
* adapt [PreferenceFragmentCompat](http://www.michenux.net/android-preferencefragmentcompat-906.html) as a more general solution to compatibility issue
* hide keyboard when user touches elsewhere than text field

##Supporting Sites
* cklol
* 3yny
* q1d1an
* z0ngheng
* l7k
* chuangsh1

##Features
1. Strong Analyzer: parse book name and author almost perfectly
2. Settings: encoding, filename, output folder
3. Search: with google custom search
4. UI: with ActionBar and Navigation Drawer

##License
* [Android-Action-Bar-Icons](https://github.com/svenkapudija/Android-Action-Bar-Icons/) - Font Awesome
	```
	Font Awesome is fully open source and is GPL friendly.
	You can use it for commercial projects, open source projects,
	or really just about whatever you want.

	You may obtain a copy of the License at
	http://fortawesome.github.io/Font-Awesome/license/
	```
* [android-process-button](https://github.com/dmytrodanylyk/android-process-button/)
	```
	The MIT License (MIT)

	Copyright (c) 2014 Danylyk Dmytro

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
	```
* [ExFilePicker](https://github.com/bartwell/ExFilePicker/)
	```
	The MIT License (MIT)

	Copyright (c) 2013 Artem Bazhanov

	Permission is hereby granted, free of charge, to any person obtaining a copy of
	this software and associated documentation files (the "Software"), to deal in
	the Software without restriction, including without limitation the rights to
	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
	the Software, and to permit persons to whom the Software is furnished to do so,
	subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	```
* [jsoup](http://jsoup.org/)
	```
	The MIT License

	Copyright © 2009 - 2013 Jonathan Hedley (jonathan@hedley.net)

	Permission is hereby granted, free of charge, to any person obtaining a copy of
	this software and associated documentation files (the "Software"), to deal in
	the Software without restriction, including without limitation the rights to
	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
	the Software, and to permit persons to whom the Software is furnished to do so,
	subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	```

##Credits
* [CSNovelCrawler](https://github.com/rngmontoli/CSNovelCrawler/)
* [JComicDownloader](https://sites.google.com/site/jcomicdownloader/)
* [JNovelDownloader](https://github.com/pupuliao/JNovelDownloader/)
