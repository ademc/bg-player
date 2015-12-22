function BgPlayer() {
}

BgPlayer.prototype._isPlaying =false;
BgPlayer.prototype._playingMediaUrl ='';

BgPlayer.prototype.isPlaying =function(mediaUrl){
	return this._playingMediaUrl == mediaUrl;
}

BgPlayer.prototype.play = function(mediaURL,options,successCallback, errorCallback) {
	if(this.isPlaying(mediaURL) == false)
		this.stop(function(){
			cordova.exec(function(){
				this._isPlaying =true;
				this._playingMediaUrl = mediaURL;
				successCallback();
			}, errorCallback, "BgPlayer", "play",[mediaURL,options]);
		});
}

BgPlayer.prototype.stop = function(successCallback, errorCallback) {
	cordova.exec(function(){
		this._isPlaying =false;
		this._playingMediaUrl = '';
		successCallback();
	}, errorCallback, "BgPlayer", "stop",[]);
}

BgPlayer.prototype.onStartPlaying=function(){}

BgPlayer.prototype.onfailure=function(error){}

BgPlayer.prototype.onstart=function(){}

BgPlayer.prototype.onstop=function(){}

BgPlayer.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }

  window.plugins.BgPlayer = new BgPlayer();
  return window.plugins.BgPlayer;
};

cordova.addConstructor(BgPlayer.install);