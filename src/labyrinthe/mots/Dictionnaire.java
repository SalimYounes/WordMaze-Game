package labyrinthe.mots;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;


public class Dictionnaire {
	
	  private List<String> dictionnaire;
	  
	
	  // Dictionnaires par thème
	    private final Map<String, String> fichiersDictionnaires = Map.of(
	            "Informatique", "D:\\eclipse-workspace\\ING\\essai2\\dictInfo.txt",
	            "Nourriture", "D:\\eclipse-workspace\\ING\\essai2\\nourriture.txt",
	            "Voiture", "D:\\eclipse-workspace\\ING\\essai2\\voiture.txt"
	        );
	    
	    
	    public void chargerDictionnaire(String theme) {
	        dictionnaire = new ArrayList<>();	      
	       String fichierDictionnaire = fichiersDictionnaires.get(theme);
	        try (BufferedReader lecteur = new BufferedReader(new FileReader(fichierDictionnaire))) {
	            String ligne;
	            while ((ligne = lecteur.readLine()) != null) {
	                dictionnaire.add(ligne.trim()); // Ajouter les mots au dictionnaire
	            } 
	        } catch (IOException e) {
	            JOptionPane.showMessageDialog(null, "Erreur lors du chargement du dictionnaire !", "Erreur", JOptionPane.ERROR_MESSAGE);
	        }
	    }
	    public List<String> getDictionnaire() {
	        return dictionnaire;
	    }

 	    public boolean estMotValide(String mot) {
	        return dictionnaire.contains(mot);
	    }
 	    // String contient mot valide ou non 
	    public boolean contientMotValide(String mot) {
	        for (int i = 0; i < mot.length(); i++) {
	            for (int j = i + 1; j <= mot.length(); j++) {
	                String sousMot = mot.substring(i, j);
	                if (estMotValide(sousMot)) {
	                    return true;
	                }
	            }
	        }
	        return false;
	    }
	    public List<Character> calculerLettresMoinsProbables(String theme) {
	        Map<Character, Integer> frequences = new HashMap<>();
 
	        for (String mot : getDictionnaire()) {
	            for (char c : mot.toCharArray()) {
	                frequences.put(c, frequences.getOrDefault(c, 0) + 1);
	            }
	        } 
	        
	        return frequences.entrySet().stream()
	                .sorted(Map.Entry.comparingByValue()) // tri par fréquence d'apparence de lettre
	                .limit(10) //   les 10 moins fréquentes
	                .map(Map.Entry::getKey) // extraire les lettres
	                .collect(Collectors.toList()); // etocker dans une liste
	    }
	    
	    
	    
	    
}

