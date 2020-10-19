package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.Menu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity{
    public Socket socket;
    public DataInputStream in;
    public DataOutputStream out;
    private Connection mConnect = null;
    private SoundPool soundNewMessage; // - Звук по приходу нового сообщения пользователю

    TextView tvServerChange;
    TextView tvUsers;
    TextView tvMessage;
    EditText etMessage;
    Button sendBtn;
    ///Button openBtn;
    ///Button closeBtn;

    static int Sh = 2;  // Sh = 2 - шифруем  Sh=0 не шифруем
    //private String HOST = "10.0.2.2";
    //private String HOST = "35.235.241.19";
    private String HOST = "35.208.16.242";
    //private String HOST = "109.252.5.143";
    private int PORT = 8188;
    // Таблица цветов - https://www.color-hex.com/
    String nameThisClient="NoName";
    int idThisClient=0;
    Protocol P = new Protocol(); //  - Данные пользователя в формате протокола
    Protocol P1 = new Protocol(); // - Данные текущего входящего сообщения в формате протокола
    int i01  = 0;
    String newMessage=""; // - переменная для временной фиксации нового входящего сообщения
    String oldMessage="";
    int iColor=0;
    static int connectOn = 0; //  - признак наличия = 1 или отсутствия = 0 соединения без разрыва сокета
    static String oldTextString="Добро пожаловать в магический Чат!";
    //String[] oldText;
    static ArrayList<String> oldText = new ArrayList<>(); // - Здесь хранятся старые сообщения из окна сообщений которые отображаются после переворота экрана.


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString("messages", oldTextString);
        outState.putStringArrayList("messages", oldText);
        outState.putString("OLDHOST", HOST);
        connectOn = 3; outState.putInt("ConnectOn",connectOn); // - признак закрытия соединения без разрыва сокета
        //System.out.println("Соханены следующие данные окна сообщений = " + oldTextString);
        System.out.println("Соханены следующие данные окна сообщений = " + oldText);
        onCloseClick();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //oldTextString = savedInstanceState.getString("messages");
        oldText = savedInstanceState.getStringArrayList("messages");
        HOST = savedInstanceState.getString("OLDHOST");
        // connectOn = savedInstanceState.getInt ("ConnectOn",connectOn);
        connectOn = 3;
        //System.out.println("Восстановлены - !!! - следующие данные окна сообщений = " + oldTextString);
        System.out.println("Восстановлены - !!! - следующие данные окна сообщений = " + oldText);
        sendBtn.setEnabled(true);
        onOpenClick(); // -  Сюда miniOpenClick надо сделать без повторной авторизации на сервере и
        // установки нового сокета если старый Сокет еще не разорван
        // Возможно передать Сокет в ИнстантСтейт чтобы проверить разорвался он уже или еще нет.
        // Либо в уже имеющийся ОпенКлик передать параметр сокращающий его функционал
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // !!!!!!!!!!!!!!!!!!!!! ПРИ ПОВОРОТЕ ЭКРАНА ПОИСХОДИТ РАЗРЫВ ЧАСТИЧНЫЙ СОЕДИНЕНИЯ !!!!!!!!!!!!!!!!!!!!!!
        // Строкой ниже устанавливается запрет на поворот экрана.
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // - Запрещаем поворот экрана в горизонтальный режим
        setContentView(R.layout.activity_main);
        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setMovementMethod(new ScrollingMovementMethod()); // - устанавливаем скроллинг в окно с сообщениями

        tvServerChange = findViewById(R.id.serverIpChangeWindow);
        etMessage = findViewById(R.id.etMessage);
        //oldText.add("Добро пожаловать в магический Чат!\n\nВыберите подключение к серверу чата в правом верхнем меню :");
        sendBtn = findViewById(R.id.sendBtn);

        ////sendBtn.setEnabled(false);

        //tvMessage.setText(oldTextString);
        // ========================================   Выводим сохраненные сообщения на экран =============================
        tvMessage.setText("");
        tvMessage.append("Добро пожаловать в Чат!\n\nВыберите справа сверху в меню подключение к серверу вашего чата");
        tvMessage.setTextColor(Color.RED);
        int colorN=0;
        for(String onestroka : oldText){
            try {  P1.RazborProtocol(onestroka); }
                catch (ParseException e) {  e.printStackTrace();   }
//- Как скролить текст вниз много методов - http://www.ohandroid.com/textview-android-x43.html
            colorN = whaitColor(P1.idUser);
            String time = P1.data.substring(P1.data.indexOf(':') - 2, P1.data.indexOf(':') + 3); // - Выбираем из времени часы и минуты в строковом виде
            Spannable wordOne = new SpannableString("\n" + time + " " + P1.name);
            wordOne.setSpan(new ForegroundColorSpan(colorN), 0, wordOne.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvMessage.append(wordOne);// - Выводим покрашенную в свой цвет часть текста на экран пользователю
            Spannable wordTwo = new SpannableString(" " + P1.message);
            wordTwo.setSpan(new ForegroundColorSpan(Color.RED), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvMessage.append(wordTwo);// - Выводим покрашенную в другой цвет часть текста на экран пользователю
            // Скролим текст вниз:
            Editable editable = tvMessage.getEditableText(); Selection.setSelection(editable, editable.length());
            //tvMessage.append("\n" + onestroka);
        }
        /*// http://www.ohandroid.com/textview-android.html - взято здесь, типа скролит текствью в конец
            mTextStatus = (TextView) findViewById(R.id.TEXT_STATUS_ID);
        mScrollView = (ScrollView) findViewById(R.id.SCROLLER_ID);
        private void scrollToBottom() { mScrollView.post(new Runnable()
            { public void run() { mScrollView.smoothScrollTo(0, mTextStatus.getBottom()); } }); }*/
        //===================================================================================================================

        //tvMessage.setBackgroundResource(R.drawable.fonklen);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendClick();
            }
        });

        //soundNewMessage = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundNewMessage = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();

        //soundNewMessage.play(1, 1, 1, 0, 1, 1);
        //int play(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate)
        //soundID - идентификатор звука (который вернул load())
        //leftVolume - уровень громкости для левого канала (от 0.0 до 1.0)
        //rightVolume - уровень громкости для правого канала (от 0.0 до 1.0)
        //priority - приоритет потока (0 - самый низкий)
        //loop - количество повторов (0 - без повторов, (-1) - зациклен)
        //rate - скорость воспроизведения (от 0.5 до 2.0)

        // - Скрываем экранную клавиатуру при запуске приложения :
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //InputMethodManager inputManager = (InputMethodManager) context. getSystemService(Context.INPUT_METHOD_SERVICE);
        //inputManager.hideSoftInputFromWindow( this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
