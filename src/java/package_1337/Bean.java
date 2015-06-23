/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package package_1337;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Michał
 */

@ManagedBean()
@ApplicationScoped
public class Bean {
    public static final String ROOT_PATH = "d:\\dane\\chmura\\dropbox\\programowanie\\projekty\\NetBeans\\TimWebAppAndroid\\";
    public static List<Uzytkownik> listaKont = null; //new ArrayList<Uzykownik>();
    public static List<Uzytkownik> listaZalogowanych = null; //new ArrayList<Uzykownik>();
    
    static void load() {
        if (listaZalogowanych == null) listaZalogowanych = new ArrayList<Uzytkownik>();
        
        
        if (listaKont == null) {
            try {
                FileInputStream fileIn = new FileInputStream(ROOT_PATH + "listaKont.ser");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                listaKont = (List<Uzytkownik>) in.readObject();
                in.close();
                fileIn.close();
                //listaKont = new ArrayList<Uzykownik>();
            } catch (FileNotFoundException ex) {
                System.out.println("Nie znaleziono zapisanej listy, tworzenie nowej listy listaKont");
                listaKont = new ArrayList<Uzytkownik>();
            } catch (IOException ex) {
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static void save() {
        try {
            FileOutputStream fileOut = new FileOutputStream(ROOT_PATH + "listaKont.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(listaKont);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in listaKont.ser");
        } catch (Exception e) {
            e.printStackTrace();
        } 
        
    }
    
    public static Uzytkownik getUzytkownikKonto(Uzytkownik u) {
        return listaKont.get(listaKont.indexOf(u));
    }
    
    
    public static String getUzytkownikLogin(Uzytkownik u) {
        return listaZalogowanych.get(listaZalogowanych.indexOf(u)).getLogin();
    }
    
    public static String getUzytkownikAktywnyProjekt(Uzytkownik u) {
        return listaKont.get(listaKont.indexOf(u)).getAktywnyProjekt();
    }
    
    public static void setUzytkownikAktywnyProjekt(Uzytkownik u, String nazwaProjektu) {
        int index = listaZalogowanych.indexOf(u);
        Uzytkownik uTmp = listaZalogowanych.get(index);
        uTmp.setAktywnyProjekt(nazwaProjektu);
        listaZalogowanych.set(index, uTmp);
        
        index = listaKont.indexOf(u);
        uTmp = listaKont.get(index);
        uTmp.setAktywnyProjekt(nazwaProjektu);
        listaKont.set(index, uTmp);
    }
    
    public static boolean validateUzytkownik(Uzytkownik u) {
        if (listaKont.contains(u))
            return listaKont.get(listaKont.indexOf(u)).getHaslo().equals(u.getHaslo());
        else
            return false;
    }
    
    public static boolean isUzytkownikZalogowany(Uzytkownik u) {
        return listaZalogowanych.contains(u);
    }
    
    public static boolean isUzytkownikZarejestrowany(Uzytkownik u) {
        return listaKont.contains(u);
    }
    
    public static void zalogujUzytkownika(Uzytkownik u) {
        listaZalogowanych.add(u);
    }
    
    public static void wylogujUzytkownika(Uzytkownik u) {
        listaZalogowanych.remove(u);
    }
    
    public static void zarejestrujUzytkownika(Uzytkownik u) {
        listaKont.add(u);
    }

    static String getListaKont() {
        String txt = "";
        int i = 0;
        
        for (Uzytkownik u : listaKont) {
            i++;
            txt += i + ". " +  u.toString() + "\n";
        }
        
        return txt;
    }
    
    static String getListaZalogowanych() {
        String txt = "";
        int i = 0;
        
        for (Uzytkownik u : listaZalogowanych) {
            i++;
            txt += i + ". " +  u.toString() + "\n";
        }
        
        return txt;
    }
}
