package git.lucianogg.tyranobuilder;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

public class MyJavaScriptInterface {

    private Context context;
    private String base_path;
    public MyJavaScriptInterface(Context context){
        this.context =context;
    }
    public MyJavaScriptInterface(Context context,String base_path){

        this.context =context;
        this.base_path = base_path;

    }

    @JavascriptInterface
    public void audio(String json_str){

        System.out.println("Audio!!!");
        System.out.println(json_str);

        try {

            JSONObject obj = new JSONObject(json_str);


            //((MainActivity)context).audio(obj);



        }catch(Exception e) {
            System.out.println("JSON ERROR!!!");
            System.out.println(e.toString());

        }

    }

    @JavascriptInterface
    public void setStorage(String key,String val){
        ((MainActivity)context).setStorage(key,val);

    }


    @JavascriptInterface
    public String getStorage(String key){

        String data = ((MainActivity)context).getStorage(key);
        return data;

    }

    @JavascriptInterface
    public void openUrl(String url){

        ((MainActivity)context).openUrl(url);

    }




    @JavascriptInterface
    public void stopMovie() {


    }
}
