Changelog
======

## 1.5.0
* Several fixes for API changes
* Switch the directory picker
* New about page
* Material UI
* Removed Parse components
* Switch to GitHub releases as update source

## 1.4.2
* added bookmark action icons from [Android-Action-Bar-Icons](https://github.com/svenkapudija/Android-Action-Bar-Icons/)
* introduced jsoup to parse html
* updated zh-rTW translations

## 1.4.1
* fix: cklol html structure changes
* acquire wakelock during post-processing
* introduced [android-process-button](https://github.com/dmytrodanylyk/android-process-button)
* substituted DirectoryChooserDialog with [ExFilePicker](https://github.com/bartwell/ExFilePicker)

## 1.4.0
* change structure of downloaders
* support q1d1an, z0ngheng, l7k, chuangsh1
* implement simple and faster string replacement
* fix cklol analyzer
* update mechanism

## 1.3.1
* change behaviour of up navigation to popping back stack in settings page
* change method of dynamic class creating to Class.forName() with class name array

## 1.3.0
* minor bug fix: when directory chooser dialog popup, if download folder is not existed then it'll be created.
* adopt ActionBarCompat (android support library v7)
* adopt Navigation Drawer pattern to get rid of dependency on slidingmenu library
* adapt [PreferenceFragmentCompat](http://www.michenux.net/android-preferencefragmentcompat-906.html) as a more general solution to compatibility issue
* hide keyboard when user touches elsewhere than text field
