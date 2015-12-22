package com.ademc.plugin.bgplayer;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
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

		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(cordova.getActivity()))
			return;
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

		Intent intent = new Intent(context, ForegroundService.class);

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

		Intent intent = new Intent(context, ForegroundService.class);

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

//		if (event != PluginEvent.FAILURE)
	//		return;

		switch (event) {
		case PLAY:
			eventName = ACTION_PLAY;
			break;
		case STOP:
			eventName = ACTION_STOP;
			break;
		case PLAYING:
			eventName = ACTION_START_PLAYING;
		default:
			eventName = ACTION_FAILURE;
		}

		String fn = String.format("setTimeout('%s.on%s(%s)',0);", JS_NAMESPACE,
				eventName, params);

		final String js = flag + fn;

		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				webView.loadUrl("javascript:" + js);
			}
		});
	}

	public class ForegroundService extends Service implements
			OnPreparedListener {

		// Fixed ID for the 'foreground' notification
		private static final int NOTIFICATION_ID = -574543954;

		// Scheduler to exec periodic tasks
		final Timer scheduler = new Timer();

		// Used to keep the app alive
		TimerTask keepAliveTask;

		private MediaPlayer mMediaPlayer;

		/**
		 * Allow clients to call on to the service.
		 */
		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		/**
		 * Put the service in a foreground state to prevent app from being
		 * killed by the OS.
		 */
		@Override
		public void onCreate() {
			super.onCreate();
			keepAwake();
			startPlay();
		}

		@Override
		public void onDestroy() {
			sleepWell();
			stopForeground(true);
			if (mMediaPlayer != null) {
				mMediaPlayer.release();
				mMediaPlayer = null;
			}

			super.onDestroy();

		}

		/**
		 * Put the service in a foreground state to prevent app from being
		 * killed by the OS.
		 */
		public void keepAwake() {
			final Handler handler = new Handler();
			startForeground(NOTIFICATION_ID, makeNotification());

			keepAliveTask = new TimerTask() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						@Override
						public void run() {
							// Nothing to do here
							// Log.d("BgPlayer", "" + new Date().getTime());
						}
					});
				}
			};

			scheduler.schedule(keepAliveTask, 0, 1000);
		}

		private void startPlay() {
			final ForegroundService ths = this;
			BgPlayer.getCordova().getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						mMediaPlayer = new MediaPlayer(BgPlayer.getCordova()
								.getActivity());
						mMediaPlayer.setVolume(100, 100);
						mMediaPlayer.setDataSource(BgPlayer.getMediaPath());
						mMediaPlayer.setOnPreparedListener(ths);
						mMediaPlayer.prepareAsync();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			});
		}

		public void onPrepared(MediaPlayer mediaplayer) {
			if (mMediaPlayer != null) {
				mMediaPlayer.start();
				BgPlayer.getInstance().fireJsEvent(PluginEvent.PLAYING, null);
			}
		}

		/**
		 * Stop background mode.
		 */
		private void sleepWell() {
			stopForeground(true);
			keepAliveTask.cancel();
		}

		/**
		 * Create a notification as the visible part to be able to put the
		 * service in a foreground state.
		 * 
		 * @return A local ongoing notification which pending intent is bound to
		 *         the main activity.
		 */
		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		private Notification makeNotification() {
			JSONObject settings = BgPlayer.getSettings();
			Context context = getApplicationContext();
			String pkgName = context.getPackageName();
			Intent intent = context.getPackageManager()
					.getLaunchIntentForPackage(pkgName);

			Notification.Builder notification = new Notification.Builder(
					context).setContentTitle(settings.optString("title", ""))
					.setContentText(settings.optString("text", ""))
					// .setTicker(settings.optString("ticker", ""))
					.setOngoing(true).setSmallIcon(getIconResId());

			if (intent != null) {

				PendingIntent contentIntent = PendingIntent.getActivity(
						context, NOTIFICATION_ID, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);

				notification.setContentIntent(contentIntent);
			}

			if (Build.VERSION.SDK_INT < 16) {
				// Build notification for HoneyComb to ICS
				return notification.getNotification();
			} else {
				// Notification for Jellybean and above
				return notification.build();
			}
		}

		/**
		 * Retrieves the resource ID of the app icon.
		 * 
		 * @return The resource ID of the app icon
		 */
		private int getIconResId() {
			Context context = getApplicationContext();
			Resources res = context.getResources();
			String pkgName = context.getPackageName();

			int resId;
			resId = res.getIdentifier("icon", "drawable", pkgName);

			return resId;
		}

	}

}
