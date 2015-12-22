var exec    = require('cordova/exec'),
    channel = require('cordova/channel');

document.addEventListener('backbutton', function () {}, false);


channel.onCordovaReady.subscribe(function () {

    channel.onCordovaInfoReady.subscribe(function () {
    });

});

function BgPlayer() {
}

BgPlayer.prototype._isPlaying =false;
BgPlayer.prototype._playingMediaUrl ='';

BgPlayer.prototype.isPlaying =function(){
	return this._isPlaying;
}

BgPlayer.prototype.play = function(mediaURL,options) {
	this._isPlaying = true;
    cordova.exec(null, null, 'BgPlayer', 'play', [mediaURL,options]);
}

BgPlayer.prototype.stop = function() {
	this._isPlaying = false;
    cordova.exec(null, null, 'BgPlayer', 'stop', []);
}

BgPlayer.prototype.onactivate = function () {};
BgPlayer.prototype.ondeactivate = function () {};
BgPlayer.prototype.onStartPlaying=function(){}

BgPlayer.prototype.onfailure=function(error){}

BgPlayer.prototype.onplay=function(){}

BgPlayer.prototype.onstop=function(){}

BgPlayer.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }

  window.plugins.BgPlayer = new BgPlayer();
  return window.plugins.BgPlayer;
};

cordova.addConstructor(BgPlayer.install);
