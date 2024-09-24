

var TyranoPlayer = (function() {
    // クラス内定数
    var COUNTRY = 'aaaaa';

    // コンストラクタ
    var TyranoPlayer = function(storage_url) {
        if (!(this instanceof TyranoPlayer)) {
            return new TyranoPlayer(storage_url);
        }

        this.storage_url = storage_url;
        //this.age  = age;
        //this.volume ="1";
        //this.loop = false;
        //this.currentTime = 0;
        //this.map_event = {}; //コールバックが必要な部分について、イベント登録する。

    }
    var p = TyranoPlayer.prototype;

    p.pauseAllAudio = function() {
        //tyranoのすべてのaudioを停止する。
        console.log("pause All Audio!");
        console.log(TYRANO.kag.tmp.map_bgm);

        var bgm_objs = TYRANO.kag.tmp.map_bgm;
        var se_objs = TYRANO.kag.tmp.map_se;

        for (var key in bgm_objs) {
            bgm_objs[key].pause();
        }

        for (var key in se_objs) {
            se_objs[key].pause();
        }

    }
    p.resumeAllAudio = function() {
        //tyranoのすべてのaudioを再開する。
        console.log("resume All Audio!");
        var bgm_objs = TYRANO.kag.tmp.map_bgm;
        var se_objs = TYRANO.kag.tmp.map_se;

	if(TYRANO.kag.stat.current_bgm==""){
            return;
        }

        if (bgm_objs[TYRANO.kag.stat.current_bgm]) {
            bgm_objs[TYRANO.kag.stat.current_bgm].play();
        } else if (bgm_objs[0]) {
            bgm_objs[0].play();
        }

        //seは再開しない
        /*
         for(var key in se_objs){
         se_objs[key].play();
         }
         */

    }

    return TyranoPlayer;

})();

//ティラノプレイヤーの定義
var _tyrano_player = new TyranoPlayer("");

//fitBaseSizeの上書き
tyrano.base.fitBaseSize = function(width, height) {

    $(".tyrano_base").css("position","absolute");

    var that = this;
    var view_width = $.getViewPort().width;
    var view_height = $.getViewPort().height;

    var width_f = view_width / width;
    var height_f = view_height / height;

    var scale_f = 0;

    var space_width = 0;

    var screen_ratio = this.tyrano.kag.config.ScreenRatio;

    //比率を固定にしたい場合は以下　以下のとおりになる
    if (screen_ratio == "fix") {

        if (width_f > height_f) {
            scale_f = height_f;
        } else {
            scale_f = width_f;
        }

        this.tyrano.kag.tmp.base_scale = scale_f;

        setTimeout(function() {

            //中央寄せなら、画面サイズ分を引く。
            if (true) {

                $(".tyrano_base").css("transform-origin", "0 0");
                $(".tyrano_base").css({
                    margin : 0
                });

                var width = Math.abs(parseInt(window.innerWidth) - parseInt(that.tyrano.kag.config.scWidth * scale_f)) / 2;
                var height = Math.abs(parseInt(window.innerHeight) - parseInt(that.tyrano.kag.config.scHeight * scale_f)) / 2;

                if (width_f > height_f) {
                    $(".tyrano_base").css("left", width + "px");
                    $(".tyrano_base").css("top", "0px");
                } else {

                    $(".tyrano_base").css("left", "0px");
                    $(".tyrano_base").css("top", height + "px");

                }

            }

            $(".tyrano_base").css("transform", "scale(" + scale_f + ") ");
            if (parseInt(view_width) < parseInt(width)) {
                if (scale_f < 1) {
                    window.scrollTo(width, height);
                }
            }

        }, 100);

    } else if (screen_ratio == "fit") {

        //スクリーンサイズに合わせて自動的に調整される
        setTimeout(function() {
            $(".tyrano_base").css("transform", "scaleX(" + width_f + ") scaleY(" + height_f + ")");
            window.scrollTo(width, height);
        }, 100);

    } else {

        //スクリーンサイズ固定

    }

};

