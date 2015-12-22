function BgPlayer() {
}

BgPlayer.prototype._isPlaying =false;
BgPlayer.prototype._playingMediaUrl ='';

BgPlayer.prototype.isPlaying =function(){
	return this._isPlaying;
}

BgPlayer.prototype.play = function(mediaURL,options,successCallback, errorCallback) {
	cordova.exec(function(){
				this._isPlaying =true;
				this._playingMediaUrl = mediaURL;
				if(successCallback)
					successCallback();
			}, errorCallback, "BgPlayer", "play",[mediaURL,options]);
	/*
	if(this.isPlaying()){
		if(this._playingMediaUrl != mediaURL)
			this.stop(function(){ this._playInternal(mediaURL,options,successCallback,errorCallback); },null);
	}else{
		this._playInternal(mediaURL,options,successCallback,errorCallback);
	}*/
}

BgPlayer.prototype._playInternal = function(mediaURL,options,successCallback, errorCallback){
	
}

BgPlayer.prototype.stop = function(successCallback, errorCallback) {
	cordova.exec(function(){
		this._isPlaying =false;
		this._playingMediaUrl = '';
		if(successCallback)
			successCallback();
	}, errorCallback, "BgPlayer", "stop",[]);
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
