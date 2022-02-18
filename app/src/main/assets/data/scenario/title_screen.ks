
;==============================
; タイトル画面
;==============================

[hidemenubutton]

[tb_clear_images]

[tb_keyconfig flag=0]


	;標準のメッセージレイヤを非表示
	[tb_hide_message_window]

	;タイトル表示
	[bg storage ="title.jpg"]

	*title

	

	[glink color="black" text="New&nbsp;Game" x=75 y=370 size=20 target="*start"]
	[glink color="black" text="Load&nbsp;Game" x=75 y=470 size=20 target="*load"]


	

	[s]

	;-------ボタンが押されたときの処理

	*start

	
	[showmenubutton]
	

	[cm]
	[tb_keyconfig flag=1]

	@jump storage="scene1.ks"
	[s]

	;--------ロードが押された時の処理
	*load

	[cm]
	[showload]
	[jump target=*title]

	[s]