//============================= Меню ===============================================================================
    @Override // - создаем визуальный элемент меню в виде трех точк справа сверху на активности.
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override // - Метод обработки пунктов меню.
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.connectLocal: // - Действия при выборе пунка меню "Соединяемся с локальным Андроид-Сервером 10.0.2.2"
                HOST = "10.0.2.2";
                tvMessage.append("\n\nТекущй IP-адрес сервера изменен на \n" + HOST + ":" + PORT/* + "\n\nВыбирите в меню Подключиться к чату"*/);
                onOpenClick();
                return true;
            case R.id.connectOneServer: // - Действия при выборе пунка меню "Соединяемся с сервером 35.208.16.242"
                HOST = "35.208.16.242";
                //tvMessage.setText("Текущй IP-адрес сервера\n" + HOST + ":" + PORT + "\n\nВыбирите в меню Подключиться к чату");
                onOpenClick();
                return true;
            case R.id.connectVladLenServer: // - Действия при выборе пунка меню "Соединяемся с сервером 45.12.18.246"
                HOST = "45.12.18.246";
                //tvMessage.setText("Текущй IP-адрес сервера\n" + HOST + ":" + PORT + "\n\nВыбирите в меню Подключиться к чату");
                onOpenClick();
                return true;
            case R.id.connectTwoServer: // - Действия при выборе пунка меню "Соединяемся с сервером XXX"
                String str = etMessage.getText().toString();
                HOST = str;
                tvMessage.append("\nТекущй IP-адрес сервера\n" + HOST + ":" + PORT + "\n\n(Новый адрес указывайте внизу ДО выбора данного пункта в меню). \n\nФормат - ЧЧЧ.ЧЧЧ.ЧЧЧ.ЧЧЧ");
                return true;
            case R.id.server_change :
                //  - Действия при выборе пунка меню "Изменить сервер"
                InetAddress iaLocal;
                //   iaLocal = InetAddress.getLocalHost(); // - Получаем текущий адрес клиентского - приложения

                // inflate the layout of the popup window
                /*LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.server_change, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(tvMessage, Gravity.CENTER, 0, 0);
                //popupWindow.setText("орорло");*/
                tvMessage.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                tvMessage.append("\n\nТекущй IP-адрес сервера\n" + HOST + ":" + PORT/* + "\nВыберите новый IP-адрес и порт"*/);
                // dismiss the popup window when touched
                /*popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });*/


                return true;
            case R.id.server_connect: // - Действия при выборе пунка меню "Соединиться с сервером"
                onOpenClick();
                return true;
            case R.id.server_disconnect: // - Действия при выборе пунка меню "Отключиться от сервера"
                onCloseClick();
                return true;
            case R.id.users_subscribe_settings: // - Действия при выборе пунка меню "Подписаться на пользователей"
                return true;
            case R.id.self_name_change:
                // - Действия при выборе пунка меню "Изменить свое имя в чате"
                // - Диалоговое окно. Честно стырено отсюда - https://randroid.ru/dev/android-studio-dialogovoe-okno-alertdialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Важное сообщение!")
                        .setMessage("Закройте окно. Произошла аварийная разгерметизация!")
                        //.setIcon(R.drawable.server_change)
                        .setCancelable(false)
                        .setNegativeButton("ОК.  ВЫХОЖУ В КОСМОС!",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case R.id.viewAllUsers:
                // - Действия при выборе пунка меню "Список кто сегодня был в чате"
                SendTechMessagesClientToServer(14,"Сервер, покажи всех кто был в чате!");
                return true;
            case R.id.skin_settings:
                // - Действия при выборе пунка меню "Сменить скин"
                //tvMessage.setBackgroundResource(R.drawable.fon1); // первый вариант
                // Цвета взяты здеть. Первые две FF добавляем слева автоматом.
                if (iColor%10==0)  tvMessage.setBackgroundColor(Color.YELLOW); // Желтый фон в поле сообщений
                if (iColor%10==1)  tvMessage.setBackgroundColor(Color.LTGRAY); // Серый фон экрана
                if (iColor%10==2)  tvMessage.setBackgroundColor(0xFF7FFFD4); // Бирюзовый фон экрана в поле сообщений
                if (iColor%10==3)  tvMessage.setBackgroundColor(Color.WHITE); // Белый фон экрана в поле сообщений
                if (iColor%10==4)  tvMessage.setBackgroundColor(0xFFFFCCFF); // Красноватый фон экрана в поле сообщений
                if (iColor%10==5)  tvMessage.setBackgroundColor(0xFF99FF99); // Зеленоватый фон экрана в поле сообщений
                if (iColor%10==6)  tvMessage.setBackgroundColor(0xFFFFCC66); // Оранжевый фон экрана в поле сообщений
                if (iColor%10==7)  tvMessage.setBackgroundColor(0xFF00CCFF); // Голубоватый фон экрана в поле сообщений
                if (iColor%10==8)  tvMessage.setBackgroundColor(0xFFFCC99); // Светло малиноватый фон экрана в поле сообщений
                if (iColor%10==9)  tvMessage.setBackgroundResource(R.drawable.fonklen); // С кленовыми листьями
                iColor++;
                //tvMessage.setBackgroundResource(R.drawable.fonklen); // второй вариант
                //tvMessage.setBackgroundColor(getResources().getColor(R.color.tvBackground)); // второй вариант
                return true;
            case R.id.get_users:
                // - Действия при выборе пунка меню "Показать кто сейчас в чате"
                System.out.println("Запрашиваем список кто сейчас в чате");
                SendTechMessagesClientToServer(11,"Сервер, покажи кто сейчас в чате");
                tvMessage.append("\n-------------------------- ");
                return true;
            case R.id.getMaxOLdMessages10:
                // - Действия при выборе пунка меню "Показать последние 10 сообщений"
                tvMessage.append("\n---------------------------------\nПоследние 10 сообщений чата:");
                SendTechMessagesClientToServer(13,"10");
                return true;
            case R.id.getMaxOLdMessages20:
                // - Действия при выборе пунка меню "Показать последние20 сообщений"
                tvMessage.append("\n---------------------------------\nПоследние 20 сообщений чата:");
                SendTechMessagesClientToServer(13,"20");
                return true;
            case R.id.getMaxOLdMessages50:
                // - Действия при выборе пунка меню "Показать последние 40 сообщений"
                tvMessage.append("\n---------------------------------\nПоследние 50 сообщений чата:");
                SendTechMessagesClientToServer(13,"50");
                return true;
            case R.id.getMaxOLdMessages100:
                // - Действия при выборе пунка меню "Показать последние 80 сообщений"
                tvMessage.append("\n---------------------------------\nПоследние 100 сообщений чата:");
                SendTechMessagesClientToServer(13,"100");
                return true;
            case R.id.viewMainSocket:
                // - Действия при выборе пунка меню "Показать текущий Сокет"
                tvMessage.append("\n Текущий Сокет = " + mConnect.getSocket().toString());
                //tvMessage.setText("Текущй IP-адрес сервера\n" + HOST + ":" + PORT + "\nВыберите новый IP-адрес и порт");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
//============================Метод установки соединения с сервером сообщений ===============================================================
    private void onOpenClick()  { // throws IOException, ClassNotFoundException
        //if (connectOn == 1 ) {tvMessage.append("\n\n ConnectOn = 1 , Текущий Сокет = "/* + mConnect.getSocket().toString()*/);}
        //if (connectOn == 3 ) {tvMessage.append("\n\n ConnectOn = 3 , Текущий Сокет = "/* + mConnect.getSocket().toString()*/);}
        if (0 == 0) {
            // Создание подключения
            mConnect = new Connection(HOST, PORT);
            //tvMessage.setMovementMethod(new ScrollingMovementMethod()); // - устанавливаем скроллинг в окно с сообщениями
            //etMessage.setMovementMethod(new ScrollingMovementMethod());
            tvMessage.setTextColor(Color.RED); // - установим красный текст в окне сообщений
            //tvMessage.append("\n\n ConnectOn = 0 " + connectOn);
            //connectOn = 1;
        }

        // Открытие сокета в отдельном потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //if ((1 == 1) || (connectOn == 3)) {
                        mConnect.openConnection();
                        //connectOn = 1;
                        //outState.putString("ConnectOn", connectOn);

                        in = new DataInputStream(mConnect.getSocket().getInputStream());
                        out = new DataOutputStream(mConnect.getSocket().getOutputStream());
                        //tvMessage.append("\n Текущий " + mConnect.getSocket().toString());
                        //String userName = in.readUTF();
                        //String userName = Pack.unpaked(in.readUTF(),Sh);
                        System.out.println("Текущий сокет - !!! - " + mConnect.getSocket());
                        P.RazborProtocol(Pack.unpaked(in.readUTF(), Sh)); // - Читаем строку, расшифровываем и разделяем на части.
                        //P.name = P.name.replace(",","");// - убираем запятые из имени пользователя
                        idThisClient = P.idUser;
                        String userName = P.message;

                        //userName = userName.replace(",","");// - убираем запятые из имени пользователя
                        System.out.println("Принят сигнал от сервера =" + P.name + ":Id=" + P.idUser + " Data=" + P.data + " Message=" + P.message);
                        final String str = userName;
                        //tvMessage.append("\nВаше имя в чате - " + userName);

                        //tvMessage.append("\n" + "Установлено соединение c чатом на сервере " + HOST);
                        // Разблокирование кнопок в UI потоке
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessage.setText("\n" + str);
                                sendBtn.setEnabled(true);
                                ///closeBtn.setEnabled(true);
                            }
                        });

                        //tvMessage.setText(""); //  - Обнуляем сообщения на экране сообщений чата
                    //}
                    while (!mConnect.getSocket().isClosed()) {
                        String response = null;
                        try {
                            //P1.RazborProtocol(Pack.unpaked(in.readUTF(),Sh));
                            newMessage = Pack.unpaked(in.readUTF(),Sh);
                            P1.RazborProtocol(newMessage);
                            oldText.add(newMessage);
                            i01++;
                            System.out.println(i01 + "= i01 После разбора P=" + P1.idUser + " " + P1.message.toString());
                            //Protocol P3 = new Protocol();
                            //P3=P1;

// ================================   Выводим клиенту сообщения на экран =======================================
                            //if (oldMessage.equals(P1.message)==false) {
                            //if (P1.message.toString()!="")&&(oldMessage.toString()!=P1.message.toString()) {
                            final CountDownLatch latch = new CountDownLatch(1);

                            runOnUiThread(new Runnable() {
                                    // - ожидание конца выполнения метода - https://ru.stackoverflow.com/questions/555309/java-%D0%B4%D0%BE%D0%B6%D0%B4%D0%B0%D1%82%D1%8C%D1%81%D1%8F-%D0%B7%D0%B0%D0%B2%D0%B5%D1%80%D1%88%D0%B5%D0%BD%D0%B8%D1%8F-runnable
                                    @Override
                                    public void run() {
                                        int colorN = 0; // - Выбираем цвет начала сообщения в окне чата пользователя по умолчанию
                                        switch (P1.type) { //=== -  Разборщик сообщений сервера ===================================
                                            case 14: // - Выдаем клиенту список всех кто был в чате
                                                /*String[] subStr;
                                                String delimeter = ", "; // Разделитель
                                                subStr = str.split(delimeter); // Разделения строки str с помощью метода split()
                                                // Вывод результата на экран
                                                for(int i = 0; i < subStr.length; i++) {
                                                    System.out.println(subStr[i]);
                                                }*/
                                                String strktobyl = P1.message;
                                                String Name = "NoName";
                                                String id = "NoId";
                                                colorN = Color.BLUE;
                                                tvMessage.append("\n-------------------------- ");
                                                tvMessage.append("\nСписок всех кто был в чате: ");

                                                while (strktobyl.indexOf("-#-") >= 0){
                                                    //System.out.println("strktobyl 1 = " + strktobyl);
                                                    //System.out.println("strktobyl.indexOf(\"-#-\") = " + strktobyl.indexOf("-#-"));

                                                    id = strktobyl.substring( strktobyl.indexOf("-#-")-3, strktobyl.indexOf("-#-"));
                                                    id = id.replace(" ","");
                                                    //System.out.println("id = " + id);

                                                    Name = strktobyl.substring(strktobyl.indexOf("-#-")+3,strktobyl.indexOf(","));
                                                    //System.out.println("Name = " + Name);

                                                    colorN = whaitColor(Integer.parseInt(id));

                                                    strktobyl = strktobyl.substring(strktobyl.indexOf("-#-")+3, strktobyl.length());
                                                    strktobyl = strktobyl.substring(strktobyl.indexOf(",")+1, strktobyl.length());
                                                    //System.out.println("strktobyl 2 = " + strktobyl);

                                                    Spannable wordOne = new SpannableString(/*" " + id + "-" + */" " + Name);
                                                    wordOne.setSpan(new ForegroundColorSpan(colorN), 0, wordOne.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                    tvMessage.append(wordOne);// - Выводим покрашенную в свой цвет часть текста на экран пользователю

                                                    if ((strktobyl.indexOf("-#-")>=0)) { // - ставим точки и зяпятые между пользователями в списке
                                                        tvMessage.append(",");}
                                                    else {tvMessage.append("."); }
                                                };

                                                break;
                                            case 2: case 3: case 11: case 13:
                                                // - Выбираем цвет начала сообщения в окне чата пользователя
                                                /*if (P1.idUser % 6 == 5) colorN = Color.RED;
                                                if (P1.idUser % 6 == 4) colorN = Color.BLUE;
                                                if (P1.idUser % 6 == 3) colorN = Color.MAGENTA;
                                                if (P1.idUser % 6 == 2) colorN = 0xFF9900FF;// Малиновый 0xFF9900FF
                                                if (P1.idUser % 6 == 1) colorN = 0xFF660033;// T-красный 0xFF660033
                                                if (P1.idUser % 6 == 0) colorN = 0xFF003300;*/// Т-Зеленый https://ege-ok.ru/wp-content/uploads/2015/06/a43.png
                                                // https://htmlweb.ru/html/table_colors.php

                                                colorN = whaitColor(P1.idUser);

                                                // - Выводим в окно чата пользователя новое полученное сообщение
                                                String time = P1.data.substring(P1.data.indexOf(':') - 2, P1.data.indexOf(':') + 3); // - Выбираем из времени часы и минуты в строковом виде
                                                Spannable wordOne = new SpannableString("\n" + time + " " + P1.name);
                                                wordOne.setSpan(new ForegroundColorSpan(colorN), 0, wordOne.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                tvMessage.append(wordOne);// - Выводим покрашенную в свой цвет часть текста на экран пользователю
                                                Spannable wordTwo = new SpannableString(/*"-№" + P1.idUser + */" " + P1.message);
                                                wordTwo.setSpan(new ForegroundColorSpan(Color.RED), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                tvMessage.append(wordTwo);// - Выводим покрашенную в другой цвет часть текста на экран пользователю
                                                //tvMessage.append("\n" + P1.name + " Кл.№" + P1.idUser + " " + P1.message + " i01=" + i01);

                                                // Скролим текст вниз!!!
                                                Editable editable = tvMessage.getEditableText();
                                                Selection.setSelection(editable, editable.length());

                                                oldMessage = P1.message;
                                                //oldTextString = oldTextString.concat("/n"+ time + " " + P1.name + " " + P1.message);
                                                //oldText.add(time + " " + P1.name + " " + P1.message);
                                                //System.out.println("oldTextString = " + oldTextString );
                                                System.out.println("oldText = " + oldText);
                                                //TimeUnit.MILLISECONDS.sleep(100);

                                                /*ObjectOutputStream toFile = null;
                                                    toFile = new ObjectOutputStream(new FileOutputStream(file));
*/
                                                break;
                                        }
                                        latch.countDown();
                                    }
                                });
                            latch.await();// - Ожидаем конца выполнения метода вывода текущей строки на экран чата пользователя
                            //}
                        } catch (IOException exception) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvMessage.append("\n" + "Соединение с сервером чата отсутствует!");
                                    //tvMessage.append("\n" + "Сервер недоступен!");
                                }
                            });

                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    Log.d(Connection.LOG_TAG, "Соединение установлено");
                    Log.d(Connection.LOG_TAG, "(mConnect != null) = " + (mConnect != null));
                } catch (Exception e) {
                    Log.e(Connection.LOG_TAG, e.getMessage());
                    mConnect = null;
                }
            }
        }).start();
    }
