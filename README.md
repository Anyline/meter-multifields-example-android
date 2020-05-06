# meter-multifields-example-android
How to use the Multifields Meter scanner - Android Example App 


## Overview

This repository contains a full Android Studio project which will scan multivalue meters using the Anyline Android SDK and show the results in a result screen.


## Requirements

- Android Studio
- An Android device to run the app


## Quick Start

Clone or download this repository. 
Open Android Studio and run the project on a real smartphone.

Once the app starts scan the barcode of a Multivalue meter.

Then scan the meter values, the app will run continuously collecting results. It will stop and return a result once one of the following conditions are met:

1) All changing meter values were read (one full circle with the first counter number appearing again)
2) The scanning process has been running for 20 seconds without returning a result
3) The user presses a “Stop scan” button

The app is preconfigured to accept values for counters ("Zählwerke") 161, 162, 180, 181


## License

This app comes with a demo license. 
To claim your own trial license, go to: [Anyline SDK Register Form]( https://anyline.com/free-demos/ ) or get in contact with our sales representative.


## Using the Android Anyline SDK

Here you can find all the necessary information to get your app up and running with the Anyline Android SDK: https://documentation.anyline.com/toc/platforms/android/index.html


## API Reference

The API reference for the Anyline SDK for Android can be found here: https://documentation.anyline.com/api/android/index.html



