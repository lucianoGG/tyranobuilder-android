package git.lucianogg.tyranobuilder;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import androidx.activity.OnBackPressedCallback;

public class MainActivity extends AppCompatActivity {

    private WebView webview ;

    private String base_url ;
    private boolean flag_init = false;

    private AdView mAdView;

    private InterstitialAd interstitialAd;
    private CountDownTimer countDownTimer;
    private long timerMilliseconds;
    private boolean gameIsInProgress;

    AdsConfig adsConfig;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webview = findViewById(R.id.webview_playgame);

        if(savedInstanceState != null){
            webview.restoreState(savedInstanceState);
            return;
        }


        final String base_url = "file:///android_asset/";;

        this.base_url = base_url;


        adsConfig = new AdsConfig();

        String userAgent = webview.getSettings().getUserAgentString();
        webview.getSettings().setUserAgentString(userAgent + ";tyranoplayer-android-1.0");
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAllowFileAccessFromFileURLs(true);
        webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webview.getSettings().setUseWideViewPort(true);
        webview.clearCache(true);

        MyJavaScriptInterface obj = new MyJavaScriptInterface(this, base_url);
        webview.addJavascriptInterface(obj,"appJsInterface");

        try {

            BufferedReader reader ;
            reader = new BufferedReader(new InputStreamReader(getAssets().open("index.html"), StandardCharsets.UTF_8));
            startGame(reader);

        }catch (Exception e) {
            System.out.println("ERROROROROR");
            System.out.print(e.toString());
        }

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                Log.d("dddd",request.toString());
                return false;
            }
        });


if(adsConfig.isShowBannerAd){
    List<String> testDeviceIds = Collections.singletonList("33BE2250B43518CCDA7DE426D04EE231");
    RequestConfiguration configuration =
            new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
    MobileAds.setRequestConfiguration(configuration);


    //ADS
    MobileAds.initialize(this, initializationStatus -> {
    });

    MobileAds.setRequestConfiguration(
            new RequestConfiguration.Builder().setTestDeviceIds(Collections.singletonList("ABCDEF012345"))
                    .build());

    mAdView = new AdView(this);

    mAdView = findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);
}

       if(adsConfig.isShowInterstitialAd){
           loadAd();
           startInterstitialAd();
       }

    }



    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                getString(R.string.InterstitialAd),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        MainActivity.this.interstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {

                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i(TAG, loadAdError.getMessage());
                        interstitialAd = null;
                    }
                });
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(this);
        } else {
            startInterstitialAd();
        }
    }
    private void startInterstitialAd() {
        if (interstitialAd == null) {
            loadAd();
        }

        InterstitialAdSeconds(adsConfig.seconds);
    }

    private void InterstitialAdSeconds(long milliseconds) {
        gameIsInProgress = true;
        timerMilliseconds = milliseconds;
        createTimer(timerMilliseconds);
        countDownTimer.start();
    }



    @Override
    public void onResume() {
        super.onResume();
        if(flag_init) {
            webview.loadUrl("javascript:_tyrano_player.resumeAllAudio();");
            webview.onResume();
        }else {
            flag_init = true;
        }

        mAdView.resume();

        if (gameIsInProgress) {
            InterstitialAdSeconds(timerMilliseconds);
        }

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (interstitialAd != null) {
                    interstitialAd.show(MainActivity.this);
                } else {
                    finishGame();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        webview.loadUrl("javascript:_tyrano_player.pauseAllAudio();");
        webview.onPause();

        mAdView.pause();
        countDownTimer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webview != null) {
            webview.destroy();
            webview = null;
        }

        mAdView.destroy();

    }

    private void createTimer(final long milliseconds) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(milliseconds, 50) {
            @Override
            public void onTick(long millisUnitFinished) {
                timerMilliseconds = millisUnitFinished;
               // Log.d("TAG", "seconds remaining:"  + millisUnitFinished);
            }

            @Override
            public void onFinish() {
                showInterstitial();
            }
        };
    }

    //add method to start game, adjust the path to your html file and the path to the tyrano_player.js file
    public void startGame(BufferedReader reader) {
        Log.d("MainActivity", "start game!!");

        try {
            StringBuilder htmlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("</head>")) {
                    htmlBuilder.append("<script type='text/javascript' src='file:///android_asset/tyrano_player.js'></script>\n");
                }
                htmlBuilder.append(line).append("\n");
            }

            final String htmlContent = htmlBuilder.toString();
            Log.d("MainActivity", htmlContent);

            runOnUiThread(() -> webview.loadDataWithBaseURL(base_url, htmlContent, "text/html", "UTF-8", null));

        } catch (IOException e) {
            Log.e("MainActivity", "Error reading HTML data", e);
        }
    }


    //access to storage of app not need permission
    //remove the permission of android.permission.WRITE_EXTERNAL_STORAGE in AndroidManifest.xml
    public void setStorage(String key, String val) {

        File file = new File(getFilesDir(), key + ".sav");

        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)))) {
            pw.println(val);
        } catch (IOException e) {
            Toast.makeText(this, "Error writing to file: " + key, Toast.LENGTH_SHORT).show();
            Log.e("Storage", "Error writing to file: " + key, e);
        }
    }


    public String getStorage(String key) {


        File file = new File(getFilesDir(), key + ".sav");

        if (!file.exists()) {
            Log.e("Storage", "File not found: " + key);
            return "";
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error reading file: " + key, Toast.LENGTH_SHORT).show();
            Log.e("Storage", "Error reading file: " + key, e);
            return "";
        }

        return result.toString();
    }

    public  void finishGame() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Exit Game?")
                .setCancelable(false)
                .setPositiveButton("yes",
                        (dialog, id) -> {
                            moveTaskToBack(true);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        })

                .setNegativeButton("no", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void openUrl (String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}