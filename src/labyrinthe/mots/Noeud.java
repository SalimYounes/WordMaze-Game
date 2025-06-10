package labyrinthe.mots;

import java.util.Objects;

public class Noeud implements Cloneable{
   
	private char lettre; 
    private boolean bloque;  
    private String id; 
    private boolean used; 
    
    

    // Constructeur
    public Noeud(char lettre, String id) {
    	this.id = id;
        this.lettre = lettre;
        this.bloque = false;
        this.used = false; 
         
        }
    public Noeud(Noeud autre) {
        this.lettre = autre.lettre;
        this.bloque = autre.bloque;
        this.id = autre.id;
    }

    
 // Redéfinition de la méthode clone()
    @Override
    public Noeud clone() {
        try {
            return (Noeud) super.clone(); // Clonage superficiel suffisant
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clonage non supporté", e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Noeud noeud = (Noeud) obj;
        return id.equals(noeud.id); // Compare uniquement l'ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
    
    public String getId() {
        return id;
    } 
    public char getLettre() { return lettre; }
    public void setLettre(char lettre) { this.lettre = lettre; }
    public boolean estBloque() { return bloque; }
    public void setBloque(boolean bloque) { this.bloque = bloque; }
 
    @Override
    public String toString() {
        return "lettre=" + lettre +
               ", bloque=" + bloque +
               ", id=" + id +
               "//";
    }
}