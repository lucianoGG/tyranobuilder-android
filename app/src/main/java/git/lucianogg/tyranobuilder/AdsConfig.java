package git.lucianogg.tyranobuilder;

public class AdsConfig {

    /**
     * edit strings.xml file
     * ----------APPLICATION_ID_ADS------------
     * here you put the application id as shown in the APPLICATION_ID_ADS image
     *
     *
     * BannerAd and InterstitialAd
     * here you put the banner id and Interstitial as shown in the ids_banner_interstitial image
     *
     * if in doubt, look for the official documentation at https://developers.google.com/admob/android/quick-start
     */


    //here shows the banner at the top
    public boolean isShowBannerAd = true;

    //here shows the Interstitial
    public boolean isShowInterstitialAd = true;

    //here you choose how many seconds the Interstitial ads appear (if you want to increase it to minutes, put for example 1 * 60)
    //example in 1 minute
    // private final long interstitialAdSeconds = 1 * 60;
    private final long interstitialAdSeconds = 40;

    //ignore
    public long seconds = interstitialAdSeconds * 1000;

}
