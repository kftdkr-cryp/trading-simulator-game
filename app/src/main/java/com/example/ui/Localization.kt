package com.example.ui

object Localizer {
    
    private val translations = mapOf(
        "app_title" to mapOf(
            "TR" to "MARGIN CALL",
            "EN" to "MARGIN CALL",
            "HI" to "MARGIN CALL",
            "ZH" to "MARGIN CALL",
            "FR" to "MARGIN CALL",
            "RU" to "MARGIN CALL",
            "AZ" to "MARGIN CALL"
        ),
        "google_login_title" to mapOf(
            "TR" to "Borsaya Giriş Yap",
            "EN" to "Log in to Exchange",
            "HI" to "एक्सचेंज में लॉगिन करें",
            "ZH" to "登录交易系统",
            "FR" to "Se Connecter à l'Échange",
            "RU" to "Вход на биржу",
            "AZ" to "Birjaya Giriş Et"
        ),
        "google_login_desc" to mapOf(
            "TR" to "Piyasalarda işlem yapmaya ve 200 yapay zeka rakip ile rekabete girmeye hazır mısınız? Başlamak için Google hesabınız ile giriş yapın.",
            "EN" to "Ready to trade and compete against 200 AI rivals? Sign in with your Google account to get started.",
            "HI" to "ट्रेडिंग शुरू करने और 200 एआई प्रतिद्वंद्वियों से मुकाबला करने के लिए तैयार हैं? शुरू करने के लिए अपने Google खाते से साइन इन करें।",
            "ZH" to "准备好与200个AI对手进行交易与竞争了吗？请使用您的Google账户登录以开始。",
            "FR" to "Prêt à trader et affronter 200 rivaux IA ? Connectez-vous avec votre compte Google pour commencer.",
            "RU" to "Готовы к торговле и соревнованию с 200 ИИ-соперниками? Войдите через Google, чтобы начать.",
            "AZ" to "Bazarda ticarət etməyə və 200 süni intellekt rəqibi ilə mübarizə aparmağa hazırsınız? Başlamaq üçün Google hesabınızla giriş edin."
        ),
        "google_login_btn" to mapOf(
            "TR" to "Google ile Giriş Yap",
            "EN" to "Sign In with Google",
            "HI" to "Google के साथ साइन इन करें",
            "ZH" to "使用 Google 登录",
            "FR" to "Se connecter avec Google",
            "RU" to "Войти через Google",
            "AZ" to "Google ilə Giriş Et"
        ),
        "continue_with_google" to mapOf(
            "TR" to "Google ile devam et",
            "EN" to "Continue with Google",
            "HI" to "Google के साथ जारी रखें",
            "ZH" to "继续使用 Google",
            "FR" to "Continuer avec Google",
            "RU" to "Продолжить с Google",
            "AZ" to "Google ilə davam et"
        ),
        "continue_with_microsoft" to mapOf(
            "TR" to "Microsoft ile Devam Et",
            "EN" to "Continue with Microsoft",
            "HI" to "Microsoft के साथ जारी रखें",
            "ZH" to "继续使用 Microsoft",
            "FR" to "Continuer avec Microsoft",
            "RU" to "Продолжить с Microsoft",
            "AZ" to "Microsoft ilə davam et"
        ),
        "continue_with_apple" to mapOf(
            "TR" to "Apple ile devam et",
            "EN" to "Continue with Apple",
            "HI" to "Apple के साथ जारी रखें",
            "ZH" to "继续使用 Apple",
            "FR" to "Continuer avec Apple",
            "RU" to "Продолжить с Apple",
            "AZ" to "Apple ilə davam et"
        ),
        "select_account" to mapOf(
            "TR" to "Bir hesap seçin",
            "EN" to "Select an account",
            "HI" to "एक खाता चुनें",
            "ZH" to "选择一个帐户",
            "FR" to "Sélectionnez un compte",
            "RU" to "Выберите аккаунт",
            "AZ" to "Bir hesab seçin"
        ),
        "portfolio" to mapOf(
            "TR" to "Portföy",
            "EN" to "Portfolio",
            "HI" to "पोर्टफोलियो",
            "ZH" to "投资组合",
            "FR" to "Portefeuille",
            "RU" to "Портфель",
            "AZ" to "Portfel"
        ),
        "market" to mapOf(
            "TR" to "Grafik",
            "EN" to "Charts",
            "HI" to "चार्ट",
            "ZH" to "图表",
            "FR" to "Graphiques",
            "RU" to "Графики",
            "AZ" to "Qrafiklər"
        ),
        "social_feed" to mapOf(
            "TR" to "Akış",
            "EN" to "Feed",
            "HI" to "फ़ीड",
            "ZH" to "动态",
            "FR" to "Flux",
            "RU" to "Лента",
            "AZ" to "Axın"
        ),
        "leaderboard" to mapOf(
            "TR" to "Sıralama",
            "EN" to "Rankings",
            "HI" to "रैंकिंग",
            "ZH" to "排行榜",
            "FR" to "Sıralama",
            "RU" to "Рейтинги",
            "AZ" to "Sıralama"
        ),
        "mini_games" to mapOf(
            "TR" to "İşler / Oyunlar",
            "EN" to "Jobs & Games",
            "HI" to "काम और गेम्स",
            "ZH" to "任务与游戏",
            "FR" to "Jobs & Jeux",
            "RU" to "Работы и игры",
            "AZ" to "İşlər və Oyunlar"
        ),
        "earn_capital_desc" to mapOf(
            "TR" to "Sermayeniz sıfır! Borsaya atılmadan önce mini oyunları oynayarak ilk fonlarınızı kazanın.",
            "EN" to "Your capital is zero! Play mini-games to earn your initial funds before diving into the market.",
            "HI" to "आपकी पूंजी शून्य है! बाज़ार में उतरने से पहले प्रारंभिक धन कमाने के लिए मिनी-गेम खेलें।",
            "ZH" to "您的资本为零！在进入市场前，通过玩小游戏来赚取您的初始资金。",
            "FR" to "Votre capital est à zéro ! Jouez à des mini-jeux pour gagner vos premiers fonds avant de vous lancer.",
            "RU" to "Ваш капитал равен нулю! Играйте в мини-игры, чтобы заработать стартовые средства.",
            "AZ" to "Sermayəniz sıfırdır! Bazara girməzdən əvvəl mini oyunlar oynayaraq ilkin vəsaitinizi qazanın."
        ),
        "available_cash" to mapOf(
            "TR" to "Kullanılabilir Nakit",
            "EN" to "Available Cash",
            "HI" to "उपलब्ध नकद",
            "ZH" to "可用现金",
            "FR" to "Espèces Disponibles",
            "RU" to "Доступные наличные",
            "AZ" to "Mövcud Nağd Pul"
        ),
        "total_equity" to mapOf(
            "TR" to "Toplam Varlık Değeri",
            "EN" to "Total Net Worth",
            "HI" to "कुल शुद्ध मूल्य",
            "ZH" to "总净资产",
            "FR" to "Valeur Nette Totale",
            "RU" to "Общая стоимость активов",
            "AZ" to "Ümumi Aktiv Dəyəri"
        ),
        "spot_trade" to mapOf(
            "TR" to "Spot İşlem",
            "EN" to "Spot Trading",
            "HI" to "स्पॉट ट्रेडिंग",
            "ZH" to "现货交易",
            "FR" to "Trading Spot",
            "RU" to "Спот торговля",
            "AZ" to "Spot Ticarət"
        ),
        "leverage_trade" to mapOf(
            "TR" to "Kaldıraçlı İşlem",
            "EN" to "Leverage (Futures)",
            "HI" to "लेवरेज ट्रेडिंग",
            "ZH" to "杠杆(合约)",
            "FR" to "Marge & Levier",
            "RU" to "Маржинальная торговля",
            "AZ" to "Kaldıraçlı Ticarət"
        ),
        "buy" to mapOf(
            "TR" to "AL / BUY",
            "EN" to "BUY",
            "HI" to "खरीदें",
            "ZH" to "买入",
            "FR" to "ACHETER",
            "RU" to "КУПИТЬ",
            "AZ" to "AL"
        ),
        "sell" to mapOf(
            "TR" to "SAT / SELL",
            "EN" to "SELL",
            "HI" to "बेचें",
            "ZH" to "卖出",
            "FR" to "VENDRE",
            "RU" to "ПРОДАТЬ",
            "AZ" to "SAT"
        ),
        "long" to mapOf(
            "TR" to "LONG (YÜKSELİŞ)",
            "EN" to "LONG (BUY)",
            "HI" to "लॉन्ग (खरीदें)",
            "ZH" to "看涨 (LONG)",
            "FR" to "LONG (ACHAT)",
            "RU" to "LONG (ПОКУПКА)",
            "AZ" to "LONG (YÜKSƏLİŞ)"
        ),
        "short" to mapOf(
            "TR" to "SHORT (DÜŞÜŞ)",
            "EN" to "SHORT (SELL)",
            "HI" to "शॉर्ट (बेचें)",
            "ZH" to "看跌 (SHORT)",
            "FR" to "SHORT (VENTE)",
            "RU" to "SHORT (ПРОДАЖА)",
            "AZ" to "SHORT (ENİŞ)"
        ),
        "leverage_multiplier" to mapOf(
            "TR" to "Kaldıraç Çarpanı",
            "EN" to "Leverage Multiplier",
            "HI" to "लेवरेज गुणक",
            "ZH" to "杠杆倍数",
            "FR" to "Multiplicateur de Levier",
            "RU" to "Кредитное плечо",
            "AZ" to "Kaldıraç Çarpanı"
        ),
        "margin_amount" to mapOf(
            "TR" to "Yatırılacak Teminat ($)",
            "EN" to "Margin Amount ($)",
            "HI" to "मार्जिन राशि ($)",
            "ZH" to "保证金金额 ($)",
            "FR" to "Montant de Marge ($)",
            "RU" to "Сумма обеспечения ($)",
            "AZ" to "Yatırılacaq Təminat ($)"
        ),
        "liquidation_price" to mapOf(
            "TR" to "Likidasyon Fiyatı",
            "EN" to "Liquidation Price",
            "HI" to "लिक्विडेशन मूल्य",
            "ZH" to "强平价格",
            "FR" to "Prix de Liquidation",
            "RU" to "Цена ликвидации",
            "AZ" to "Likidasiya Qiyməti"
        ),
        "active_positions" to mapOf(
            "TR" to "Açık Pozisyonlarınız",
            "EN" to "Your Active Positions",
            "HI" to "आपके सक्रिय पोजीशन्स",
            "ZH" to "您的持仓",
            "FR" to "Vos Positions Actives",
            "RU" to "Ваши активные позиции",
            "AZ" to "Açıq Mövqeləriniz"
        ),
        "close_position" to mapOf(
            "TR" to "KAPAT",
            "EN" to "CLOSE",
            "HI" to "बंद करें",
            "ZH" to "平仓",
            "FR" to "FERMER",
            "RU" to "ЗАКРЫТЬ",
            "AZ" to "BAĞLA"
        ),
        "indicator_settings" to mapOf(
            "TR" to "Grafik İndikatörleri",
            "EN" to "Chart Indicators",
            "HI" to "चार्ट संकेतक",
            "ZH" to "图表指标",
            "FR" to "Indicateurs Graphiques",
            "RU" to "Индикаторы графика",
            "AZ" to "Qrafik İndikatorları"
        ),
        "drawing_tools" to mapOf(
            "TR" to "Çizim Araçları (Çiz / Temizle)",
            "EN" to "Drawing Tools (Draw / Clear)",
            "HI" to "ड्राइंग टूल्स (बनाएं / साफ करें)",
            "ZH" to "画线工具 (绘制 / 清除)",
            "FR" to "Outils de Dessin (Dessiner / Effacer)",
            "RU" to "Инструменты рисования",
            "AZ" to "Çəkiş Alətləri (Çək / Təmizlə)"
        ),
        "game1_title" to mapOf(
            "TR" to "Trend Tahmincisi (Grafik Analizi)",
            "EN" to "Trend Predictor (Chart Analysis)",
            "HI" to "ट्रेंड प्रेडिक्टर (चार्ट विश्लेषण)",
            "ZH" to "趋势预测器 (图表分析)",
            "FR" to "Prédicteur de Tendance",
            "RU" to "Прогноз тренда (Анализ графика)",
            "AZ" to "Trend Təxminçisi (Qrafik Analizi)"
        ),
        "game1_desc" to mapOf(
            "TR" to "Rastgele üretilen mum grafiklerinde sıradaki mumun Yeşil mi Kırmızı mı olacağını bil, para kazan!",
            "EN" to "Guess whether the next candle in a random chart will be Green or Red, and win cash!",
            "HI" to "अनुमान लगाएं कि एक यादृच्छिक चार्ट में अगला कैंडल हरा होगा या लाल, और नकद जीतें!",
            "ZH" to "预测随机图表中的下一个蜡烛是绿还是红，赢取现金奖励！",
            "FR" to "Devinez si la prochaine bougie sera Verte ou Rouge et gagnez de l'argent !",
            "RU" to "Угадайте, будет ли следующая свеча зеленой или красной, и выиграйте наличные!",
            "AZ" to "Təsadüfi yaradılan şam qrafiklərində növbəti şamın Yaşıl yoxsa Qırmızı olacağını təxmin et, pul qazan!"
        ),
        "game2_title" to mapOf(
            "TR" to "Kripto Hızlı Dokunma (Hacking Terminal)",
            "EN" to "Crypto Quick Tap (Hacking Terminal)",
            "HI" to "क्रिप्टो क्विक टैप (हैकिंग टर्मिनल)",
            "ZH" to "加密快点 (终端黑客)",
            "FR" to "Tap Rapide Crypto (Terminal)",
            "RU" to "Быстрый клик (Взлом терминала)",
            "AZ" to "Kripto Sürətli Toxunuş (Hacking Terminal)"
        ),
        "game2_desc" to mapOf(
            "TR" to "Hızlıca hareket eden barda hedef yeşil 'Kâr Al' bölgesine tam zamanında dokun, fonları kap!",
            "EN" to "Tap exactly when the fast-moving bar hits the green 'Take Profit' zone to claim your funds!",
            "HI" to "अपने धन का दावा करने के लिए तेजी से आगे बढ़ने वाले बार के हरे 'टेक प्रॉफिट' क्षेत्र में आने पर बिल्कुल सही समय पर टैप करें!",
            "ZH" to "当快速移动的指针正好落在绿色'止盈'区域时点击，夺取资金！",
            "FR" to "Tapez exactement quand la barre mobile touche la zone verte 'Take Profit' !",
            "RU" to "Нажмите точно в тот момент, когда индикатор попадет в зеленую зону 'Take Profit'!",
            "AZ" to "Sürətlə hərəkət edən barda hədəf yaşıl 'Mənfəəti Götür' bölgəsinə tam zamanında toxun, vəsaiti qazan!"
        ),
        "game3_title" to mapOf(
            "TR" to "Kripto Yakalama (Coin Catcher)",
            "EN" to "Crypto Catcher (Coin Catcher)",
            "HI" to "क्रिप्टो कैचर (सिक्का पकड़ने वाला)",
            "ZH" to "加密接币器 (接金币)",
            "FR" to "Attrape-Kryptos (Coin Catcher)",
            "RU" to "Ловец криптовалюты",
            "AZ" to "Kripto Tutucu (Coin Catcher)"
        ),
        "game3_desc" to mapOf(
            "TR" to "Ekranda uçuşan yeşil logolara dokunarak puan kazan, kırmızı zehirli 'Likidasyon' logolarından uzak dur!",
            "EN" to "Tap flying green coins to score points, but strictly avoid red toxic 'Liquidation' icons!",
            "HI" to "अंक प्राप्त करने के लिए उड़ने वाले हरे सिक्कों पर टैप करें, लेकिन लाल जहरीले 'लिक्विडेशन' आइकन से बचें!",
            "ZH" to "点击飘浮的绿色代币以得分，但千万避开红色的有害'清算'图标！",
            "FR" to "Touchez les pièces vertes volantes mais évitez les icônes rouges de liquidation !",
            "RU" to "Нажимайте на летающие зеленые монеты, но избегайте красных значков ликвидации!",
            "AZ" to "Ekranda uçuşan yaşıl loqolara toxunaraq xal qazan, qırmızı zəhərli 'Likidasiya' loqolarından uzaq dur!"
        ),
        "play" to mapOf(
            "TR" to "OYNA / PLAY",
            "EN" to "PLAY",
            "HI" to "खेलें",
            "ZH" to "开始玩",
            "FR" to "JOUER",
            "RU" to "ИГРАТЬ",
            "AZ" to "OYNA"
        ),
        "hospital_illness_msg" to mapOf(
            "TR" to "BORÇLARIN SENİ AÇ BIRAKTI VE HASTALANDIN...",
            "EN" to "YOUR DEBTS LEFT YOU HUNGRY AND YOU FELL ILL...",
            "ES" to "TUS DEUDAS TE DEJARON HAMBRIENTO Y TE ENFERMASTE...",
            "DE" to "DEINE SCHULDEN HABEN DICH HUNGRIG GEMACHT UND DU BIST KRANK GEWORDEN...",
            "RU" to "ТВОИ ДОЛГИ ОСТАВИЛИ ТЕБЯ ГОЛОДНЫМ, И ТЫ ЗАБОЛЕЛ...",
            "ZH" to "你的债务让你饥饿交迫，你病倒了……",
            "HI" to "आपके कर्ज ने आपको भूखा छोड़ दिया और आप बीमार पड़ गए...",
            "AZ" to "BORCLARIN SƏNİ AC QOYDU VƏ XƏSTƏLƏNDİN...",
            "FR" to "VOS DETTES VOUS ONT LAISSÉ AFFAMÉ ET VOUS ÊTES TOMBÉ MALADE...",
            "TH" to "หนี้สินทำให้คุณหิวโหยและล้มป่วย..."
        ),
        "hospital_died_msg" to mapOf(
            "TR" to "HAYATINI KAYBETTİN",
            "EN" to "YOU LOST YOUR LIFE",
            "ES" to "PERDISTE LA VIDA",
            "DE" to "DU HAST DEIN LEBEN VERLOREN",
            "RU" to "ВЫ ПОТЕРЯЛИ ЖИЗНЬ",
            "ZH" to "你失去了生命",
            "HI" to "आपने अपनी जान गंवा दी",
            "AZ" to "HƏYATINI İTİRDİN",
            "FR" to "VOUS AVEZ PERDU LA VIE",
            "TH" to "คุณเสียชีวิตแล้ว"
        ),
        "play_again" to mapOf(
            "TR" to "TEKRAR OYNA",
            "EN" to "PLAY AGAIN",
            "ES" to "JUGAR DE NUEVO",
            "DE" to "NOCHMAL SPIELEN",
            "RU" to "ИГРАТЬ СНОВА",
            "ZH" to "再玩一次",
            "HI" to "फिर से खेलें",
            "AZ" to "YENİDƏN OYNA",
            "FR" to "REJOUER",
            "TH" to "เล่นอีกครั้ง"
        ),
        "settings_title" to mapOf(
            "TR" to "Ayarlar / Settings",
            "EN" to "Settings",
            "ES" to "Ajustes",
            "DE" to "Einstellungen",
            "RU" to "Настройки",
            "ZH" to "设置",
            "HI" to "सेटिंग्स",
            "AZ" to "Ayarlar",
            "FR" to "Paramètres",
            "TH" to "การตั้งค่า"
        ),
        "theme_mode" to mapOf(
            "TR" to "Tema Modu / Theme",
            "EN" to "Theme Mode",
            "ES" to "Modo de tema",
            "DE" to "Themamodus",
            "RU" to "Режим темы",
            "ZH" to "主题模式",
            "HI" to "थीम मोड",
            "AZ" to "Tema Modu",
            "FR" to "Mode de Thème",
            "TH" to "โหมดธีม"
        ),
        "dark_mode" to mapOf(
            "TR" to "Koyu Tema / Dark",
            "EN" to "Dark Mode",
            "ES" to "Modo oscuro",
            "DE" to "Dunkelmodus",
            "RU" to "Тёмный режим",
            "ZH" to "暗黑模式",
            "HI" to "डार्क मोड",
            "AZ" to "Qaranlıq Tema",
            "FR" to "Mode Sombre",
            "TH" to "โหมดมืด"
        ),
        "light_mode" to mapOf(
            "TR" to "Açık Tema / Light",
            "EN" to "Light Mode",
            "ES" to "Modo claro",
            "DE" to "Hellmodus",
            "RU" to "Светлый режим",
            "ZH" to "明亮模式",
            "HI" to "लाइट मोड",
            "AZ" to "Açıq Tema",
            "FR" to "Mode Clair",
            "TH" to "โหมดสว่าง"
        ),
        "close" to mapOf(
            "TR" to "Kapat / Close",
            "EN" to "Close",
            "ES" to "Cerrar",
            "DE" to "Schließen",
            "RU" to "Закрыть",
            "ZH" to "关闭",
            "HI" to "बंद करें",
            "AZ" to "Bağla",
            "FR" to "Fermer",
            "TH" to "ปิด"
        )
    )

    fun translate(key: String, lang: String): String {
        val keyMap = translations[key] ?: return key
        return keyMap[lang] ?: keyMap["EN"] ?: key
    }
}
