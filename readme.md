# Ambient Alarm Clock

<p align="center">
<img src="https://raw.githubusercontent.com/TVLukeProductions/AmbientAlarmClock/master/clock/src/main/res/drawable-mdpi/icon.png" alt="Logo"/>
</p>

The simple but effective alarm clock that plays you some music, offers you the news, turns on the light and more. Ambient Technology is the vision of technology that adapts your surroundings to your needs and offers services to you in better ways then ever before using sensing and acting technology.

Imagine your coffee machine turning on 5 minutes before your alarm clock rings so you always wake up to the smell of fresh coffee. Imagine music slowly swelling up while the lights simulate a sunrise. Imagine the heating in your bath turning on before you step into your bath and all of it controlled by your alarm time instead of at a fixed time. Using Home Automation technology from ezControl, the Philips Hue and other technologies the Ambient Alarm Clock makes these scenarios possible.

You can [Download the Ambient Alarm Clock here](https://www.dropbox.com/s/b8irps6uhls48hv/AAC11.apk).

<p align="center">
<img src="https://raw.githubusercontent.com/TVLukeProductions/AmbientAlarmClock/master/pics/overviewnew.jpg" alt="UI"/>
</p>

Features:
#### Alarm Clock
* Alarm Clock that wakes you up with music of your choice.
* You can chose what songs you want to wake up to by selecting a folder on your device with music (or use DropBox). The alarm clock will select one song out of the selected folder at random.
* You can have the song fade in slowly or start loud.
* Define your own snooze time or if you even want a snooze button shown at all.
* Choose a set of actions for each alarm which are then performed by the clock. Actions can include control over light, music or home appliances as well as data shown on the device.
* Each action can be activated individually on a chosen time before or after the alarm.

#### Website on wake up
* Define a web site to be shown on your device on alert (for example a news web site).

#### Countdown on wake up
* You don't have all the time in the world. You need to get the bus? You always want to know how much time is left until you need to get out? Activate the Countdown, which will start with the first alarm and tell you when you have to got. The alarm-clock can, if you want it to, stop the alert when it has run out, so you don't even have to be there any more.

#### Reminder to go to bed
* Get a reminder to go to bed several hours before the alarm rings (you can define how many hours)
* Get the reminder send to you via email.

#### Radio Player
* Listen to the radio after the alarm has rung. You have the choice between several radio stations

#### DropBox Integration
* Use either a local folder or define a DropBox folder to synchronize your wake up songs. You can chose what songs to wake up to by just putting it into the folder.

#### Last FM Integration
* Scrobble the song you woke up to on [last.fm](http://www.last.fm)

#### Philips Hue Integration
* Turn on the lights when its time to wake up with the Philips Hue that can be connected with almost no effort
* Choose the color of light to wake you up (not yet implemented)

#### Home Automation
* Control home appliances like your coffee machine or the heating with the EZControl Home automation server

## FAQ
**What the Hell? There are like a billion alarm clock apps out there!**

Yes, but I don't know of any clock that does all the stuff this one does. I have a hard time getting up so I want loud music, bright lights
and my coffee to be ready. It is basically designed around my needs and I just put it here in case anybody wants to have this stuff too.
You can just download the APK or get the code right here and make it even better.

**Will there be more features?**

Yes. Whenever I make the app better to make sure I get up better I get used to the new feature after one or two weeks and I need to make the clock even better, even more tricky, even more effective. So, new features will come.

## Version History
#### Current

**AmbientAlarmClock 2.11** (June 1st 2014)
* Completely rewritten code base
* Now supports multiple independent ambient alarms
* Users can configure which alarms should be used on which days
* Users can configure actions to be performed for each alarm
* Actions include all previously available actions like turning on lights, switching power plugs, playing music with syncing DropBox folders and muc more
* Faster, nicer and more responsive UI

Change Log:
* Fixed an issue with using IntentService with a high frequency of calls, now uses a handler
* Fixed excessive logging, now uses own Logger-class which als can write into file for long term debugging efforts
* Fixed an issue with DropBox Sync that could drain data plans
* removed some unreachable code
* Fixed some error handling in the database handler

#### Older Versions

**AmbientAlarmClock 2.6.0.1** (May 1st 2014)
* Completely rewritten code base
* Now Supports multiple independent ambient alarms
* Configure which alarms should be used on which days
* Configure actions to be performed for each alarm
* Actions include all previously available actions like turning on lights, switching power plugs, playing music with syncing DropBox folders and muc more
* Faster, nicer and more responsive UI

**AmbientAlarmClock 1.5.1.9** (March 1st 2014)
* Set Your Alarm Time
* Set A Reminder to be displayed on the device and send via email
* Choose a folder for music to play
* If no folder is selected or no music is on the device a default song is now included
* Choose from 4 different radio stations to play either during alarm or after
* Shows you a web site on wake up (best for tablets)
* Can show you a countdown starting from the alarm time, to motivate you to get up quickly
* Last.fm integration
* DropBox integration
* Philips Hue integration
* Prototypical EZcontrol Home Server integration

Change Log:
* Fixed issue with [DLF](http://www.deutschlandradio.de/) Streaming
* Fixed Autorestart after Boote Issue
* Included A countdown functionality


**AmbientAlarmClock 1.5.1.8** (Jan. 4th 2014)

* Set Your Alarm Time
* Set A "Go-To-Bed"-Reminder to be displayed on the device and send via email
* Choose a folder for music to play on Alarm
* If no folder is selected or no music is on the device a default song is now included
* Choose from 4 different radio stations to play either during alarm or after
* Shows you a web site on wake up (best for tablets)
* Last.fm integration
* DropBox integration (You can simply put the music you want to wake up to in a folder, the clock automatically synchronizes it)
* Philips Hue integration (The app can turn on the lights on alarm time)
* Prototypical EZcontrol Home Server integration (Control heating or power plugs depending on alrm time)

Change Log:
* Included default song
* Fixed a crashing issue (critical)
* Fixed Folder finding during alarm (critical)

**AmbientAlarmClock 1.5.1.7**

* Set Your Alarm Time
* Set A Reminder to be displayed on the device and send via email
* Choose a folder for music to play
* Choose from 4 different radio stations to play either during alarm or after
* Shows you a web site on wake up (best for tablets)
* Last.fm integration
* DropBox integration
* Philips Hue integration
* Prototypical EZcontrol Home Server integration

#License

<p align="center"><a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">
<img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" /></a></p>

<span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">Ambient Alarm Clock</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/TVLuke/AmbientAlarmClock/" property="cc:attributionName" rel="cc:attributionURL">Lukas Ruge</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License</a>.<br />Permissions beyond the scope of this license may be available at <a xmlns:cc="http://creativecommons.org/ns#" href="http://www.lukeslog.de/" rel="cc:morePermissions">http://www.lukeslog.de/</a>.
