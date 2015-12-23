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

BgPlayer.prototype.isPlaying =function(){
	return this._isPlaying;
}

BgPlayer.prototype._playingMediaUrl ='';

BgPlayer.prototype.getPlayingMediaUrl=function(){
	return this._playingMediaUrl;
}

BgPlayer.prototype.play = function(mediaURL,options) {
	if(this.isPlaying()){
		if(this.getPlayingMediaUrl() != mediaURL){
			this.stop();			
			this._playInternal(mediaURL,options,null,null);
		}
	}else
		this._playInternal(mediaURL,options);    
}

BgPlayer.prototype._playInternal = function(mediaURL,options){
	cordova.exec(null,null, 'BgPlayer', 'play', [mediaURL,options]);
	this._isPlaying = true;
    this._playingMediaUrl = mediaURL;
}

BgPlayer.prototype.stop = function() {
    cordova.exec(null, null, 'BgPlayer', 'stop', []);
	this._isPlaying =false;
	this._playingMediaUrl = '';
}

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
