package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
//import android.telecom.Connection;
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
import android.widget.TextView;
import android.view.Menu;

//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity{
    public Socket socket;
    public DataInputStream in;
    public DataOutputStream out;
    private Connection mConnect = null;

    TextView tvServerChange;
    TextView tvUsers;
    TextView tvMessage;
    EditText etMessage;
    Button sendBtn;
    Button openBtn;
    Button closeBtn;



    static int Sh = 2;  // Sh = 2 - шифруем  Sh=0 не шифруем
    //private String HOST = "10.0.2.2";
    //private String HOST = "35.235.241.19";
    private String HOST = "35.208.16.242";
    //private String HOST = "109.252.5.143";
    private int PORT = 8188;
    // Таблица цветов - https://www.color-hex.com/
    String nameThisClient="NoName";
    int idThisClient=0;
    Protocol P = new Protocol();
    Protocol P1 = new Protocol();
    int i01  = 0;
    int ViewOldMessages=10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUsers = findViewById(R.id.tvUsers);
        tvMessage = findViewById(R.id.tvMessage);
        tvServerChange = findViewById(R.id.serverIpChangeWindow);
        etMessage = findViewById(R.id.etMessage);
        sendBtn = findViewById(R.id.sendBtn);
        openBtn = findViewById(R.id.openBtn);
        closeBtn = findViewById(R.id.closeBtn);

        sendBtn.setEnabled(false);
        closeBtn.setEnabled(false);

        //tvMessage.setBackgroundResource(R.drawable.fonklen);




        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOpenClick();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendClick();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseClick();
            }
        });

        // - Скрываем экранную клавиатуру при запуске приложения :
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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
                tvMessage.setText("Текущй IP-адрес сервера\n" + HOST + ":" + PORT + "\n\nВыбирите в меню Подключиться к чату");
                onOpenClick();
                return true;
            case R.id.connectOneServer: // - Действия при выборе пунка меню "Соединяемся с сервером 35.208.16.242"
                HOST = "35.208.16.242";
                tvMessage.setText("Текущй IP-адрес сервера\n" + HOST + ":" + PORT + "\n\nВыбирите в меню Подключиться к чату");
                onOpenClick();
                return true;
            case R.id.connectVladLenServer: // - Действия при выборе пунка меню "Соединяемся с сервером 45.12.18.246"
                HOST = "45.12.18.246";
                tvMessage.setText("Текущй IP-адрес сервера\n" + HOST + ":" + PORT + "\n\nВыбирите в меню Подключиться к чату");
                onOpenClick();
                return true;
            case R.id.connectTwoServer: // - Действия при выборе пунка меню "Соединяемся с сервером 35.208.16.242"
                String str = etMessage.getText().toString();
                HOST = str;
                tvMessage.setText("Текущй IP-адрес сервера\n" + HOST + ":" + PORT + "\n\n(Новый адрес указывайте внизу ДО выбора данного пункта в меню). \n\nФормат - ЧЧЧ.ЧЧЧ.ЧЧЧ.ЧЧЧ");
                return true;


            case R.id.server_change :
                //  - Действия при выборе пунка меню "Изменить сервер"
                InetAddress iaLocal;
                //   iaLocal = InetAddress.getLocalHost(); // - Получаем текущий адрес клиентского - приложения

                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
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
                //popupWindow.setText("орорло");
                tvMessage.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                tvMessage.setText("Текущй IP-адрес сервера\n" + HOST + ":" + PORT + "\nВыберите новый IP-адрес и порт");
                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });


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
                        .setMessage("Закройте окно!")
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
            case R.id.set_settings:
                // - Действия при выборе пунка меню "Настройки приложения"
                return true;
            case R.id.skin_settings:
                // - Действия при выборе пунка меню "Сменить скин"
                //tvMessage.setBackgroundResource(R.drawable.fon1); // первый вариант
                tvMessage.setBackgroundResource(R.drawable.fonklen); // второй вариант
                //tvMessage.setBackgroundColor(getResources().getColor(R.color.tvBackground)); // второй вариант
                return true;
            case R.id.get_prikol:
                // - Действия при выборе пунка меню "Получить Прикол!"
                //tvMessage.setBackgroundResource(R.drawable.fonklen); // второй вариант
                return true;
            case R.id.getMaxOLdMessages10:
                // - Действия при выборе пунка меню "Показать последние 10 сообщений"
                tvMessage.append("\n Последние 10 сообщений чата:");
                SendTechMessagesClientToServer(13,"10");
                return true;
            case R.id.getMaxOLdMessages20:
                // - Действия при выборе пунка меню "Показать последние20 сообщений"
                tvMessage.append("\n Последние 20 сообщений чата:");
                SendTechMessagesClientToServer(13,"20");
                return true;
            case R.id.getMaxOLdMessages40:
                // - Действия при выборе пунка меню "Показать последние 40 сообщений"
                tvMessage.append("\n Последние 40 сообщений чата:");
                SendTechMessagesClientToServer(13,"40");
                return true;
            case R.id.getMaxOLdMessages80:
                // - Действия при выборе пунка меню "Показать последние 80 сообщений"
                tvMessage.append("\n Последние 80 сообщений чата:");
                SendTechMessagesClientToServer(13,"80");
                return true;
            case R.id.viewMainSocket:
                // - Действия при выборе пунка меню "Показать текущий Сокет"
                //tvMessage.setText("Текущй IP-адрес сервера\n" + HOST + ":" + PORT + "\nВыберите новый IP-адрес и порт");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
