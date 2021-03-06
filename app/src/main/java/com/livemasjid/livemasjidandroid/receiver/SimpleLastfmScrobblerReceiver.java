/*
 * ServeStream: A HTTP stream browser/player for Android
 * Copyright 2012 William Seemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.livemasjid.livemasjidandroid.receiver;

import com.livemasjid.livemasjidandroid.R;
import com.livemasjid.livemasjidandroid.provider.Media;
import com.livemasjid.livemasjidandroid.service.MediaPlaybackService;
import com.livemasjid.livemasjidandroid.preference.PreferenceConstants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SimpleLastfmScrobblerReceiver extends BroadcastReceiver {
	private static final String BROADCAST_ACTION = "com.adam.aslfms.notify.playstatechanged";
	
	private static final int START = 0;
	private static final int RESUME = 1;
	private static final int PAUSE = 2;
	private static final int COMPLETE = 3;
	
	private static final String APP_NAME_NAME = "app-name";
	private static final String APP_PACKAGE_NAME = "app-package";
	private static final String STATE_NAME = "state";
	private static final String ARTIST_NAME = "artist";
	private static final String ALBUM_NAME = "album";
	private static final String TRACK_NAME = "track";
	private static final String DURATION_NAME = "duration";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action.equals(MediaPlaybackService.PLAYBACK_STARTED) ||
				action.equals(MediaPlaybackService.PLAYSTATE_CHANGED) ||
				action.equals(MediaPlaybackService.PLAYBACK_COMPLETE) ||
				action.equals(MediaPlaybackService.META_CHANGED)) {
		
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
			if (preferences.getBoolean(PreferenceConstants.SEND_SCROBBLER_INFO, false) &&
					!preferences.getBoolean(PreferenceConstants.SEND_BLUETOOTH_METADATA, true)) {
				if (metadataPresent(intent)) {
					sendBroadcast(context, intent);
				}
			}
		}
	}

	/**
	 * Checks if an intent has the minimum metadata necessary to scrobble.
	 * 
	 * @param intent The intent to validate.
	 * @return True if the intent has valid metadata, false otherwise.
	 */
	private boolean metadataPresent(Intent intent) {
		String artist = intent.getStringExtra("artist");
		String track = intent.getStringExtra("track");
		long duration = intent.getLongExtra("duration", 0);
		
		if (artist == null || artist.equals(Media.UNKNOWN_STRING)) {
			return false;
		}
		
		if (track == null || track.equals(Media.UNKNOWN_STRING)) {
			return false;
		}
		
		if (duration == -1) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Parses a broadcasted MediaPlaybackService intent and sends a broadcast to SLS.
	 * 
	 * @param context The current context.
	 * @param intent The received intent.
	 */
	private void sendBroadcast(Context context, Intent intent) {
		int state = RESUME;
		
		Intent i = new Intent(BROADCAST_ACTION);
		i.putExtra(APP_NAME_NAME, context.getString(R.string.app_name));
		i.putExtra(APP_PACKAGE_NAME, context.getApplicationContext().getPackageName());
		
		String action = intent.getAction();
		if (action.equals(MediaPlaybackService.PLAYBACK_STARTED)) {
			state = START;
		} else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
			boolean isPlaying = intent.getBooleanExtra("playing", false);
			
			if (isPlaying) {
				state = RESUME;
			} else {
				state = PAUSE;
			}
		} else if (action.equals(MediaPlaybackService.PLAYBACK_COMPLETE)) {
			state = COMPLETE;
		}
		
		i.putExtra(STATE_NAME, state);
		i.putExtra(ARTIST_NAME, intent.getStringExtra("artist"));
		
		String album = intent.getStringExtra("album");
		if (album != null && !album.equals(Media.UNKNOWN_STRING)) {
			i.putExtra(ALBUM_NAME, album);
		}
		
		i.putExtra(TRACK_NAME, intent.getStringExtra("track"));
		i.putExtra(DURATION_NAME, ((int) intent.getLongExtra("duration", 0) / 1000));

		// send the broadcast
		context.sendBroadcast(i);
	}
}
