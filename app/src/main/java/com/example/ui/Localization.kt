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
            "TR" to "İşler & Oyunlar",
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
            "TR" to "AL",
            "EN" to "BUY",
            "HI" to "खरीदें",
            "ZH" to "买入",
            "FR" to "ACHETER",
            "RU" to "КУПИТЬ",
            "AZ" to "AL"
        ),
        "sell" to mapOf(
            "TR" to "SAT",
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
            "TR" to "Çizim Araçları (Çiz & Temizle)",
            "EN" to "Drawing Tools (Draw & Clear)",
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
            "TR" to "OYNA",
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
        "secure_session" to mapOf(
            "TR" to "GÜVENLİ OTURUM",
            "EN" to "SECURE SESSION",
            "ES" to "SESIÓN SEGURA",
            "DE" to "SICHERE SITZUNG",
            "RU" to "ЗАЩИЩЕННЫЙ СЕАНС",
            "ZH" to "安全会话",
            "HI" to "सुरक्षित सत्र",
            "AZ" to "TƏHLÜKƏSİZ SESSİYA",
            "FR" to "SESSION SÉCURISÉE",
            "TH" to "เซสชันที่ปลอดภัย"
        ),
        "career_progress" to mapOf(
            "TR" to "Kariyer Hedefiniz",
            "EN" to "Career Progress",
            "ES" to "Progreso de Carrera",
            "DE" to "Karrierefortschritt",
            "RU" to "Прогресс карьеры",
            "ZH" to "职业生涯进度",
            "HI" to "करियर प्रगति",
            "AZ" to "Karyera Hədəfiniz",
            "FR" to "Progression de Carrière",
            "TH" to "ความก้าวหน้าทางอาชีพ"
        ),
        "live_market" to mapOf(
            "TR" to "PİYASA AKTİF",
            "EN" to "LIVE MARKET",
            "ES" to "MERCADO ACTIVO",
            "DE" to "MARKT AKTIV",
            "RU" to "РЫНОК АКТИВЕН",
            "ZH" to "市场活跃",
            "HI" to "बाज़ार सक्रिय",
            "AZ" to "BAZAR AKTİVDİR",
            "FR" to "MARCHÉ ACTIF",
            "TH" to "ตลาดเปิดทำการ"
        ),
        "gameplay_tip" to mapOf(
            "TR" to "Oynanış İpucu",
            "EN" to "Gameplay Tip",
            "ES" to "Consejo de Juego",
            "DE" to "Gameplay-Tipp",
            "RU" to "Игровой совет",
            "ZH" to "游戏提示",
            "HI" to "गेมप्ले टिप",
            "AZ" to "Oyun İpucu",
            "FR" to "Conseil de Jeu",
            "TH" to "คำแนะนำในการเล่น"
        ),
        "net_worth" to mapOf(
            "TR" to "Net Değer",
            "EN" to "Net Worth",
            "ES" to "Valor Neto",
            "DE" to "Nettovermögen",
            "RU" to "Чистая стоимость",
            "ZH" to "净资产",
            "HI" to "कुल मूल्य",
            "AZ" to "Net Dəyər",
            "FR" to "Valeur Nette",
            "TH" to "มูลค่าสุทธิ"
        ),
        "target" to mapOf(
            "TR" to "Hedef",
            "EN" to "Target",
            "ES" to "Objetivo",
            "DE" to "Ziel",
            "RU" to "Цель",
            "ZH" to "目标",
            "HI" to "लक्ष्य",
            "AZ" to "Hədəf",
            "FR" to "Cible",
            "TH" to "เป้าหมาย"
        ),
        "milestone_1_title" to mapOf(
            "TR" to "Bölüm 1: Sıfırdan Başlangıç",
            "EN" to "Chapter 1: Starting from Zero",
            "ES" to "Capítulo 1: Empezando de Cero",
            "DE" to "Kapitel 1: Neuanfang von Null",
            "RU" to "Глава 1: Старт с нуля",
            "ZH" to "第1章：从零开始",
            "HI" to "अध्याय 1: शून्य से शुरुआत",
            "AZ" to "Bölüm 1: Sıfırdan Başlamaq",
            "FR" to "Chapitre 1 : Partir de Zéro",
            "TH" to "บทที่ 1: เริ่มต้นจากศูนย์"
        ),
        "milestone_1_desc" to mapOf(
            "TR" to "Borsada adını duyurmak için ilk adımı attın. Mini oyunlarla veya akıllı işlemlerle sermaye biriktir.",
            "EN" to "You took the first step to make a name in the exchange. Accumulate capital with mini-games or smart trades.",
            "ES" to "Diste el primer paso para hacerte un nombre en la bolsa. Acumula capital con mini juegos o transacciones inteligentes.",
            "DE" to "Du hast den ersten Schritt getan, um dir an der Börse einen Namen zu machen. Sammle Kapital durch Minispiele oder kluge Trades.",
            "RU" to "Вы сделали первый шаг к успеху на бирже. Копите стартовый капитал с помощью мини-игр или умных сделок.",
            "ZH" to "你迈出了在交易所成名的第一步。通过小游戏或明智的交易积累资本。",
            "HI" to "आपने एक्सचेंज में नाम बनाने के लिए पहला कदम उठाया है। मिनी-गेम्स या स्मार्ट ट्रेडों के साथ पूंजी जमा करें।",
            "AZ" to "Birjada adınızı duyurmaq üçün ilk addımı atdınız. Mini oyunlar və ya ağıllı ticarətlərlə sermayə toplayın.",
            "FR" to "Vous avez fait le premier pas pour vous faire un nom sur la bourse. Accumulez du capital avec des mini-jeux ou des transactions intelligentes.",
            "TH" to "คุณได้เริ่มก้าวแรกเพื่อสร้างชื่อเสียงในตลาดหุ้น สะสมเงินทุนด้วยมินิเกมหรือการซื้อขายที่ชาญฉลาด"
        ),
        "milestone_2_title" to mapOf(
            "TR" to "Bölüm 2: Amatör Yatırımcı",
            "EN" to "Chapter 2: Amateur Trader",
            "ES" to "Capítulo 2: Operador Amateur",
            "DE" to "Kapitel 2: Amateur-Trader",
            "RU" to "Глава 2: Аматор",
            "ZH" to "第2章：业余交易员",
            "HI" to "अध्याय 2: शौकिया ट्रेडर",
            "AZ" to "Bölüm 2: Həvəskar Ticarətçi",
            "FR" to "Chapitre 2 : Trader Amateur",
            "TH" to "บทที่ 2: เทรดเดอร์สมัครเล่น"
        ),
        "milestone_2_desc" to mapOf(
            "TR" to "Piyasanın acımasız olduğunu fark ettin. İlk kaldıraçlı işlemlerinde likit olmamaya çalış!",
            "EN" to "You realized how ruthless the market is. Try not to get liquidated in your first leverage trades!",
            "ES" to "Te diste cuenta de lo despiadado que es el mercado. ¡Intenta no quedar liquidado en tus primeras operaciones con apalancamiento!",
            "DE" to "Du hast gemerkt, wie unbarmherzig der Markt ist. Versuche, bei deinen ersten Hebel-Trades nicht liquidiert zu werden!",
            "RU" to "Вы поняли, насколько беспощаден рынок. Постарайтесь не ликвидироваться на первых маржинальных сделках!",
            "ZH" to "你意识到市场是多么无情。试着在你的首次杠杆交易中不被爆仓！",
            "HI" to "आपको एहसास हुआ कि बाजार कितना निर्मम है। अपने पहले लेवरेज ट्रेडों में लिक्विडेट होने से बचने की कोशिश करें!",
            "AZ" to "Bazarın nə qədər amansız olduğunu anladınız. İlk kaldıraçlı ticarətlərinizdə likvidasiya olmamağa çalışın!",
            "FR" to "Vous avez réalisé à quel point le marché est impitoyable. Essayez de ne pas être liquidé lors de vos premiers trades à levier !",
            "TH" to "คุณตระหนักดีว่าตลาดโหดร้ายเพียงใด พยายามอย่าโดนล้างพอร์ตในการเทรดแบบใช้เลเวอเรจครั้งแรกของคุณ!"
        ),
        "milestone_3_title" to mapOf(
            "TR" to "Bölüm 3: Balina Avcısı",
            "EN" to "Chapter 3: Whale Hunter",
            "ES" to "Capítulo 3: Cazador de Ballenas",
            "DE" to "Kapitel 3: Walfänger",
            "RU" to "Глава 3: Охотник на китов",
            "ZH" to "第3章：巨鲸捕手",
            "HI" to "अध्याय 3: व्हेल शिकारी",
            "AZ" to "Bölüm 3: Balina Ovçusu",
            "FR" to "Chapitre 3 : Chasseur de Baleines",
            "TH" to "บทที่ 3: นักล่าปลาวาฬ"
        ),
        "milestone_3_desc" to mapOf(
            "TR" to "Artık büyük balıklar seni fark etmeye başladı. Twitter akışındaki uzmanları kopyalayarak güç kazan.",
            "EN" to "Big players are starting to notice you. Gain power by copying experts from the Twitter feed.",
            "ES" to "Los peces gordos están empezando a notarte. Gana poder copiando a los expertos del feed de Twitter.",
            "DE" to "Die großen Fische fangen an, dich zu bemerken. Gewinne an Macht, indem du Experten aus dem Twitter-Feed kopierst.",
            "RU" to "Крупные игроки начинают вас замечать. Набирайте силу, копируя экспертов из Твиттер-ленты.",
            "ZH" to "巨头们开始注意到你了。通过在推特动态上跟单专家来获取力量。",
            "HI" to "बड़े खिलाड़ी आपको नोटिस करने लगे हैं। ट्विटर फ़ीड से विशेषज्ञों की नकल करके शक्ति प्राप्त करें।",
            "AZ" to "Artıq böyük balıqlar sizi fərq etməyə başladı. Twitter axınındakı ekspertləri kopyalayaraq güc qazanın.",
            "FR" to "Les gros poissons commencent à vous remarquer. Gagnez en puissance en copiant les experts du fil Twitter.",
            "TH" to "ผู้เล่นรายใหญ่เริ่มสังเกตเห็นคุณแล้ว เพิ่มความแข็งแกร่งด้วยการเลียนแบบผู้เชี่ยวชาญจากหน้าฟีดทวิตเตอร์"
        ),
        "milestone_4_title" to mapOf(
            "TR" to "Bölüm 4: Borsa Efendisi",
            "EN" to "Chapter 4: Exchange Master",
            "ES" to "Capítulo 4: Maestro de la Bolsa",
            "DE" to "Kapitel 4: Meister der Börse",
            "RU" to "Глава 4: Магистр биржи",
            "ZH" to "第4章：交易大师",
            "HI" to "अध्याय 4: एक्सचेंज Master",
            "AZ" to "Bölüm 4: Birja Əfəndisi",
            "FR" to "Chapitre 4 : Maître de la Bourse",
            "TH" to "บทที่ 4: เจ้าพ่อตลาดหุ้น"
        ),
        "milestone_4_desc" to mapOf(
            "TR" to "Milyonerler kulübüne giriş bileti! Sektörün en büyük 10 yapay zekasını alt etmek için son viraj.",
            "EN" to "Entry ticket to the Millionaires Club! The final stretch to defeat the top 10 AI rivals in the industry.",
            "ES" to "¡Boleto de entrada al Club de Millonarios! El tramo final para derrotar a las 10 mejores IA rivales de la industria.",
            "DE" to "Eintrittskarte in den Club der Millionäre! Die Zielgerade, um die Top-10-KI-Konkurrenten der Branche zu schlagen.",
            "RU" to "Входной билет в клуб миллионеров! Финишная прямая, чтобы победить топ-10 соперников с ИИ.",
            "ZH" to "百万富翁俱乐部的入场券！击败业内前十名AI对手的最后冲刺阶段。",
            "HI" to "करोड़पति क्लब का प्रवेश टिकट! उद्योग के शीर्ष 10 एआई प्रतिद्वंद्वियों को हराने के लिए अंतिम चरण।",
            "AZ" to "Milyonçular klubuna giriş bileti! Sektorun ən böyük 10 süni intellekt rəqibini məğlub etmək üçün son döngə.",
            "FR" to "Billet d'entrée pour le Club des Millionnaires ! La dernière ligne droite pour battre les 10 meilleurs rivaux IA.",
            "TH" to "ตั๋วเข้าคลับเศรษฐี! โค้งสุดท้ายในการเอาชนะคู่แข่ง AI 10 อันดับแรกในวงการ"
        ),
        "milestone_5_title" to mapOf(
            "TR" to "Bölüm 5: Finansal Özgürlük",
            "EN" to "Chapter 5: Financial Freedom",
            "ES" to "Capítulo 5: Libertad Financiera",
            "DE" to "Kapitel 5: Finanzielle Freiheit",
            "RU" to "Глава 5: Финансовая свобода",
            "ZH" to "第5章：财务自由",
            "HI" to "अध्याय 5: वित्तीय स्वतंत्रता",
            "AZ" to "Bölüm 5: Maliyyə Azadlığı",
            "FR" to "Chapitre 5 : Liberté Financière",
            "TH" to "บทที่ 5: อิสรภาพทางการเงิน"
        ),
        "milestone_5_desc" to mapOf(
            "TR" to "Piyasanın efendisi oldun, 200 yapay zekayı dize getirdin ve Margin Call kabusunu sonsuza dek bitirdin!",
            "EN" to "You became the master of the market, brought 200 AI rivals to their knees, and ended the Margin Call nightmare forever!",
            "ES" to "¡Te convertiste en el maestro del mercado, pusiste de rodillas a 200 IA rivales y terminaste con la pesadilla del Margin Call para siempre!",
            "DE" to "Du bist der Meister des Marktes geworden, hast 200 KI-Konkurrenten in die Knie gezwungen und den Margin-Call-Albtraum für immer beendet!",
            "RU" to "Вы стали повелителем рынка, поставили на колени 200 ИИ-соперников и навсегда покончили с кошмаром маржин-колла!",
            "ZH" to "你成为了市场的主宰，让200个AI对手俯首称臣，永远结束了追缴保证金（Margin Call）的噩梦！",
            "HI" to "आप बाजार के स्वामी बन गए, 200 एआई प्रतिद्वंद्वियों को घुटने टेकने पर मजबूर कर दिया, और मार्जिन कॉल के दुःस्वप्น को हमेशा के लिए समाप्त कर दिया!",
            "AZ" to "Bazarın ağası oldunuz, 200 süni intellekt rəqibini dize gətirdiniz və Margin Call kabusunu əbədi olaraq bitirdiniz!",
            "FR" to "Vous êtes devenu le maître du marché, avez mis à genoux 200 rivaux IA et mis fin au cauchemar du Margin Call pour toujours !",
            "TH" to "คุณกลายเป็นเจ้าแห่งตลาด สยบคู่แข่ง AI ทั้ง 200 ราย และยุติฝันร้ายของมาร์จิ้นคอลไปตลอดกาล!"
        ),
        "settings_title" to mapOf(
            "TR" to "Ayarlar",
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
            "TR" to "Tema Modu",
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
            "TR" to "Koyu Tema",
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
            "TR" to "Açık Tema",
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
            "TR" to "Kapat",
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
