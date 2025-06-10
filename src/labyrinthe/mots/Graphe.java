package labyrinthe.mots;

import java.util.*;
import java.util.stream.Collectors; 

public class Graphe implements Cloneable{ 
	
    private Map<Noeud, List<Noeud>> listeAdjacence;
    private Noeud depart, arrivee;
    private Random random = new Random();
    private List<Noeud> noeuds; 
    private List<Character> caracteresRaresDict;
    private Map<Character, Noeud> noeudsRares ; 
    private Set<String> motRares ; 
    public Set<String> getMotsRares() {return this.motRares;}
    private int niveau; 
    public Map<Character, Noeud> getNoeudsRares()  {return noeudsRares;}
    
    private String theme;
    private int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},  // Haut, Bas, Gauche, Droite
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // Diagonales
        };
    private boolean cheminExiste = false; 
    public boolean existeChemin() {return cheminExiste;}
    public Boolean setExisteChemin(boolean varBool) {return this.cheminExiste= varBool;}
    private int distancePlusCourtChemin = 0;
    public int getLongueurPlusCourtChemin() {return distancePlusCourtChemin;}
    private List<Noeud> cheminOptimal;
    public  List<Noeud> getCheminOptimal(){ return cheminOptimal;}
   
    private Set<Noeud> listeNoeudsMotsValides;
    private List<Noeud> noeudsRestants;
    private Map<String, List<Noeud>> cheminsDesMots = new HashMap<>(); // Stocker les chemins des mots
    private Set<String> tousLesMotsPlaces = new HashSet<>(); // Ensemble pour tous les mots placés
    
    
    
    public Set<Noeud> getListeNoeudsMotsValides() { return listeNoeudsMotsValides;}
    
    public List<Noeud> BFS(Noeud depart, Noeud arrivee)
    { return calculerCheminLePlusCourtBFS(depart,arrivee);}  
    
    public int getTaille(int niveau) {
        return (niveau == 1) ? 7 : (niveau == 2) ? 9 : 12;
    }
   
    @Override
    public Graphe clone() {
        try {
            Graphe clone = (Graphe) super.clone();
            if (!this.noeuds.contains(this.arrivee)) {
                System.out.println("⚠ Problème : arrivee n'est pas dans la liste des noeuds !");
            }


            // clonage  des nœuds
            Map<Noeud, Noeud> noeudCloneMap = new HashMap<>();
            
            for (Noeud original : this.noeuds) { 
                Noeud copie = original.clone(); // Clonage profond de chaque nœud
                noeudCloneMap.put(original, copie);
            }

            // clonage profond de la liste d'adjacence
            clone.listeAdjacence = new HashMap<>();
            for (Map.Entry<Noeud, List<Noeud>> entry : this.listeAdjacence.entrySet()) {
                Noeud noeudCloned = noeudCloneMap.get(entry.getKey());
                List<Noeud> voisinsCloned = entry.getValue().stream()
                    .map(noeudCloneMap::get) // Cloner chaque voisin
                    .filter(Objects::nonNull) // Filtrer les valeurs nulles
                    .collect(Collectors.toList());
                clone.listeAdjacence.put(noeudCloned, voisinsCloned);
            }

            // clonage des nœuds
            clone.noeuds = new ArrayList<>(noeudCloneMap.values());

             
            clone.noeudsRares = new HashMap<>();
            for (Map.Entry<Character, Noeud> entry : this.noeudsRares.entrySet()) {
                clone.noeudsRares.put(entry.getKey(), noeudCloneMap.get(entry.getValue()));
            }

             
            clone.motRares = new HashSet<>(this.motRares);

             
            clone.listeNoeudsMotsValides = this.listeNoeudsMotsValides.stream()
                .map(noeudCloneMap::get)
                .collect(Collectors.toSet());

             
            clone.noeudsRestants = this.noeudsRestants.stream()
                .map(noeudCloneMap::get)
                .collect(Collectors.toList());

            //  clonage chemin optimal
            clone.cheminOptimal = (this.cheminOptimal != null)
                ? this.cheminOptimal.stream().map(noeudCloneMap::get).collect(Collectors.toList())
                : null;

            //  clonage des attributs primitifs
            clone.cheminExiste = this.cheminExiste;
            clone.distancePlusCourtChemin = this.distancePlusCourtChemin;
            clone.niveau = this.niveau;
            clone.theme = this.theme;

         //  clonage départ et d'arrivee
            if (this.depart != null) {
            	 clone.depart = noeudCloneMap.get(this.depart);
            } 
           clone.arrivee = noeudCloneMap.get(this.arrivee);
            
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clonage non supporté", e);
        }
    }

    
    
    
    
    
    
    
    public boolean sontVoisins(Noeud n1, Noeud n2) {
        return listeAdjacence.containsKey(n1) && listeAdjacence.get(n1).contains(n2);
    } 
    // Constructeur
    public Graphe(String theme, int niveau) { 
    	 listeAdjacence = new HashMap<>();
         // noeudsNonBloques = new ArrayList<>();
         this.theme = theme;  
         noeudsRares = new HashMap<>(); 
         this.niveau = niveau ; 
         this.listeNoeudsMotsValides =  new HashSet<>();
         genererGrille(theme, niveau);
         // necessaire pour mode multijoueur : cloner avec noeuds 
          noeuds.add(arrivee);
         if ( depart != null) {
        	noeuds.add(depart); 
         }
         for (Noeud n : listeAdjacence.get(arrivee)) {
        	  noeuds.add(n);
         } 
    }
                
    public void genererGrille(String theme, int niveau) {
     	creerEtConnecterNoeuds(niveau);
    	definirPositionsDepartArrivee(niveau);
    	// w zidna arrivé eet départ lel listeNoeudsMotsValides 
    	Dictionnaire dictionnaire = new Dictionnaire();
        dictionnaire.chargerDictionnaire(theme);
        placerMotsDictionnaire(dictionnaire, niveau);
        // calcul BFS pour ajouter le chemin a la liste des noeudsMotsValides 
        if (niveau == 3) {
            cheminOptimal = BFS(depart, arrivee);
            listeNoeudsMotsValides.addAll(cheminOptimal);
        } else {
        	List<Noeud> listeNoeudsShuffeled = noeuds;
            Collections.shuffle(listeNoeudsShuffeled);
          Noeud  depart = listeNoeudsShuffeled.get(0);
            cheminOptimal = BFS(depart, arrivee);
            listeNoeudsMotsValides.addAll(cheminOptimal);
        }
        //Récupérer les nœuds restants
        noeudsRestants = new ArrayList<>(noeuds);
        noeudsRestants.removeAll(listeNoeudsMotsValides);
        bloquerNoeudsRestants(niveau);  
        
        }   
    private void bloquerNoeudsRestants(int niveau) {
        double pourcentageBloque = getPourcentageBloque(niveau);
        int nombreNoeudsABloquer = (int) (noeudsRestants.size() * pourcentageBloque);

        Collections.shuffle(noeudsRestants);
        Set<Noeud> noeudsBloques = new HashSet<>();

        for (Noeud noeud : noeudsRestants) {
            if (noeudsBloques.size() >= nombreNoeudsABloquer) break;

            int[] coords = getCoordsFromId(noeud.getId());
            int i = coords[0];
            int j = coords[1];

            // Vérifier qu'on ne forme pas un bloc de 2x2
            if (formeBloc2x2(i, j, noeudsBloques)) continue;

            // Bloquer le nœud
            noeud.setBloque(true);
            noeud.setLettre(' '); // Case bloquée vide
            noeudsBloques.add(noeud);
        } 
        supprimerNoeudsBloquesDesVoisins(noeudsBloques);
    }
    public void supprimerNoeudsBloquesDesVoisins(Set<Noeud> noeudsBloques) {
        for (Noeud bloque : noeudsBloques) {
            for (Noeud voisin : new HashSet<>(listeAdjacence.getOrDefault(bloque, new ArrayList<>()))) {
                listeAdjacence.get(voisin).remove(bloque); // Supprime le nœud bloqué des voisins
            }
        }
    } 
    private boolean formeBloc2x2(int i, int j, Set<Noeud> noeudsBloques) {
        return (noeudsBloques.contains(getNoeud(i - 1, j)) && noeudsBloques.contains(getNoeud(i, j - 1)) && noeudsBloques.contains(getNoeud(i - 1, j - 1))) ||
               (noeudsBloques.contains(getNoeud(i + 1, j)) && noeudsBloques.contains(getNoeud(i, j - 1)) && noeudsBloques.contains(getNoeud(i + 1, j - 1))) ||
               (noeudsBloques.contains(getNoeud(i - 1, j)) && noeudsBloques.contains(getNoeud(i, j + 1)) && noeudsBloques.contains(getNoeud(i - 1, j + 1))) ||
               (noeudsBloques.contains(getNoeud(i + 1, j)) && noeudsBloques.contains(getNoeud(i, j + 1)) && noeudsBloques.contains(getNoeud(i + 1, j + 1)));
    }
 
    private Noeud getNoeud(int i, int j) {
        String id = i + "-" + j;
        return noeuds.stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }
 
    private int[] getCoordsFromId(String id) {
        String[] parts = id.split("-");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    private double getPourcentageBloque(int niveau) {
        switch (niveau) {
            case 1: return 0.7;  
            case 2: return 0.8;  
            case 3: return 0.8;  
            default: return 0.5;
        }
    }
   private void creerEtConnecterNoeuds(int niveau) {
        int taille = getTaille(niveau);
        noeuds = new ArrayList<>();
        listeAdjacence = new HashMap<>();

        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                String id = i + "-" + j; 
                
                char lettre =  (char) ('a' + random.nextInt(26)) ; // Lettre vide si bloqué, sinon lettre aléatoire
                Noeud noeud = new Noeud(lettre, id); 
                
                noeuds.add(noeud);
                listeAdjacence.put(noeud, new ArrayList<>()); 
            }
        }

        // Connecter les nœuds (y compris les diagonales)
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                Noeud noeud = noeuds.get(i * taille + j);
                for (int[] d : directions) {
                    int ni = i + d[0], nj = j + d[1];
                    if (ni >= 0 && ni < taille && nj >= 0 && nj < taille) {
                        Noeud voisin = noeuds.get(ni * taille + nj);
                        listeAdjacence.get(noeud).add(voisin); 
                    }
                }
            }
        } 
    }  
   
   
