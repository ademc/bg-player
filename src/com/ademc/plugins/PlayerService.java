package com.ademc.plugins;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.ademc.plugins.BgPlayer.PluginEvent;

public class PlayerService extends Service implements OnPreparedListener {

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
	 * Put the service in a foreground state to prevent app from being killed by
	 * the OS.
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
	 * Put the service in a foreground state to prevent app from being killed by
	 * the OS.
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
		final PlayerService ths = this;
		if(BgPlayer.getCordova().getThreadPool() == null){
			BgPlayer.getInstance().fireJsEvent(PluginEvent.FAILURE, "Player hazırlanamadı.");
			return;
		}
		BgPlayer.getCordova().getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				try {
					/*mMediaPlayer = new MediaPlayer(BgPlayer.getCordova()
							.getActivity());*/
					mMediaPlayer = new MediaPlayer();
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
	 * Create a notification as the visible part to be able to put the service
	 * in a foreground state.
	 * 
	 * @return A local ongoing notification which pending intent is bound to the
	 *         main activity.
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private Notification makeNotification() {
		JSONObject settings = BgPlayer.getSettings();
		Context context = getApplicationContext();
		String pkgName = context.getPackageName();
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(
				pkgName);

		Notification.Builder notification = new Notification.Builder(context)
				.setContentTitle(settings.optString("title", ""))
				.setContentText(settings.optString("text", ""))
				// .setTicker(settings.optString("ticker", ""))
				.setOngoing(true).setSmallIcon(getIconResId());

		if (intent != null) {

			PendingIntent contentIntent = PendingIntent.getActivity(context,
					NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

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
