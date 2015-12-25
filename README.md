# bg-player
This cordova plugin allows you to play background audio. Media player plays audio or (audio only) video files even your application goes to background.

This plugin creates __window.plugins.BgPlayer__ object to use these functions.

__Warning__ This plugin uses [Cordova Vitamio Plugin][vitamio]s player to play media for some advantages. But you can easily change with removing import statement at [PlayerService.java][playerservice].

## Functions

### BgPlayer.play(mediaURL, options)

plays the given file. This comman also create a notification. If service already playing a different file, firstly stop service and recreate to play.

### BgPlayer.stop()

stops service and remove the notification. 

### BgPlayer.isPlaying()

returns is any file playing.

### BgPlayer.getPlayingMediaUrl()

returns actively playing file path.

## EVENTS

### BgPlayer.onplay

fires when call play method

### BgPlayer.onStartPlaying

fires when satart playing media

### BgPlayer.onstop

fires when call stop method.

### BgPlayer.onfailure(error)

fires when goes some thing wrong. media url is not correct, no internet access, unspoorted codec etc.

## Example

```javascript
window.plugins.BgPlayer.onplay = function () {
    console.log("loading....");
}

window.plugins.BgPlayer.onStartPlaying = function () {
    console.log("now playing la la la...");
}

window.plugins.BgPlayer.onstop = function () {
    console.log("that is all folks. go home now.");
}

window.plugins.BgPlayer.onfailure = function (error) {
    console.log("oh no no no. error is " + error);
}

if (window.plugins.BgPlayer.isPlaying()){
  var playingMedia = window.plugins.BgPlayer.getPlayingMediaUrl();
    console.log("yes i am the player. player is play this file: " + playingMedia);
}
else
    window.plugins.BgPlayer.play("http://www.www.com/www.mp3", {
        "title": "I am the title",
        "text": "So I am the text too"
    });
    
setTimeout(function () {
      // I will listen yout only 5 seconds, sorry
      window.plugins.BgPlayer.stop();
  }, 50000);

```
[vitamio]:https://github.com/nchutchind/Vitamio-Cordova-Plugin
[playerservice]: src/com/ademc/plugins/PlayerService.java