//Garantir que la case d'arrivée est vide
    private void definirPositionsDepartArrivee(int niveau) {
        if (niveau == 1 || niveau == 2) { 
        	  arrivee = noeuds.get((getTaille(niveau) - 1) * getTaille(niveau) + (getTaille(niveau) - 1));
        	 // arrivé déja décalaré dans creernoeud boucle 3 
            depart = null; // Le joueur choisira 
        } else if (niveau == 3) { 
        	
            while (!cheminExiste ) {
            	do {
            		List<Noeud> listeNoeudsShuffeled = noeuds;
                Collections.shuffle(listeNoeudsShuffeled);
                depart = listeNoeudsShuffeled.get(0);
                arrivee = listeNoeudsShuffeled.get(10);
                
                calculerCheminLePlusCourtBFS(depart, arrivee); 
            	}while (distancePlusCourtChemin < 7 );
            	}
        } 
        if ( depart != null) {
        	listeNoeudsMotsValides.add(depart); 
        } 
        listeNoeudsMotsValides.add(arrivee); 
        if (arrivee != null) {
            arrivee.setLettre(' ');  
        }
        if (depart != null) {
            depart.setLettre(' ');  
        }
    }
    
    
    //BFS 
    private List<Noeud> calculerCheminLePlusCourtBFS(Noeud depart, Noeud arrivee) {
        Map<Noeud, Noeud> predecesseurs = new HashMap<>(); // Pour stocker le prédécesseur de chaque nœud
        Map<Noeud, Integer> distances = new HashMap<>();   // Pour stocker la distance de chaque nœud par rapport au départ
        Queue<Noeud> queue = new LinkedList<>();
        Set<Noeud> visited = new HashSet<>();
        distances.put(depart, 0); 

        queue.add(depart);
        visited.add(depart);

        while (!queue.isEmpty()) {
            Noeud courant = queue.poll();

            // Si on atteint le nœud d'arrivée, reconstruire le chemin
            if (courant.equals(arrivee)) {
            	cheminExiste = true; 
            	 distancePlusCourtChemin = distances.get(courant); // Stocker la longueur du plus court chemin
                return reconstruireChemin(predecesseurs, arrivee);
            }
 
            for (Noeud voisin : listeAdjacence.get(courant)) {
                if (!visited.contains(voisin)) {
                    visited.add(voisin);
                    predecesseurs.put(voisin, courant);
                    distances.put(voisin, distances.get(courant) + 1); // Distance du voisin = distance du courant + 1
                    queue.add(voisin);
                }
            }
        }

        return Collections.emptyList(); // Aucun chemin trouvé
    }

    private List<Noeud> reconstruireChemin(Map<Noeud, Noeud> predecesseurs, Noeud arrivee) {
        List<Noeud> chemin = new ArrayList<>();
        Noeud courant = arrivee;

        while (courant != null) {
            chemin.add(courant);
            courant = predecesseurs.get(courant);
        }

        Collections.reverse(chemin); // Inverser pour avoir le chemin du départ à l'arrivée
        return chemin;
    } 
    
    // set obligatoire !!!! pour eviter de selectionner le meme mot 2 fois 
    public void placerMotsDictionnaire(Dictionnaire dictionnaire, int niveau) {
    	motRares = new HashSet<String>();
    	tousLesMotsPlaces = new HashSet<>();
    	List<String> motsDisponibles = new ArrayList<>(dictionnaire.getDictionnaire());
        Collections.shuffle(motsDisponibles); // Mélange pour choisir aléatoirement
        int nombreMaxMots = getNombreMaxMots(niveau);
        
        caracteresRaresDict = dictionnaire.calculerLettresMoinsProbables(theme); 

        Set<String> motsUtilises = new HashSet<>(); // Pour éviter les doublons
        int motsPlaces = 0;

        for (String mot : motsDisponibles) {
            if (motsPlaces >= nombreMaxMots) break;
            if (motsUtilises.contains(mot)) continue; // Ignorer si le mot a déjà été placé

            Noeud departAleatoire = trouverNoeudDepartDeplacement(niveau);
            if (departAleatoire == null) continue; // Aucun nœud de départ valide

            List<Noeud> chemin = trouverCheminPourMot(departAleatoire, mot.length());
            if (!chemin.isEmpty() && peutPlacerMot(mot, chemin)) { 
                insererMotDansChemin(mot, chemin);
                cheminsDesMots.put(mot, chemin); // Stocker le chemin du mot
                tousLesMotsPlaces.add(mot);
                
             // Vérifier si le dernier nœud garde un chemin vers l’arrivée
                Noeud dernierNoeud = chemin.get(chemin.size() - 1);
                List<Noeud> cheminVersArrivee = calculerCheminLePlusCourtBFS(dernierNoeud, arrivee);

                if (!cheminVersArrivee.isEmpty()) {
                    listeNoeudsMotsValides.addAll(cheminVersArrivee);
                }
                
                listeNoeudsMotsValides.addAll(chemin);
                motsUtilises.add(mot); // Marquer le mot comme utilisé
                System.out.println(mot); 
                motsPlaces++;
            }
        } System.out.println(motRares);
        System.out.println("***************"); 
    }
    public Set<String> getTousLesMotsPlaces() {
        return this.tousLesMotsPlaces; // Retourne tous les mots placés dans la grille
    }
    public Map<String, List<Noeud>> getCheminsDesMots() {
        return this.cheminsDesMots; // Retourne les chemins des mots
    }
    private Noeud trouverNoeudDepartDeplacement(int niveau) {
        // Exclure le nœud d'arrivée et ses voisins (niveaux 1 et 2)
        if (niveau == 1 || niveau == 2) {
            noeuds.remove(arrivee);
            noeuds.removeAll(listeAdjacence.get(arrivee));
        }
        // Exclure le nœud de départ (niveau 3)
        if (niveau == 3) {
            noeuds.remove(depart);
            // hedhi najem na7iha 5tr déja najem nebda ken bel depart 
            noeuds.remove(arrivee);
        }
        Collections.shuffle(noeuds);
        return noeuds.isEmpty() ? null : noeuds.get(0);
    }
    public Noeud getNoeudParId(String id) {
        for (Noeud noeud : listeAdjacence.keySet()) {
            if (noeud.getId().equals(id)) {  // Suppose que Noeud a une méthode getId()
                return noeud;
            }
        }
        return null; // Aucun nœud trouvé avec cet ID
    }


    private List<Noeud> trouverCheminPourMot(Noeud depart, int longueurMot) {
        Queue<List<Noeud>> queue = new LinkedList<>();
        queue.add(Collections.singletonList(depart));

        while (!queue.isEmpty()) {
            List<Noeud> chemin = queue.poll();
            if (chemin.size() == longueurMot) return chemin;

            Noeud dernier = chemin.get(chemin.size() - 1);
            for (Noeud voisin : listeAdjacence.get(dernier)) {
                // On peut utiliser un nœud s'il n'est pas déjà dans le chemin OU s'il fait partie des nœuds valides
                if (!chemin.contains(voisin) && (!voisin.isUsed() || listeNoeudsMotsValides.contains(voisin))) {
                	List<Noeud> nouveauChemin = new ArrayList<>(chemin);
                    nouveauChemin.add(voisin);
                    queue.add(nouveauChemin);
                }
            }
        }
        return Collections.emptyList();
    }

    private boolean peutPlacerMot(String mot, List<Noeud> chemin) {
        for (int i = 0; i < mot.length(); i++) {
            Noeud noeud = chemin.get(i);
            // Vérifier si le nœud est déjà utilisé ou si c'est la case d'arrivée
            if (noeud.isUsed() || noeud.equals(arrivee)) {
                return false; // Le mot ne peut pas être placé ici
            }
        }
        return true; // Le mot peut être placé
    }

    private void insererMotDansChemin(String mot, List<Noeud> chemin) {
    	
        for (int i = 0; i < mot.length(); i++) {
            Noeud noeud = chemin.get(i);
            char lettre = mot.charAt(i);
            
            if (!noeud.equals(arrivee)) { // Ne pas ecraser la case d'arrivée surtout dans niveau 3 
            	noeud.setLettre(mot.charAt(i));
                noeud.setUsed(true); // marquer le noeud comme used
           
                    if (caracteresRaresDict.contains(lettre) && noeudsRares.size()< getNombreLettreRaresàPlacer(niveau) ) {
                 	noeudsRares.put(lettre, noeud);
                	motRares.add(mot); 
                }
            
            }
        }
    } 
    private int getNombreLettreRaresàPlacer ( int niveau) {
    	int nbr ; 
    	switch (niveau) {
    	case 1 : nbr = 1; break;
    	case 2 :  nbr = 2; break;
    	case 3 :  nbr = 3; break;
    	default : nbr = 0; break;
    	}
    	return nbr;
    }
    private int getNombreMaxMots(int niveau) {
        switch (niveau) {
            case 1: return 4;  
            case 2: return 5;  
            case 3: return 7;  
            default: return 0;  
        }
    }
    public List<Noeud> getNoeuds() {
        return listeAdjacence.keySet().stream()
            .filter(Objects::nonNull) // Filtrer les nœuds null
            .sorted(Comparator.comparing(Noeud::getId)) // Tri basé sur l'ID
            .collect(Collectors.toList());
    } 
    public Noeud getDepart() { return depart; }
    public Noeud getArrivee() { return arrivee; }
    public Map<Noeud, List<Noeud>> getListeAdjacence() { return listeAdjacence; }
}
