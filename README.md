### About

A project by [Emiliano Burgos][1] and [Genevieve Hoffman][2], conceived with [Scott Wayne Indiana][3], and [Rune Madsen][4].

We have all seen satellite images of planet Earth at night, but aside from expressing that cities are bright, or that nighttime power is primarily a first-world commodity, these images do not portray much practical information. The Darkness Map aims to communicate dark and light levels within the nighttime urban environment on a human scale.

Inspired by a desire to find dark areas (in order to determine the best locations for outdoor projection) in a city dominated by lighting for commerce, entertainment and public safety, the Darkness Map was conceived of to visualize the nighttime light levels on New York City's sidewalks. Data is collected by capturing video of each city block, tagging it with a GPS location, then analyzing it algorithmically to compute the average brightness of each frame. This data is displayed as a map visualization that depicts the lightness and darkness of city streets at night. Beta versions of the Darkness Map are active in New York City and the San Francisco Bay Area.

Possible extensions of the Darkness map could be data mashing to see whether light levels in the city at night correlate with other location-specific data, like public safety records, traffic incidents, or unauthorized interventions in public space (graffiti).

The Darkness Map was part of the San Francisco [Urban Prototyping Festival][5] in October 2012, and is made possible in part by the [Gray Area Foundation for the Arts][6].

 [1]: http://www.goliatone.com
 [2]: http://www.genevievehoffman.com
 [3]: http://www.39forks.com
 [4]: http://runemadsen.com
 [5]: http://sf.urbanprototyping.org
 [6]: http://www.gaffta.org
 
* * *

### Legal

The Darkness Map is an open-source project that uses crowd-sourced data to visualize the urban environment at night. The Darkness Map app provides a way for users to contribute to the Darkness Map visualization. The Darkness Map app does not maintain any personal information about users. The data users collect and send to our servers consists of a light level reading, timestamp, GPS coordinate, and session ID. There is no way to trace back to an individual or phone. By using the Darkness Map app users are contributing to an open-source project, and the data collected will be added to the Darkness Map database, and made available as an open source dataset.

* * *

### Open Source

All code for the Darkness Map is open source and available to use under an MIT License. All of the code components of the Darkness Map are hosted on [github][1].   
Darkness Map code repositories include:

Mobile app for Android - native Android/Java. The mobile app uses the phone camera preview as a light sensor. After receiving a GPS signal, the app captures the brightness of each frame, and sends this data to the Darkness Map server. The iPhone app is currently in development stage, and has not yet been submitted to the App Store. Future versions will have variable sampling rates -- for walking, biking and driving.

[Mobile app for iPhone][2] - made with OpenFrameworks for iPhone and Objective-C. The mobile app uses the phone camera preview as a light sensor. After receiving a GPS signal, the app captures the brightness of each frame, and sends this data to the Darkness Map server. The Android app is currently a prototype, and available to download from the Darkness Map website if you navigate to the homepage on an Android device. Future versions will have variable sampling rates -- for walking, biking and driving.

[Data Collection Device][3] - basic electronics tutorial for building your own data logger. The Data Collection Device was the initial prototype for the Darkness Map, before we decided to make a mobile app as the data collection tool. These instructions go over how to connect an Arduino, GPS module, and light sensor, and how to save this data to an SD card. Currently, there is no way to integrate the data collected with the Darkness Map web-based visualization, but we are hoping to develop an upload system in the future. Currently, you can use make your own visualizations from the data saved to the CSV file, and I will be uploading code to demonstrate how to get started with that soon. Hopefully you can use this tutorial as the basis for other projects, and swap out the light sensor for other environmental sensors.

[Arduino Data Collection Device][4] - made with Arduino. Repository for Arduino code that works with the Data Collection Device. In addition to a few core Arduino libraries (Wire, SD, and Software Serial), the code uses the [TinyGPS Library][5], by Mikal Hart, the [TSL2561 Library][6], by Ladyada (Limor Fried), and the [RTClib][7], by Ladyada. The RTClib was originally developed by [Jeelabs][8]. Before using the code, download the additional libraries and drop them into the libraries folder inside your Arduino projects folder. For best results, follow the steps in the Data Collection Device tutorial when using this code.

[Dataviz Projection][9] - made with Processing. This is a short sketch that pulls data from the [Darkness Map API][10] and visualizes the brightness values as animating grayscale bars. The code was developed for a projection installation at the Urban Prototyping Festival in San Francisco, which was displayed at the entrance to the Intersection for the Arts on Mission Street.

[Website][11] - made with HTML, CSS and Javascript. Code for the Darkness Map website and map based visualization. The map visualization utilizes the [Leaflet][12] library for JS, and incorporates map tiles made with data from the <a href="http://www.openstreetmap.org"OpenStreetMap</a> project, and styled by [TileMill][13].

[Server][14] - made with NodeJS. Code for the Darkness Map Server, which hosts the data collected by the Darkness Map mobile app, and powers the map visualization.

[Client][15] - made with Javascript.

All code for the Darkness Map is open source and available to use under an MIT License.

Copyright (c) 2012-13 Emiliano Burgos and Genevieve Hoffman.  
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 [1]: https://github.com/darknessmap
 [2]: https://github.com/darknessmap/app-iphone
 [3]: https://github.com/darknessmap/data-collection-device
 [4]: https://github.com/darknessmap/arduino-data-collection-device
 [5]: http://arduiniana.org/libraries/tinygps/
 [6]: https://github.com/adafruit/TSL2561-Arduino-Library
 [7]: https://github.com/adafruit/RTClib
 [8]: https://github.com/jcw/rtclib
 [9]: https://github.com/darknessmap/dataviz-projection
 [10]: http://178.79.145.84:8080/api/darkness
 [11]: https://github.com/darknessmap/website
 [12]: http://leafletjs.com
 [13]: http://mapbox.com/tilemill/
 [14]: https://github.com/darknessmap/server
 [15]: https://github.com/darknessmap/client