//セーブデータ
$.setStorage = function(key, val, type) {
    if ("appJsInterface" in window) {
        appJsInterface.setStorage(key, escape(JSON.stringify(val)));
    } else {
        //セーブと同時に、データを書き出しておく。
        window.tyrano_save[key] = encodeURIComponent(JSON.stringify(val));
        location.href = 'tyranoplayer-save://?key=' + key + '&data=' + encodeURIComponent(JSON.stringify(val));

    }
}

$.getStorage = function(key, type) {

    //とりあえず、データは保存する
    console.log("bbbb");
    console.log(key);

    if ("appJsInterface" in window) {

        try {

            var json_str = appJsInterface.getStorage(key);

            if (json_str == "") {
                return null;
            }

            return unescape(json_str);

        } catch(e) {
            console.log(e);
        }

    } else {
        //console.log("load:"+key+"/"+window.tyrano_save[key]);

        if (!window.tyrano_save[key] || window.tyrano_save[key] == "") {
            return null;
        } else {
            return decodeURIComponent(window.tyrano_save[key]);
        }
    }

}

$.openWebFromApp = function(url){
    if ("appJsInterface" in window) {
        appJsInterface.openUrl(url);
    } else {
        location.href = 'tyranoplayer-web://?url=' + url;
    }
}


setTimeout(function(){

    (function() {

        var player_back_cnt = 0;

        var j_menu_button = $("<div id='player_menu_button' class='player_menu_area' style='display:none;opacity:0.6;border-radius:5px;padding:10px;margin:10px;cursor:pointer;position:absolute;left:0px;top:0px;background-color:white'><span style='color:#6495ED'>メニュー</span></div>");
        var j_end_button = $("<div class='player_menu_area' id='player_end_button' style='display:none;opacity:0.6;border-radius:5px;padding:10px;margin:10px 10px 10px 10px;cursor:pointer;position:absolute;left:0px;top:0px;background-color:white'><span style='color:#6495ED'>タイトルへ</span></div>");
        var j_auto_button = $("<div class='player_menu_area' id='player_auto_button' style='display:none;opacity:0.6;border-radius:5px;padding:10px;margin:60px 10px 10px 10px;cursor:pointer;position:absolute;left:0px;top:0px;background-color:white'><span style='color:#6495ED'>オート</span></div>");
        var j_skip_button = $("<div class='player_menu_area' id='player_skip_button' style='display:none;opacity:0.6;border-radius:5px;padding:10px;margin:110px 10px 10px 10px;cursor:pointer;position:absolute;left:0px;top:0px;background-color:white'><span style='color:#6495ED'>スキップ</span></div>");

        function hide_menu(){
                j_end_button.hide();
                j_auto_button.hide();
                j_skip_button.hide();
                j_menu_button.hide();
                player_back_cnt = 0;
        }

        j_menu_button.click(function(e) {
            j_menu_button.hide();
            j_end_button.show();
            j_auto_button.show();
            j_skip_button.show();

        });

        j_end_button.click(function(e) {
            //アンドロイドとiOSで処理分け
            hide_menu();
            if ("appJsInterface" in window) {
                appJsInterface.finishGame();
            } else {
                location.href = "tyranoplayer-back://endgame";
            }

            e.stopPropagation();

        });

        j_auto_button.click(function(e) {

            hide_menu();
            TYRANO.kag.ftag.startTag("autostart", {});
            e.stopPropagation();
        });

        j_skip_button.click(function(e) {

            hide_menu();
            TYRANO.kag.ftag.startTag("skipstart", {});
            e.stopPropagation();
        });

        $("body").append(j_menu_button);

        $("body").append(j_end_button);
        $("body").append(j_auto_button);
        $("body").append(j_skip_button);

        $("#tyrano_base").on("click.player", function() {

            if(player_back_cnt > 8){
                hide_menu();
            }

            player_back_cnt = 0;

        });

        //macプレイヤーの場合、10秒操作がなかったら、ボタンを表示する。
        setInterval(function() {
            if (player_back_cnt == 9) {
                j_menu_button.show();
            } else if (player_back_cnt > 3) {

            }

            player_back_cnt++;

        }, 1000);

    })();


    $("#tyrano_base").on("click.player", function() {
        player_back_cnt = 0;
    });


},1000);

/*
 $.userenv = function(){
 console.log("wwww");
 return "pc";
 }
 */

