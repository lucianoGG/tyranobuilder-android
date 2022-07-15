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

public class MainActivity extends AppCompatActivity {

    private WebView webview ;

    private String base_url ;
    private String base_path ;

    private boolean flag_init = false;

    private static final int PERMISSION_REQUEST_CODE = 1;

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

        MyJavaScriptInterface obj = new MyJavaScriptInterface(this, base_path);
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

    public void startGame(BufferedReader reader){

        System.out.println("start game!!");

        try{
            String s = "";

            StringBuilder et = new StringBuilder();
            String data;
            while ((data = reader.readLine()) != null) {

                if(data.contains("</head>")){

                    et.append("<script type='text/javascript' src='file:///android_asset/tyrano_player.js'></script>");
                    et.append("\n");
                    et.append("</head>");


                }else {
                    et.append(data);
                }

                et.append("\n");
            }

            s = et.toString();

            final String html_str = s;

            System.out.println(html_str);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webview.loadDataWithBaseURL(base_url, html_str, "text/html", "UTF-8",null);
                }
            });

        }catch(Exception e){
            System.out.print("erroror");
            System.out.print(e.toString());
        }

    }

    public void setStorage(String key, String val){
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                _setStorage(key, val);
            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {
            _setStorage(key, val);
        }

    }

    public void _setStorage(String key, String val){

        File dataDir = getFilesDir();
        String localPath = dataDir.getAbsolutePath()+ "/";
        String save_path = localPath + key +".sav";

        try{

            File file = new File(save_path);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file,false)));
            pw.println(val);
            pw.close();

        }catch(IOException e){
            System.out.println(e);
        }

    }

    public String getStorage(String key){
        String get = "";
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                get =  _getStorage(key);
            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {
            get =  _getStorage(key);
        }

        return get;
    }

    private String _getStorage(String key){
        File dataDir = getFilesDir();
        String localPath = dataDir.getAbsolutePath()+ "/";
        String save_path = localPath + key +".sav";
        StringBuilder result_str = new StringBuilder();
        try{
            File file = new File(save_path);

            if(!file.exists()){

                System.out.println("file not found!");
                return "";
            }

            BufferedReader br = new BufferedReader(new FileReader(file));

            String str ;
            while((str = br.readLine()) != null){
                result_str.append(str);
            }

            br.close();
        } catch(IOException e){
            System.out.println(e.toString());
        }

        return result_str.toString();
    }

    @Override
    public void onBackPressed() {
        if (interstitialAd != null) {
            interstitialAd.show(this);
        } else {
            exitGame();
        }

    }

    public  void exitGame(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("sair do jogo?")
                .setCancelable(false)
                .setPositiveButton("SIM",
                        (dialog, id) -> {
                            moveTaskToBack(true);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        })

                .setNegativeButton("NÃO", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webview.saveState(outState);
    }
    @Override
    public void onRestoreInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void openUrl(String url){
        Uri uri = Uri.parse(url);
        startActivity(new Intent(Intent.ACTION_VIEW,uri));

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setTitle("Permissão para SAVE")
                    .setMessage("o jogo precisa de permissão para salvar e ler os saves")
                    .setCancelable(false)
                    .setPositiveButton("SIM",
                            (dialog, id) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })

                    .setNegativeButton("NÃO", (dialog, id) -> dialog.cancel());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .");
            } else {
                Toast.makeText(this, "Permissão de acesso negado, você não pode salvar e nem carregar", Toast.LENGTH_LONG).show();
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
        }
    }
}