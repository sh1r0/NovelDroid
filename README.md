NovelDroid
==========
An android app for extracting novel contents from forum posts

##Latest Version
1.4.1

##Changelog

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

##Libraries
* [android-process-button](https://github.com/dmytrodanylyk/android-process-button/)
* [ExFilePicker](https://github.com/bartwell/ExFilePicker/)
* [jsoup](http://jsoup.org/)

##Credits
* [CSNovelCrawler](https://github.com/rngmontoli/CSNovelCrawler/)
* [JComicDownloader](https://sites.google.com/site/jcomicdownloader/)
* [JNovelDownloader](https://github.com/pupuliao/JNovelDownloader/)