//============================Метод установки соединения с сервером сообщений ===============================================================
    private void onOpenClick(){
        // Создание подключения
        mConnect = new Connection(HOST, PORT);
        tvMessage.setMovementMethod(new ScrollingMovementMethod());
        tvMessage.setTextColor(Color.RED); // - установим красный текст в окне сообщений


        // Открытие сокета в отдельном потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mConnect.openConnection();
                    in = new DataInputStream(mConnect.getSocket().getInputStream());
                    out = new DataOutputStream(mConnect.getSocket().getOutputStream());
                    //String userName = in.readUTF();
                    //String userName = Pack.unpaked(in.readUTF(),Sh);
                    System.out.println(mConnect.getSocket());
                    P.RazborProtocol(Pack.unpaked(in.readUTF(),Sh)); // - Читаем строку, расшифровываем и разделяем на части.

                    idThisClient = P.idUser;
                    String userName =  P.message;
                    System.out.println("Принят сигнал от сервера =" + P.name + ":Id=" + P.idUser + " Data=" +P.data + " Message=" + P.message);
                    final String str = userName;
                    //tvMessage.setText("\n" + "Установлено соединение Socket=" + Socket);
                    // Разблокирование кнопок в UI потоке
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            tvMessage.setText("\n" + str);
                            sendBtn.setEnabled(true);
                            closeBtn.setEnabled(true);
                        }
                    });

                    while (!mConnect.getSocket().isClosed()) {
                        String response = null;
                        try {
                            //response = in.readUTF();
                            ///response = Pack.unpaked(in.readUTF(),Sh);
                            //Protocol P1 = new Protocol();
                            //System.out.println("До разбора P="  + P1.idUser + " " + P1.message.toString());
                            P1.RazborProtocol(Pack.unpaked(in.readUTF(),Sh));
                            i01++;
                            System.out.println(i01 + " После разбора P=" + P1.idUser + " " + P1.message.toString());
                            /*String names = "Маша, Саша]##МашаСаша"; //response.substring(1);
                            final String[] responseArray = names.split("]##");
                            String[] allUsers = responseArray[0].split(",");
                            //String[] allUsers = "Маша Саша";
                            final StringBuilder sb = new StringBuilder();
                            for (String allUser : allUsers) {
                                String name = allUser.trim();
                                sb.append(name).append("\n");
                            }*/

// ================================   Выводим клиенту сообщения на экран =======================================
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int colorN=0;
                                    if (P1.idUser%3==0) colorN = Color.RED;
                                    if (P1.idUser%3==1) colorN = Color.BLUE;
                                    if (P1.idUser%3==2) colorN = Color.MAGENTA;
                                    //if (P1.idUser%6==3) colorN = Color.BLACK;
                                    //if (P1.idUser%6==4) colorN = Color.BLUE;
                                    //if (P1.idUser%6==5) colorN = Color.GREEN;

                                    //tvUsers.setText(sb.toString());
                                    //tvMessage.append("\n" + responseArray[1]);
                                    tvUsers.setText(P1.name);

                                    String time = P1.data.substring(P1.data.indexOf(':') - 2, P1.data.indexOf(':')+3); // - Выбираем из времени часы и минуты в строковом виде

                                    Spannable wordOne = new SpannableString("\n" + time + " " + P1.name);
                                    wordOne.setSpan(new ForegroundColorSpan(colorN), 0, wordOne.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    tvMessage.append(wordOne);
                                    Spannable wordTwo = new SpannableString(/*"-№" + P1.idUser + */" " + P1.message);
                                    wordTwo.setSpan(new ForegroundColorSpan(Color.RED), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    tvMessage.append(wordTwo);
                                    //tvMessage.append("\n" + P1.name + " Кл.№" + P1.idUser + " " + P1.message + " i01=" + i01);

                                    ///tvMessage.append("\n" + P1.name + " Кл.№" + P1.idUser + " " + P1.message + " i01=" + i01);
                                }
                            });

                        } catch (IOException exception) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvMessage.setText("\n" + "Соединение с сервером чата отсутствует!");
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
                    nameThisClient = str;  // - Получаем с окна имя клиента при регистрации
                    try {
                        //out.writeUTF(str);
                        Date data = new Date();
                        data.getTime();

                        System.out.println("Время и дата = " + data );
                        out.writeUTF(Pack.paked("02/"+ idThisClient + "/" + data.toString() +"/" + nameThisClient+"/1/6/7/" + str + "/41", Sh));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etMessage.setText("");
                                etMessage.requestFocus();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(Connection.LOG_TAG, e.getMessage());
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
                    if (typeMessage==13) {  // - Запрос(Тип=13) на выдачу предыдущих № сообщений с сервера клиенту в окно чата.
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
        // Блокирование кнопок
        sendBtn .setEnabled(false);
        closeBtn.setEnabled(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMessage.setText("");
                tvUsers.setText("");
            }
        });
        Log.d(Connection.LOG_TAG, "Соединение закрыто");
    }

}

