package package_1337;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ZarzadzaniePlikami {
    public static final String ROOT_PATH = "d:\\dane\\chmura\\dropbox\\programowanie\\projekty\\NetBeans\\TimWebAppAndroid\\web\\resources";
    static List<Zadanie> listaZadanOczekujacych = null;
    static Zadanie zadaniePrzetwarzane = null;
    static List<Zadanie> listaZadanUkonczonych = null;
    // TODO folder wyniki tworzony bedzie po zakonczeniu wykonywania serii zdjec
    // TODO program przetwarzajacy bedzie oddzielnym watkiem workerem
    // TODO watek powolywany bedzie jesli nadejdzie zadanie do pustej listy
    // TODO worker usunie element z listy tylko wtedy, kiedy go przetworzy
    // TODO worker bedzie zaczynal pracowac tylko nad zadaniami, ktore maja status "oczekuje"
    // TODO worker moze usunac tylko zadanie, nad ktorym pracowal i je ukonczyl
    // TODO worker zacznajac prace nad zadaniem zamienia jego status na "przetwarzany"
    // TODO zmiany statusow moga byc odzwierciedlane poprzez tworzenie plikow nazwaStatusu.status (ale nie musza -> serializacja)

    // trzeba zaimplementowac metode porownujaca - done
    // trzeba zaimplementowac serializacje, zapis, odczyt listy zadan - done

    // TODO foldery <user>, <projekt>, <obrazki>, <wyniki> itd (z wyjatkiem users) powinny byc tworzone w trybie leniwym (kiedy trzeba) 
    // pomoze to w wykrywaniu, jesli nie bedzie folderu wyniki to nie ma problemu

    // TODO przetestowac w przypadku braku folderow obrazki lub users

    // TODO ogarnac liste serializowana userow, klase user, metody klasy user, backupy co jakis czas w oddzielnych plikach, oznaczenie data, usuwanie najstarszych (zostawianie n najnowszych)
    // TODO zastanowic sie nad dedykowanymi folderami do tych plikow

    // TODO zastanowic sie czy nazwy folderow nie beda kolidowac z baza w liscie serializowanej userow, ewentualne odzyskiwanie listy po nazwach folderow i komunikat podania nowego hasla
		
	
/*    public static void main(String[] args) throws IOException  {
        test();
        load();
        scanFolders(listaZadanOczekujacych);
        System.out.println(getZadaniaNaLiscie(listaZadanOczekujacych));
        System.out.println(getZadaniaNaLiscie(listaZadanUkonczonych));
        save();
    }*/
    
    

    private static void load() {
//        try {
//            FileInputStream fileIn = new FileInputStream(makePath(ROOT_PATH, "\\listaZadanOczekujacych.ser"));
//            ObjectInputStream in = new ObjectInputStream(fileIn);
//            listaZadanOczekujacych = (List<Zadanie>) in.readObject();
//            in.close();
//        fileIn.close();
//        } catch (Exception e) {
//            System.out.println("Nie znaleziono zapisanej listy, tworzenie nowej listy listaZadanOczekujacych");
//            listaZadanOczekujacych = new ArrayList<Zadanie>();
//        } 
        
        listaZadanOczekujacych = new ArrayList<Zadanie>();
        
        try {
            FileInputStream fileIn = new FileInputStream(makePath(ROOT_PATH, "\\listaZadanUkonczonych.ser"));
            ObjectInputStream in = new ObjectInputStream(fileIn);
            listaZadanUkonczonych = (List<Zadanie>) in.readObject();
            in.close();
        fileIn.close();
        } catch (Exception e) {
            System.out.println("Nie znaleziono zapisanej listy, tworzenie nowej listy listaZadanUkonczonych");
            listaZadanUkonczonych = new ArrayList<Zadanie>();
        }
    }

    private static void save() {
//        try {
//            FileOutputStream fileOut = new FileOutputStream(makePath(ROOT_PATH, "\\listaZadanOczekujacych.ser"));
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//            out.writeObject(listaZadanOczekujacych);
//            out.close();
//            fileOut.close();
//            System.out.println("Serialized data is saved in listaZadanOczekujacych.ser");
//        } catch (Exception e) {
//            System.out.println("zapis listy listaZadanOczekujacych.ser nie udal sie");
//        } 

        try {
            FileOutputStream fileOut = new FileOutputStream(makePath(ROOT_PATH, "\\listaZadanUkonczonych.ser"));
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(listaZadanUkonczonych);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in listaZadanUkonczonych.ser");
        } catch (Exception e) {
            System.out.println("zapis listy listaZadanUkonczonych.ser nie udal sie");
        } 
    }
    
    public static void dodajZadanie(String nazwaUzytkownika, String nazwaProjektu) throws IOException {
        load();
        new File(makePath(ROOT_PATH, "users",  nazwaUzytkownika, nazwaProjektu, "wyniki")).mkdirs();
        
        
        System.out.println("listaZadanOczekujacych.isEmpty() = " + listaZadanOczekujacych.isEmpty());
        System.out.println("liczba watkow = " + OperacjeOpenCv.liczbaWatkow);
        //if (listaZadanOczekujacych.isEmpty() || ((!listaZadanOczekujacych.isEmpty()) && OperacjeOpenCv.liczbaWatkow == 0)) {
        scanFolders(listaZadanOczekujacych);
        if (listaZadanOczekujacych.contains(zadaniePrzetwarzane)) listaZadanOczekujacych.remove(zadaniePrzetwarzane);
        //if (!listaZadanOczekujacych.isEmpty() && OperacjeOpenCv.liczbaWatkow == 0) {
        if (OperacjeOpenCv.liczbaWatkow == 0) {
            System.out.println("powolanie nowego watku");
            //System.out.println("listaZadanOczekujacych.isEmpty(): true -> false");
            OperacjeOpenCv oocWorker = new OperacjeOpenCv();
            //oocWorker.init(listaZadanOczekujacych, listaZadanUkonczonych);
            //oocWorker.run();
            Thread oocThread = new Thread(oocWorker, "thread_"+System.currentTimeMillis());
            oocThread.start();
        }
        //}
        save();
    }
    
    public static String makePath(String ... str) {
        return String.join("\\", str);
    }
    
    public static void changeZadanieStatus (List<Zadanie> l, Zadanie z, String status) {
        int index = l.indexOf(z);
        Zadanie zTmp = l.get(index);
        zTmp.setStatus(status);
        l.set(index, zTmp);
    }

    
    public static void makeFoldersWork(Zadanie z) {
        String[] folderyRobocze = {"debug", "dopasowane", "filmiki", "usrednione", "wydobyte"};

        for (String fr : folderyRobocze)
            new File(makePath(ROOT_PATH, "users", z.getNazwaUzytkownika() , z.getNazwaProjektu(), fr)).mkdirs();
    }

    public static void scanFolders(List<Zadanie> lista, File ... root) throws IOException {
        File[] listOfFiles;
        int plikowWynikowych = -1;
        int plikowZrodlowych = -1;
        
        if (root.length != 1) {
            File users = new File(makePath(ROOT_PATH, "users"));
            listOfFiles = users.listFiles();
        }
        else {
            listOfFiles = root[0].listFiles();
        }

        for (File f : listOfFiles) {
            System.out.println(f);
            if (f.isDirectory()) {
                    scanFolders(lista, f);
                }
            if (f.getName().equals("obrazki")) plikowZrodlowych = f.listFiles().length;
            if (f.getName().equals("wyniki")) plikowWynikowych = f.listFiles().length;
        }

        if (plikowWynikowych == 0)
            if (plikowZrodlowych > 1) {
                String sciezka = root[0].toString();
                String[] sciezkaArray = sciezka.split("\\\\");

                String userName = sciezkaArray[sciezkaArray.length - 2];
                String projectName = sciezkaArray[sciezkaArray.length - 1];

                Zadanie z = new Zadanie(userName, projectName, sciezka, "oczekuje");
                if (!lista.contains(z)) {
                    lista.add(z);
                }
                System.out.println("We have a situation: " + z);
            }
    }


    public static void wyswietlZadaniaNaLiscie(List<Zadanie> lista, String ... user) {
        int i = 0;
        if (lista == null) {
            System.out.println("brak zadan na liscie");
        } else {
            for (Zadanie z : lista) {
                if (user.length == 0) {
                    i++;
                    System.out.println(i + ". " + z);
                }
                else {
                    if (z.getNazwaUzytkownika().equals(user[0])) {
                        i++;
                        System.out.println(i + ". " + z);
                        // todo: moze zamiast wyswietlac tutaj, laczyc to w string i zwracac?
                        // duzo zalet powyzszego rozwiazania
                    }
                }
            }
        }
    }

    public static String getZadaniaNaLiscie(List<Zadanie> lista, String ... user) {
        int i = 0;
        String str = "";
        if (lista == null) {
            return "brak zadan na liscie";
        } else {
            for (Zadanie z : lista) {
                if (user.length == 0) {
                    i++;
                    str += i + ". " + z;
                }
                else {
                    if (z.getNazwaUzytkownika().equals(user[0])) {
                        i++;
                        str += i + ". " + z;
                    }
                }
            }
        }
        return str;
    }


    
    public void test() {
        List<Zadanie> lst = new ArrayList<Zadanie>();;
        Zadanie z1 = new Zadanie("zbyszek", "p31", "users\\zbyszek\\p31\\obrazki", "oczekuje");
        Zadanie z2 = new Zadanie("zbyszek", "p31", "users\\zbyszek\\p31\\obrazki", "przetwarzany");
        lst.add(z1);

        assert getZadaniaNaLiscie(lst).equals("1. " + z1.toString());

        // powinno byc prawda
        assert z1.equals(z2);
        assert lst.contains(z2);

        z2 = new Zadanie("zbyszek", "asd", "users\\zbyszek\\p31\\obrazki", "przetwarzany");

        // powinno byc falsz
        assert !z1.equals(z2);
        assert !lst.contains(z2);

        assert makePath("a", "b", "c", "d", "e").equals("a\\b\\c\\d\\e");
    }
    
    
}