// ==================== Обработчик нажатия кнопки Send(Ок) ============================================
    private void onSendClick()
    {
        if (mConnect == null) {
            Log.d(Connection.LOG_TAG, "Соединение не установлено");
        }  else {
            Log.d(Connection.LOG_TAG, "Отправка сообщения");
            new Thread(new Runnable() {
                @Override
                public void run() {

                    String str = etMessage.getText().toString();
                    str = str.replace("/",""); // - убираем возможный слэш из имени пользователя.
                    if (str.length()>0) {
                        nameThisClient = str;  // - Получаем с окна имя клиента при регистрации
                        try {
                            //out.writeUTF(str);
                            Date data = new Date();
                            data.getTime();

                            System.out.println("Время и дата = " + data);
                            out.writeUTF(Pack.paked("02/" + idThisClient + "/" + data.toString() + "/" + nameThisClient + "/1/6/7/" + str + "/41", Sh));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    etMessage.setText("");
                                    //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                                    etMessage.requestFocus();
                                    // - Скрываем экранную клавиатуру при запуске приложения :

                                }
                            });
                        } catch (Exception e) {
                            Log.e(Connection.LOG_TAG, e.getMessage());
                        }
                    }
                }
            }).start();
        }
    }
//==================================== Обработчик отправки на сервер технических сообщений =======================
    private void SendTechMessagesClientToServer(final int typeMessage, final String message)
    {
        if (mConnect == null) {
            Log.d(Connection.LOG_TAG, "Соединение не установлено");
        }  else {
            Log.d(Connection.LOG_TAG, "Отправка сообщения");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if ((typeMessage==13)||(typeMessage==11)||(typeMessage==14)) {
                        // - Запрос(Тип=13) на выдачу предыдущих № сообщений с сервера клиенту в окно чата.
                        // запрос на список кто сейчас в чате = 11;
                        // запрос на список ВСЕХ кто был в чате = 14;
                        String str = message;
                        //nameThisClient = str;  // - Получаем с окна имя клиента при регистрации
                        Date data = new Date();
                        data.getTime();
                        System.out.println("Отправляем техническое сообщение на сервер. Тип сообщения = " + typeMessage + "  Время и дата = " + data + " Само сообщение = " + str);
                        try {
                            out.writeUTF(Pack.paked(typeMessage + "/" + idThisClient + "/" + data.toString() + "/" + nameThisClient + "/1/6/7/" + str + "/77", Sh));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }
    }
//============================== Обработчик нажатия кнопки и выбора элемента меню отключения от сети(от сервера) ==========================
    private void onCloseClick()
    {
        // Закрытие соединения
        mConnect.closeConnection();
        connectOn = 0;
        // Блокирование кнопок
        sendBtn .setEnabled(false);
        ///closeBtn.setEnabled(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMessage.setText("Вы отключились от сервера");
                //tvUsers.setText("");
            }
        });
        Log.d(Connection.LOG_TAG, "Соединение закрыто");
    }

// ====================  - Метод для определения в какой цвет красить клиента по его номеру id ================
    int whaitColor(int id){  // - Всего шесть цветов клиентов в окне сообщений
        int colorN = 0;
        if (id % 6 == 5) colorN = Color.RED;
        if (id % 6 == 4) colorN = Color.BLUE;
        if (id % 6 == 3) colorN = Color.MAGENTA;
        if (id % 6 == 2) colorN = 0xFF9900FF;// Малиновый 0xFF9900FF
        if (id % 6 == 1) colorN = 0xFF660033;// T-красный 0xFF660033
        if (id % 6 == 0) colorN = 0xFF003300;// Т-Зеленый https://ege-ok.ru/wp-content/uploads/2015/06/a43.png
        // https://htmlweb.ru/html/table_colors.php
        return colorN;
    }
//============================================================================================================

}

// - Как установить звук в приложение - http://www.mobilab.ru/androiddev/androidsoundpoolmediaplayer.html