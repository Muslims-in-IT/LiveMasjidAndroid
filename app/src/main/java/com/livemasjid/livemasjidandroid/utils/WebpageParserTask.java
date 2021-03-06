package com.livemasjid.livemasjidandroid.utils;

import java.util.List;

import com.livemasjid.livemasjidandroid.bean.UriBean;
import com.livemasjid.livemasjidandroid.fragment.BrowseFragment;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class WebpageParserTask extends AsyncTask<Void, Void, List<UriBean>> {

	private Handler mHandler;
	private UriBean mDirectory;
	
	public WebpageParserTask(Handler handler, UriBean directory) {
		mHandler = handler;
		mDirectory = directory;
	}
	
	@Override
	protected List<UriBean> doInBackground(Void... params) {
		WebpageParser webpageParser = new WebpageParser(mDirectory.getURL());
		webpageParser.parse();
		return webpageParser.getParsedLinks();
	}

	@Override
    protected void onPostExecute(List<UriBean> uris) {
		Message msg = mHandler.obtainMessage(BrowseFragment.MESSAGE_SHOW_DIRECTORY_CONTENTS);
	    msg.obj = uris;
	    mHandler.sendMessage(msg);
    }
}
