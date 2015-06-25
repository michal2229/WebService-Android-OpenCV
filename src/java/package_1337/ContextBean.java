/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package package_1337;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import static package_1337.ZarzadzaniePlikami.ROOT_PATH;
import static package_1337.ZarzadzaniePlikami.makePath;

/**
 *
 * @author Michał
 */

@ManagedBean(name="contextBean")
@SessionScoped
public class ContextBean implements Serializable {
    public String login = "a";
    public String haslo = "a";
    public boolean zalogowany;
    public boolean zarejestrowany;
    public static List<StreamedContent> dbImages = new ArrayList<>();
    public static List<String> dbImagesString = new ArrayList<>();
    public String wiadomosc;
    private static int i = 0;
    
    
    
    public String zaloguj() {
        Uzytkownik u = new Uzytkownik(); 
        u.setLogin(login);
        u.setHaslo(haslo);

        if (Bean.validateUzytkownik(u)) {
            u = Bean.getUzytkownikKonto(u);
            Bean.zalogujUzytkownika(u); // tu nastepuje logowanie
            wiadomosc = "zalogowano poprawnie "  + login;
            return "index";
        } else {
            wiadomosc = "zly login lub haslo";
        }
        return "login";
    }
    
    public String zarejestruj() {
        Uzytkownik u = new Uzytkownik(); 
        u.setLogin(login);
        u.setHaslo(haslo);

        if (!Bean.isUzytkownikZarejestrowany(u)) {
            Bean.zarejestrujUzytkownika(u);
            wiadomosc = "zarejestrowano poprawnie "  + login;
        } else {
            wiadomosc = login + " juz zarejestrowany!";
        }
        return "login";
    }
    
    public String wyloguj() {
        Uzytkownik u = new Uzytkownik(); 
        u.setLogin(login);

        Bean.wylogujUzytkownika(u);
        wiadomosc = "wylogowano uzytkownika " + login;
        return "login";
    }
    
    
    public String index() {
        return "index";
    }
        
    public String galeria() {
        return "galeria";
    }

    public String opcje() {
        return "opcje";
    }
    
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHaslo() {
        return haslo;
    }

    public void setHaslo(String haslo) {
        this.haslo = haslo;
    }

    public String getWiadomosc() {
        return wiadomosc;
    }

    public void setWiadomosc(String wiadomosc) {
        this.wiadomosc = wiadomosc;
    }

    public boolean isZalogowany() {
        Uzytkownik u = new Uzytkownik(); 
        u.setLogin(login);
        
        zalogowany = Bean.isUzytkownikZalogowany(u);
        return zalogowany;
    }

    public void setZalogowany(boolean zalogowany) {
        this.zalogowany = zalogowany;
    }

    public boolean isZarejestrowany() {
        Uzytkownik u = new Uzytkownik(); 
        u.setLogin(login);
        
        zarejestrowany = Bean.isUzytkownikZarejestrowany(u);
        return zarejestrowany;
    }

    public void setZarejestrowany(boolean zarejestrowany) {
    }

    public List<StreamedContent> getDbImages() {
        //List<Path> listaObrazkow = new ArrayList<>();
        i = 0;
        dbImages = new ArrayList<>();
        System.out.println("getDbImages()");
        for (File projekt :(new File(makePath(ROOT_PATH, "users", login))).listFiles()) {
            System.out.println("projekt");
            for (File obrazek : (new File(projekt.getAbsolutePath() + "\\wyniki")).listFiles()) {
                System.out.println("obrazek " + obrazek.getName());
                FileInputStream obrazekFIS;
                try {
                    obrazekFIS = new FileInputStream(obrazek);
                    StreamedContent chartImage = new DefaultStreamedContent(obrazekFIS, "image/png");
                    dbImages.add(chartImage);
                    System.out.println("dbImages.size() = " + dbImages.size());
                } catch (FileNotFoundException ex) {
                    System.out.println("cos dupło... " + ex.getMessage());
                    
                }
                
            }
        }
            
        return dbImages;
    }
    

    
    public StreamedContent getDbImage() throws FileNotFoundException, IOException {
        //dbImages = getDbImages();
        //i++; if (i > dbImages.size() - 1) i = 0;
        System.out.println("dbImages.size() = " + dbImages.size());
        StreamedContent chartImage = dbImages.remove(i);
        return chartImage;
    }

    public void setDbImages(List<StreamedContent> dbImage) {
    }
    
    
    public List<String> getDbImagesString() throws FileNotFoundException, IOException {
        //List<Path> listaObrazkow = new ArrayList<>();
        i = 0;
        dbImagesString = new ArrayList<>();
        
        for (File projekt :(new File(makePath(ROOT_PATH, "users", login))).listFiles()) {
            String projectName = projekt.getName();
            System.out.println("projekt");
            for (File obrazek : (new File(projekt.getAbsolutePath() + "\\wyniki")).listFiles()) {
                System.out.println("obrazek " + obrazek.getCanonicalPath());
                
                dbImagesString.add("/resources/users/" + login + "/" + projectName + "/wyniki/" + obrazek.getName() );
                System.out.println("dbImages.size() = " + dbImages.size());
            }
        }
            
        return dbImagesString;
    }
    
    
    
}
