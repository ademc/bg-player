package com.ademc.plugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class BgPlayer extends CordovaPlugin {

	// Event types for callbacks
	public enum PluginEvent {
		PLAY, PLAYING, STOP, FAILURE
	}

	public static final String ACTION_PLAY = "play";
	public static final String ACTION_START_PLAYING = "StartPlaying";
	public static final String ACTION_STOP = "stop";
	public static final String ACTION_FAILURE = "failure";

	// Plugin namespace
	private static final String JS_NAMESPACE = "window.plugins.BgPlayer";

	// Flag indicates if the service is bind
	private boolean isBind = false;

	// Used to (un)bind the service to with the activity
	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			// Nothing to do here
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// Nothing to do here
		}
	};

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		mCordova = cordova;
		mInstance = this;

		/*if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(cordova.getActivity())){
			fireJsEvent(PluginEvent.FAILURE, "Player hazırlanamadı.");
			return;
		}*/
	}

	private static BgPlayer mInstance = null;
	private static CordovaInterface mCordova = null;

	public static CordovaInterface getCordova() {
		return mCordova;
	}

	public static BgPlayer getInstance() {
		return mInstance;
	}

	// Default settings for the notification
	private static JSONObject mSettings = new JSONObject();

	public static JSONObject getSettings() {
		return mSettings;
	}

	private static String mMediaPath = null;

	public static String getMediaPath() {
		return mMediaPath;
	}

	/**
	 * Executes the request.
	 * 
	 * @param action
	 *            The action to execute.
	 * @param args
	 *            The exec() arguments.
	 * @param callback
	 *            The callback context used when calling back into JavaScript.
	 * 
	 * @return Returning false results in a "MethodNotFound" error.
	 * 
	 * @throws JSONException
	 */
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callback) throws JSONException {

		if (action.equalsIgnoreCase(ACTION_PLAY)) {
			mMediaPath = args.getString(0);
			mSettings = args.getJSONObject(1);

			startService();

			return true;
		}

		if (action.equalsIgnoreCase(ACTION_STOP)) {
			stopService();
			return true;
		}

		return false;
	}

	/**
	 * Called when the activity will be destroyed.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService();
	}

	/**
	 * Bind the activity to a background service and put them into foreground
	 * state.
	 */
	private void startService() {
		Activity context = cordova.getActivity();

		Intent intent = new Intent(context, PlayerService.class);

		if (isBind)
			return;

		try {
			context.bindService(intent, connection, Context.BIND_AUTO_CREATE);

			fireJsEvent(PluginEvent.PLAY, null);

			context.startService(intent);
		} catch (Exception e) {
			fireJsEvent(PluginEvent.FAILURE, e.getMessage());
		}

		isBind = true;
	}

	/**
	 * Bind the activity to a background service and put them into foreground
	 * state.
	 */
	private void stopService() {
		Activity context = cordova.getActivity();

		Intent intent = new Intent(context, PlayerService.class);

		if (!isBind)
			return;

		fireJsEvent(PluginEvent.STOP, null);

		context.unbindService(connection);
		context.stopService(intent);

		isBind = false;
	}

	/**
	 * Fire vent with some parameters inside the web view.
	 * 
	 * @param event
	 *            The name of the event
	 * @param params
	 *            Optional arguments for the event
	 */
	public void fireJsEvent(PluginEvent event, String params) {
		String eventName;

		// if (event != PluginEvent.FAILURE)
		// return;

		switch (event) {
		case PLAY:
			eventName = ACTION_PLAY;
			break;
		case STOP:
			eventName = ACTION_STOP;
			break;
		case PLAYING:
			eventName = ACTION_START_PLAYING;
			break;
		default:
			eventName = ACTION_FAILURE;
		}

		final String fn = String.format("setTimeout('%s.on%s(%s)',0);",
				JS_NAMESPACE, eventName, params);

		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				webView.loadUrl("javascript:" + fn);
			}
		});
	}
}
