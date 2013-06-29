package com.example.jockeytestapp;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jockeyjs.Jockey;
import com.jockeyjs.JockeyAsyncHandler;
import com.jockeyjs.JockeyCallback;
import com.jockeyjs.JockeyHandler;
import com.jockeyjs.JockeyService;
import com.jockeyjs.JockeyService.JockeyBinder;

public class MainActivity extends Activity {

	public WebView webView;
	
	public LinearLayout toolbar;
	public boolean isFullscreen = false;
	
	private Jockey jockey;
	private boolean _bound;
	
	private ServiceConnection _connection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			_bound = false;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			JockeyBinder binder = (JockeyBinder) service;
			jockey = binder.getService();

			jockey.configure(webView);
			setJockeyEvents();
			_bound = true;
			
			webView.setWebChromeClient(new WebChromeClient() {
				@Override
				public boolean onJsAlert(WebView view, String url, String message,
						JsResult result) {
					Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
					result.confirm();
					return true;
				}
			});
			webView.loadUrl("file:///android_asset/index.html");
		}
	};

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		toolbar = (LinearLayout) findViewById(R.id.colorsView);

		webView = (WebView) findViewById(R.id.webView);

		OnClickListener toolbarListener = new OnClickListener() {

			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onClick(View v) {
				ImageButton btn = (ImageButton) v;
				ColorDrawable background = (ColorDrawable) btn.getBackground();
				int colorId = background.getColor();
				String hex = String.format("#%06X", 0xFFFFFF & colorId);

				HashMap<String, String> payload = new HashMap<String, String>();
				payload.put("color", hex);

				updateColor(payload);
			}
		};

		ImageButton btnRed = (ImageButton) findViewById(R.id.color_red);
		ImageButton btnGreen = (ImageButton) findViewById(R.id.color_green);
		ImageButton btnYellow = (ImageButton) findViewById(R.id.color_yellow);
		ImageButton btnOrange = (ImageButton) findViewById(R.id.color_orange);
		ImageButton btnPink = (ImageButton) findViewById(R.id.color_pink);
		ImageButton btnBlue = (ImageButton) findViewById(R.id.color_blue);
		ImageButton btnWhite = (ImageButton) findViewById(R.id.color_white);

		btnRed.setOnClickListener(toolbarListener);
		btnGreen.setOnClickListener(toolbarListener);
		btnYellow.setOnClickListener(toolbarListener);
		btnOrange.setOnClickListener(toolbarListener);
		btnPink.setOnClickListener(toolbarListener);
		btnBlue.setOnClickListener(toolbarListener);
		btnWhite.setOnClickListener(toolbarListener);
	}
	
	protected void updateColor(Map<String, String> payload) {
		jockey.send("color-change", webView, payload);
	}

	@Override
	protected void onStart() {
		super.onStart();
		JockeyService.bind(this, _connection);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (_bound) {
			JockeyService.unbind(this, _connection);
			_bound = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_showimage:
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("feed", "http://www.google.com/doodles/doodles.xml");
			
			jockey.send("show-image", webView, new JockeyCallback() {
				public void call() {
					AlertDialog.Builder alert = new AlertDialog.Builder(
							MainActivity.this);
					alert.setTitle("Image loaded");
					alert.setMessage("callback in Android from JS event");
					alert.setNegativeButton("Score!",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
								}
							});
					alert.show();
				}
			});
			break;
		}

		return true;
	}
	
	private Handler _handler = new Handler();

	public void setJockeyEvents() {
		jockey.on("toggle-fullscreen", new JockeyHandler() {
			@Override
			protected void doPerform(Map<Object, Object> payload) {
				toggleFullscreen();
			}
		});
		
		jockey.on("toggle-fullscreen-with-callback", new JockeyAsyncHandler() {
			@Override
			protected void doPerform(Map<Object, Object> payload) {
				_handler.post(new Runnable() {
					@Override
					public void run() {
						toggleFullscreen();
					}});
				
			}
		});
		
		jockey.on("log", new JockeyHandler() {
			@Override
			public void doPerform(Map<Object, Object> payload) {
				String value = "color=" + payload.get("color");
				System.out.println(value);
				Log.d("jockey", value);
			}
			
		});
	}

	public void toggleFullscreen() {
		// TODO Auto-generated method stub
		
		Window w = getWindow();
		
		if (isFullscreen) {
			w.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			w.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

			toolbar.setVisibility(LinearLayout.VISIBLE);
			isFullscreen = false;
		} else {
			w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			w.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

			toolbar.setVisibility(LinearLayout.GONE);
			isFullscreen = true;
		}
	}
}
