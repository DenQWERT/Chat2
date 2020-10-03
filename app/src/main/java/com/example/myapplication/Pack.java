package com.example.myapplication;

//==== Данная программа пакует реверсом === Vers 4.0 (добавляем мусор + реверсируем +
// + накопительно учитываем ранее распакованные символы по номеру в алфавите
// + учитываем номер шифруемого символа с начала строки по порядку)
//Запаковать - String spack = Pack.paked(teststring, 2); где teststring = что зашифровать
//Распаковать - String unpack = Pack.unpaked(spack, 2); где spack - что расшифровать
// Дополнительный параметр 2 - шифровать  0 - не шифровать
// Разработчик - Савенков Денис 8-926-890-36-67

import java.util.Arrays;

public class Pack {  // ==== Данная программа пакует реверсом ===
    protected static String ABC = "!\"#$%&'()*+,-./:♂♀;<=>?[\\]^_`{|}~ @№╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀—0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    static char[] charABC = new char[ABC.length()];
    protected static String musor = "♀♀7♂";

    public static void Pack() {
        for (int i = 0; i < ABC.length(); i++) {
            charABC[i] = ABC.charAt(i);
            ///System.out.println("i = " + i + " charABC[i] =" + charABC[i] + "   ABC.charAt(i) = " + ABC.charAt(i));
            /*if (ABC.indexOf(charABC[i]) != ABC.lastIndexOf(charABC[i]))
            System.out.println("Внимание!!! Опасность!!! Двойное вхождение символа в алфавит " + charABC[i]);*/
        }
    }

    protected static String PlusMusor (String stroka){
        StringBuffer sb = new StringBuffer(stroka);
        int CountMusor = (int) (Math.random() * 10);
        if (CountMusor<3) CountMusor=3;
        int[] MusorPosition = new int[]{ 1,0,0,0,0,0,0,0,0,0};
        //System.out.print("Места для мусора = ");
        for ( int i=0 ; (i<(CountMusor)) ; i++ ) {
            MusorPosition[i] = (int) (1 + Math.random() * sb.length() );
            //System.out.print(MusorPosition[i]+" ");
        }
        Arrays.sort(MusorPosition);
        ///System.out.print("\nМеста для мусора отсортированные = ");
        for ( int i=1 ; (i<10) ; i++ ) {
            //System.out.print(MusorPosition[i]+" ");
        }
        //System.out.println("\nДлинна исходной строки =" + sb.length());
        for (int i=(MusorPosition.length-1); i>0; i--){
            //if (MusorPosition[i]!=0)
            {sb.insert(MusorPosition[i],musor);}  // -
            // Добавляем в строку всякий мусор musor="#^|♂" до от 3 до 13 раз в произвольное место строки
        }
        stroka = sb.toString();
        ///System.out.println("\nСтрока с мусором        =" + stroka + "   Длина= " + stroka.length());
        return stroka;
    }

    protected static String MinusMusor (String stroka){
        ///System.out.println("Убираем мусор из строки=" + stroka + "   Длина= " + stroka.length());
        while  (stroka.indexOf(musor)!=(-1)){
            stroka = stroka.replaceAll(musor,"");
        }
        ///System.out.println("Убрали мусор из строки =" + stroka + "   Длина= " + stroka.length());
        return stroka;
    }

    public static String paked(String stroka, int classpack) {
        if (classpack == 0) return stroka;   // - Шифрование не производится если classpack = 0
        stroka=Pack.PlusMusor(stroka);
        //System.out.println("Запаковываем строку     =" + stroka + " / Длина строки=" + stroka.length());
        String Temp="";
        //int N = stroka.length();
        char c;
        int MegaNomer=0;
        for (int i = 0; i < stroka.length(); i++) {
            //System.out.print("\ni="+i);
            c = stroka.charAt(i); // - выбираем из строки текущий символ с номером i
            //System.out.print("\ni=" + i + "   с =" + c );
            int n = ABC.indexOf(c); // - получаем номер шифруемой буквы по нашему алфавиту ABC
            MegaNomer=MegaNomer+(n+1)+i; // - сумарный номер запакованных символов по алфавиту ABC (1-185)
            int ABCwidth = ABC.length(); // - Длина алфавита 185
            //c = ABC.charAt(ABCwidth - n); // - шифруем символ (инвертируем символ по длине алфавита)
            while ((MegaNomer)>(ABCwidth)) {MegaNomer-=ABCwidth;}  // - вычитаем полное число алфавитов.
            c = ABC.charAt((ABCwidth - MegaNomer)-1+1); // - циклически просматриваем буквы алфавита N раз справа налево
            //System.out.print("   Упаковали в с = "+ c);
            //c = ABC.charAt(ABCwidth - (MegaNomer-ABCwidth*((MegaNomer)/ABCwidth))); // - циклически просматриваем буквы алфавита N раз справа налево
            Temp += c;  // - складываем зашифрованные символы в строку
            //System.out.println(Temp + " i=" + i);
        }
        ///System.out.println("Закрываем Packed       =" + Temp + "   Длина= " + Temp.length());
        return Temp;
    }

