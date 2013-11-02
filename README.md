NovelDroid
==========
An android app for extracting novel contents from forum posts

##Latest Version
1.3.1

##Changelog
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

##Features
1. Strong Analyzer: parse book name and author almost perfectly
2. Settings: encoding, filename, output folder
3. Search: with google custom search
4. UI: with ActionBar and Navigation Drawer

##License
This project is under GNU GPL v3.

	NovelDroid, an android app for extracting novel contents from forum posts.
    Copyright (C) 2013  sh1r0

    This project is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This project is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this project.  If not, see <http://www.gnu.org/licenses/>.

    This project is based on JNovelDownloader project.
    # Copyright (c) 2012 Liao, Chong-Po (pupuliao@gmail.com)
	#
	# This file is free software: you may copy, redistribute and/or modify it
	# under the terms of the GNU General Public License version 3 as published
	# by the Free Software Foundation.
	#
	# This file is distributed in the hope that it will be useful, but
	# WITHOUT ANY WARRANTY; without even the implied warranty of
	# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	# General Public License for more details.
	#
	# You should have received a copy of the GNU General Public License
	# along with this program. If not, see <www.gnu.org/licenses/>

##Credits
* [CSNovelCrawler](http://rngmontoli.blogspot.tw/2013/06/csnovelcrawler.html)
* [JComicDownloader](https://sites.google.com/site/jcomicdownloader/)
* [JNovelDownloader](http://www.pupuliao.info/jnoveldownloader-%E5%B0%8F%E8%AA%AA%E4%B8%8B%E8%BC%89%E5%99%A8/)
