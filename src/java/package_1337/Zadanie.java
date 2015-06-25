/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package package_1337;

/**
 *
 * @author Michał
 */
public class Zadanie implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -8091924837469902956L;

    String nazwaUzytkownika;
    String nazwaProjektu;
    String sciezkaPlikow;
    String status;

    public Zadanie(String nazwaUzytkownika, String nazwaProjektu, String sciezkaPlikow, String status) {
        super();
        this.nazwaUzytkownika = nazwaUzytkownika;
        this.nazwaProjektu = nazwaProjektu;
        this.sciezkaPlikow = sciezkaPlikow;
        this.status = status;
    }

    public String getNazwaUzytkownika() {
        return nazwaUzytkownika;
    }

    public void setNazwaUzytkownika(String nazwaUzytkownika) {
        this.nazwaUzytkownika = nazwaUzytkownika;
    }

    public String getNazwaProjektu() {
        return nazwaProjektu;
    }

    public void setNazwaProjektu(String nazwaProjektu) {
        this.nazwaProjektu = nazwaProjektu;
    }

    public String getSciezkaPlikow() {
        return sciezkaPlikow;
    }

    public void setSciezkaPlikow(String sciezkaPlikow) {
        this.sciezkaPlikow = sciezkaPlikow;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Zadanie [nazwaUzytkownika=" + nazwaUzytkownika
                        + ", nazwaProjektu=" + nazwaProjektu
                        + ", sciezkaPlikow=" + sciezkaPlikow
                        + ", status=" + status
                        + "]";
    }

    @Override
    public boolean equals(Object o) {
        Zadanie z = (Zadanie) o;

        return nazwaUzytkownika.equals(z.getNazwaUzytkownika()) && nazwaProjektu.equals(z.getNazwaProjektu()) && sciezkaPlikow.equals(z.getSciezkaPlikow());
    }
}