    public static String unpaked(String stroka, int classpack) {
        if (classpack == 0) return stroka;   // - Дешифрование не производится если classpack = 0
        String Temp="";
        char c;
        int MegaNomer=0;
        for (int i = 0; i < stroka.length(); i++) {
            c = stroka.charAt(i); // - выбираем из строки текущий символ с номером i
            int n = ABC.indexOf(c); // - получаем номер расшифруемой буквы по нашему алфавиту ABC
            if (n==-1)  System.out.println("Неопознанный символ"+ c);
            //MegaNomer+=n; // - сумарный номер запакованных символов по алфавиту ABC
            int ABCwidth = ABC.length();//-1;
            if (i==0) {
                c = ABC.charAt((ABCwidth  - n)-1); // - получаем расшифрованный символ с
                MegaNomer+=(ABC.indexOf(c)+1+i);
            }
            else {
                //c = ABC.charAt(ABCwidth - (MegaNomer-ABCwidth*(MegaNomer/ABCwidth))); // - циклически просматриваем буквы алфавита N раз справа налево
                while ((MegaNomer+n+1+i)>ABCwidth) {MegaNomer-=ABCwidth;}  // - вычитаем полное число алфавитов.
                int nomer = (ABCwidth - (n+MegaNomer+i) -1 );
                c = ABC.charAt(nomer); // - циклически просматриваем буквы алфавита N раз справа налево
                MegaNomer+=(ABC.indexOf(c)+1+i);
            }
            //c = ABC.charAt(ABCwidth - (MegaNomer-ABCwidth*(MegaNomer/ABCwidth))); // - циклически просматриваем буквы алфавита N раз справа налево
            //c = ABC.charAt(ABCwidth - (MegaNomer       -ABCwidth*(MegaNomer/ABCwidth))); // - циклически просматриваем буквы алфавита N раз справа налево
            Temp += c;  // - складываем зашифрованные символы в строку
        }
        //System.out.println("Расп. строка с мусором =" + Temp + "   Длина= " + Temp.length());
        Temp=Pack.MinusMusor(Temp);
        return Temp;
    }


 /*   public static void main(String[] args) {
        Pack();

        ///System.out.println("номер ! = " + ABC.indexOf("!") + "   " + ABC.charAt(0));
 /*       for (int i = 0; i < Pack.ABC.length() ; i++)
            System.out.println("Символ " + i + " = " + Pack.ABC.charAt(i) + " charABC(i)= " + charABC[i]);
        System.out.println("Длина Pack.ABC.length(), равна = " + Pack.ABC.length());
///
        //System.out.println("//=========================================================================================");
        ///System.out.println("Длинна алфавита = " + ABC.length() + "    Последний символ = " + charABC[ABC.length()-1]);
        ///String teststring = "!!!!!яяяяя01234567890! Вышел зайчик погулять и попал под Enter. Остались от зайчика только Contrl и Ушки! Вышел зайчик погулять и попал под Enter. Остались от зайчика только Contrl и Ушки!";
        //String teststring = "!!!0123456!!!яяя!!!яяя1234567890 Вышел зайчик погулять и попал под Enter. только Contrl и Ушки!";
        String teststring = "zzzzzzzzzzzzzzzzzzzzzzz Замучила меня эта программа чисто конкретно!!! ";
        System.out.println("Эту строку мы пакуем   =" + teststring + "   Длина= " + teststring.length());
        String spack = Pack.paked(teststring, 2);
        //System.out.println("//=========================================================================================");
        System.out.println("Запакованная строка    =" + spack + "   Длина= " + spack.length());
        //System.out.println("//==========================================================================================");
        String unpack = Pack.unpaked(spack, 2);
        System.out.println("Распаковання строка    =" + unpack + "   Длина= " + unpack.length());
        //System.out.println("//==========================================================================================");
        /*String s = Pack.paked("123",1);
        System.out.println(Pack.ABC);
        System.out.println("Привет!" + s);
    }*/
}
