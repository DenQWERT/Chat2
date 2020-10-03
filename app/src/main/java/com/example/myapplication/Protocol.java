package com.example.myapplication;

// Протокол передачи данных v2.3.
// Собирает данные в одну строку для передачи клиенту или на сервер.
// А так же разбирает пришедшую строку на компоненты.
// Разработчик Севенков Денис. 8-926-890-36-67.

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Protocol {
    String data;  // Дата и время сообщения ("dd.MM.yyyy HH:mm")
    String name="NoName";  // Имя пользователя
    int idUser=0; // id - номер пользователя отправившего сообщение в БД сервера. По умолчанию 0.
    int color; // Цвет сообщений пользователя
    int type;  // Тип сообщения (регистрация = 01; сообщение клиента на сервер = 02;
    // сообщение сервера клиенту = 03; отключение клиента = 04; запрос на смену цвета клиента = 05;
    // переход в ждущий или спящий режим = 06; подписка на сообщения отдельных пользователей чате = 07;
    // резерв1 = 08; резерв2 = 09; запрос на смену текущего имени пользователя = 10;
    // список подключенных клиентов = 11; запрос на повтор отправки данных = 12;
    String s6=" ";  // Резервный параметр
    String s7=" ";  // Резервный параметр
    String message=" ";// само сообщение клиента
    int controlSumm;  // Контрольная сумма сообщения. Если не совпадает с переданной строкой, происходит запрос на повтор передачи сообщения
    String razdelitel = "/";// - символ разделитель компонентов в строке протокола

    public Protocol() { // - конструктор класса Protocol()

    }

    public String SendStrokaProtocolMessage(){ // - Сборщик строки протокола для отправки получателю
        Date data = new Date();
        this.data= data.toString();
        System.out.println("Date = " + this.data);
        String c = (type + razdelitel + this.idUser + razdelitel + this.data + razdelitel +  name + razdelitel + color + razdelitel + "6" + razdelitel + "7" + razdelitel +message + razdelitel + "45");
        System.out.println(c);
        return (c);
    }

    public int RazborProtocol(String str) throws ParseException { // Разборщик строки протокола на компоненты. Раскладываем входящую строку на отдельные поля Протокола
        String[] protocol = str.split (razdelitel);
        if ((Integer.parseInt(protocol[protocol.length-1]) == 0)&(protocol.length<8)) return -1; // - Не совпала контрольная сумма в конце сообщения
        if (protocol.length>0)  { // - Получаем тип сообщения
            this.type     = Integer.parseInt(protocol[0]);
            //System.out.println("Тип сообщения = " + this.type);
        }
        if (protocol.length>1)  { // - Получаем тип сообщения
            this.idUser     = Integer.parseInt(protocol[1]);
            //System.out.println("Номер пользователя на сервере = " + this.idUser);
        }
        if (protocol.length>2)  {
            String s=protocol[2]; // - Получаем время и дату сообщения
            /*SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern("dd.MM.yyyy HH:mm");
            Date docDate= format.parse(s);
            this.data     = docDate; */
            this.data=s;
            //System.out.println("Время и дата = " + this.data );
        }
        if (protocol.length>3)  { // - Получаем имя клиента написавшего сообщения
            this.name     = protocol[3];
            //System.out.println("Имя = " + this.name);
        }
        if (protocol.length>4)  { // - Получаем цвет клиента написавшего сообщение
            this.color    = Integer.parseInt(protocol[4]);
            //System.out.println("Цвет = " + this.color );
        }
        if (protocol.length>5)  { // - Резерв
            this.s6     = protocol[5];
            //System.out.println("Резерв s6 = " + this.s6);
        }
        if (protocol.length>6)  { // - Резерв
            this.s7     = protocol[6];
            //System.out.println("Резерв s7 = " + this.s7);
        }
        if (protocol.length>7)  { // - Получаем само сообщение клиента
            this.message  = protocol[protocol.length-2];
            //System.out.println("Сообщение = " + this.message);
        }
        if (protocol.length>8)  { // - Получаем само сообщение клиента
            this.controlSumm  = Integer.parseInt(protocol[protocol.length-1]);
            //System.out.println("Контрольная сумма = " + this.controlSumm);
        }
        return 1;
    }

    public static void main(String[] args) throws ParseException {
        System.out.println("=======================================");
        Protocol P = new Protocol();
        String s = "02/14/31.01.2020 12:45/Маша/03/6/7/Message/45";
        System.out.println("Разбираем строку = " + s);
        P.RazborProtocol(s);
        System.out.println("=======================================");
        P.SendStrokaProtocolMessage();
        System.out.println("=======================================");
    }
}
