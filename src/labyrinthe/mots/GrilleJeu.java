package labyrinthe.mots;

import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*; 
import javafx.stage.Stage;
public class GrilleJeu {
	private Graphe graphe;
    private Noeud positionDepart;
    private Noeud positionArrivee;
    private Noeud positionCourante;
    private Dictionnaire dictionnaire;
    private String motForme;
    private int score;
    private Label motFormeLabel, tempsLabel, hintsLabel;
    public int niveau;
    private ArrayList<String> listeMotFormes;
    private String theme;
    private Map<Noeud, Button> boutonsMap;
    private int tempsRestant ;
    private boolean victoire;
    private int tentative;
    private ListView<String> motsValidesList;
    private List<Noeud> cheminOptimal;
    private List<Noeud> cheminJoueur;
    private int distanceParcourue ;
    private Timeline timer;
    private Stage stage ; 
    private  Scene scene;
    private Set<String> motsRares; 
    private boolean multijoueurMode;  
    private GridPane gridPane;
    private InterfaceGraphique interfaceGraphique; 
    private int hintsRestants; // Nombre de hints disponibles selon niveau
    private int indexMotActuel; // Index du mot actuel dans la liste des mots valides
    private int nbrMotParcourusChemin ;
    private Button btnValider;
    
    public GrilleJeu(Graphe graphe, Stage stage, String theme, int niveau) {
        this.graphe = graphe;
        dictionnaire = new Dictionnaire();
        dictionnaire.chargerDictionnaire(theme);
        interfaceGraphique = new InterfaceGraphique();
        this.stage = stage;
        this.theme = theme;
        this.niveau = niveau;
        multijoueurMode = false;
        initialiserVariables(); 
        if (!multijoueurMode) {
        	configurerFenetre(stage);
        }
    }
    private void initialiserVariables() {
    	boutonsMap = new HashMap<>(); 
        positionDepart = graphe.getDepart();
        positionArrivee = graphe.getArrivee();
        motForme = "";
        score = 0;
        listeMotFormes = new ArrayList<>();
        cheminJoueur = new ArrayList<>();
        motsRares = graphe.getMotsRares();
        distanceParcourue = 0;
	    tempsRestant = 120;
	    victoire = false;
        tentative = nbrTentativesMax(niveau);
        nbrMotParcourusChemin = 0;
	    hintsRestants = nbrHintRestants(niveau);
	    indexMotActuel = 0;
	    positionCourante = null;
    }
    
    private int nbrHintRestants(int niveau) {
    	int hintsRestants ; 
    	switch (niveau) {
          case 1: hintsRestants = 3; break;
          case 2: hintsRestants = 4; break;
          case 3: hintsRestants = 5; break;
          default: hintsRestants = 0; // Par défaut, aucun hint
              break;
      }
    	  return hintsRestants ;
    }
    private int nbrTentativesMax (int niveau) {
    	int nbr ; 
    	switch (niveau) {
    	case 1 : nbr = 2; break;
    	case 2 :  nbr = 3; break; 
    	default : nbr = 0; break;
    	}
    	return nbr;
    }
     
    public GrilleJeu(String theme) {  //  constructeur mode Multijoueur 
    	this.theme = theme;
        this.dictionnaire = new Dictionnaire();  // Initialization
        this.dictionnaire.chargerDictionnaire(theme); 
    } 

	public void configurerFenetre(Stage primaryStage) { 
        // Création de la grille
         gridPane = new GridPane();
         
        InterfaceGraphique.configurerGrille(
                gridPane, graphe, boutonsMap, positionArrivee, positionDepart, niveau, noeud -> {
                   gererClicSurCase(noeud, boutonsMap.get(noeud));
                }
            ); 
 
        // Création des labels
        if (niveau !=3) {
        	 motFormeLabel = new Label("Mot formé : ");
             motFormeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        }
        tempsLabel = new Label("Temps restant : " + tempsRestant + "s");
        tempsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        if (niveau != 3) {
        	  // Création de la liste des mots valides
            motsValidesList = new ListView<>();
            motsValidesList.setPrefSize(150, 300);
            motsValidesList.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-padding: 10px;");
            
        }
        btnValider = new Button("Valider");
        Button btnHome = new Button("Home");
        btnHome.setStyle("-fx-background-color: #6699ff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #cccccc; -fx-border-width: 1px;");

        // Gestion des événements
        if (niveau != 3) {
        	 // Création des boutons
            
            btnValider.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-opacity: 1;");
            btnValider.setOnAction(_ -> validerMot());
 
        }

        Button btnHint = new Button("Hint");
        btnHint.setStyle("-fx-background-color: #ffcc00; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        btnHint.setOnAction(_ -> donnerHint()); // Gestionnaire d'événements pour le bouton Hint
        hintsLabel = new Label("Hints restants : " + hintsRestants);
        hintsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");//afficher le nombre de hints restants
        
        
         btnHome.setOnAction(_ -> { 
            primaryStage.close();
            new InterfaceGraphique().start(primaryStage); 
             
        });

        // Layout principal
        BorderPane root = new BorderPane();
        root.setCenter(gridPane);

        // Création des conteneurs pour les labels et les boutons
        HBox topBox ; 
        if ( niveau != 3) {
             topBox = new HBox(20, motFormeLabel, tempsLabel,hintsLabel);
        }else {
        	topBox = new HBox(20, tempsLabel,hintsLabel);
        }
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER);

        HBox bottomBox;
        if (niveau != 3) {
            bottomBox = new HBox(20, btnValider, btnHint, btnHome);
        } else {
            bottomBox = new HBox(20 , btnHint,btnHome);
        }
        bottomBox.setPadding(new Insets(20));
        bottomBox.setAlignment(Pos.CENTER);

        root.setTop(topBox);
        root.setRight(motsValidesList);
        root.setBottom(bottomBox);

        // Configuration de la scène
        scene = new Scene(root, 750, 750);
         primaryStage.setScene(scene);
        primaryStage.setTitle("Labyrinthe de Mots");
        primaryStage.show();
        	// timer pour mode solo avec modeMultijoueur = false 
         demarrerTimer(multijoueurMode, this.tempsLabel, stage); 
    }
	private void donnerHint() {
	    // Vérifier s'il reste des hints
	    if (hintsRestants <= 0) {
	        Alert alert = new Alert(Alert.AlertType.WARNING, "Vous n'avez plus de hints disponibles.");
	        alert.showAndWait();
	        return;
	    }

	    // Récupérer tous les mots placés dans la grille
	    Set<String> motsPlaces = graphe.getTousLesMotsPlaces(); // Utilisez tousLesMotsPlaces

	    if (motsPlaces.isEmpty()) {
	        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucun mot valide trouvé.");
	        alert.showAndWait();
	        return;
	    }

	    // Convertir l'ensemble des mots en une liste pour accéder aux éléments par index
	    List<String> listeMots = new ArrayList<>(motsPlaces);

	    // Vérifier si l'index est valide
	    if (indexMotActuel >= listeMots.size()) {
	        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Tous les mots ont été parcourus.");
	        alert.showAndWait();
	        return;
	    }

	    // Récupérer le mot actuel
	    String motValide = listeMots.get(indexMotActuel);

	    // Récupérer le chemin du mot
	    List<Noeud> cheminDuMot = graphe.getCheminsDesMots().get(motValide);

	    if (cheminDuMot == null || cheminDuMot.isEmpty()) {
	        Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur : Impossible de trouver le chemin du mot.");
	        alert.showAndWait();
	        return;
	    }

	    // Récupérer le premier nœud du chemin (première lettre du mot)
	    Noeud noeudDebut = cheminDuMot.get(0);

	    // Colorer la case correspondante
	    Button btnCase = boutonsMap.get(noeudDebut);
	    if (btnCase != null) {
	        btnCase.setStyle("-fx-background-color: #FF0000; -fx-opacity: 1;"); // Couleur pour le hint
	    } else {
	        Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur : Case introuvable.");
	        alert.showAndWait();
	        return;
	    }

	    // Passer au mot suivant pour le prochain hint
	    indexMotActuel++;

	    // Réduire le nombre de hints restants
	    hintsRestants--;
	    // reduire le score -2 par hint
	    score = score -2 ;
	    // Mettre à jour le Label des hints restants
	    hintsLabel.setText("Hints restants : " + hintsRestants);

	    // Afficher le nombre de hints restants dans une alerte (optionnel)
	    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Hint utilisé ! Il vous reste " + hintsRestants + " hints.\nTu seras sanctionné de 2 points");
	    alert.showAndWait();
	} 
    public void demarrerTimer(boolean multijoueur, Label tempsLabel, Stage stage) {
    	 
    	if (timer != null) {
            timer.stop(); // Arrêter le timer s'il est déjà en cours
        }
    	tempsRestant = 120; // Initialiser à 120 secondes
    	tempsLabel.setText("Temps restant : " + tempsRestant + "s");
    	
    	KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), _ -> {
            tempsRestant--;
            tempsLabel.setText("Temps restant : " + tempsRestant + "s");

            if (tempsRestant == 0 || victoire) {
                timer.stop();
                if (!victoire) {
                    Platform.runLater(() -> { // Exécuter sur le thread principal JavaFX
                    	InterfaceGraphique.afficherAlerte(null,"Temps écoulé !", Alert.AlertType.WARNING);
                        reinitialiserGrille(theme, niveau);  
                    });
                }
            }
        }); 
        timer = new Timeline(keyFrame);
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play(); 	
    }
 
    private void afficherCheminOptimal() {
        if (niveau == 3) {
            cheminOptimal = graphe.getCheminOptimal();
        }
        for (Noeud noeud : cheminOptimal) {
            Button btnCase = boutonsMap.get(noeud);
            if (btnCase != null) {
            	btnCase.setStyle("-fx-background-color: cyan;"); }
        }
    }  
    protected void gererClicSurCase(Noeud noeud, Button btnCase) {
        cheminJoueur.add(noeud);
        motForme += noeud.getLettre();
       btnCase.setStyle("-fx-background-color: #ffde75;");
       
        distanceParcourue++;
        if (noeud.equals(positionArrivee)) {
            victoire = true;
        }
        if (niveau == 3) {
            gererClicNiveau3(noeud, btnCase);
        } else {
        	motFormeLabel.setText("Mot formé : " + motForme);
            gererClicNiveaux1et2(noeud, btnCase);
        }
    }

    private void gererClicNiveau3(Noeud noeud, Button btnCase) {  
        if (positionCourante == null) {
            positionCourante = noeud; 
            interfaceGraphique.activerVoisins(positionCourante, boutonsMap, graphe);
        }
        else if (graphe.getListeAdjacence().get(positionCourante).contains(noeud)) {
            positionCourante = noeud; 
            interfaceGraphique.activerVoisins(positionCourante, boutonsMap, graphe);
            if (noeud.equals(positionArrivee)) {
            	victoire = true;
                verifierEtGererFinDePartie(cheminJoueur);
            }
        } else { 
            InterfaceGraphique.afficherAlerte(null, "Déplacement invalide !",Alert.AlertType.ERROR);
            
        } 
    }

    private void gererClicNiveaux1et2(Noeud noeud, Button btnCase) {
        if (positionDepart == null) {
            if (noeud.equals(positionArrivee)) {
                InterfaceGraphique.afficherAlerte(null, "Ce nœud ne peut pas être choisi comme point de départ.",Alert.AlertType.ERROR);
                return; 
            }  else if (graphe.getListeAdjacence().get(positionArrivee).contains(noeud)) { 
                InterfaceGraphique.afficherAlerte(null, "Too close!",Alert.AlertType.WARNING);
            } else {
                positionDepart = noeud;
                positionCourante = noeud;  
                 cheminOptimal = graphe.BFS(noeud, positionArrivee); 
                if (!cheminOptimal.isEmpty()) {  
                	interfaceGraphique.activerVoisins(positionCourante, boutonsMap, graphe);
                } else { 
                    InterfaceGraphique.afficherAlerte(null, "Échec ! Tu es resté piégé dans le labyrinthe ! ",Alert.AlertType.ERROR);
                 	reinitialiserGrille(theme, niveau );
                }
            } 
        } else if (positionCourante != null && graphe.getListeAdjacence().get(positionCourante).contains(noeud)) {
             if (!cheminOptimal.isEmpty()) {
                positionCourante = noeud;  
                interfaceGraphique.activerVoisins(positionCourante, boutonsMap, graphe);
                if (noeud.equals(positionArrivee)) {
                	victoire = true;
                    verifierEtGererFinDePartie(cheminJoueur);
                }
            } 
        }
    }  
    private int nbrMotsParChemin (int niveau) {
    	int nbr ; 
    	switch (niveau) {
    	case 1 : nbr = 2; break;
    	case 2 :  nbr = 3; break;
    	case 3 :  nbr = 4; break;
    	default : nbr = 0; break;
    	}
    	return nbr;
    }
    private void verifierEtGererFinDePartie(List<Noeud> cheminJoueur) {
        boolean contientMot = contientMotValide(motForme, listeMotFormes);
        String messageFinal = ""; 
        
        // minimum de mots pour avoir victoire
        if (listeMotFormes.size() < 1) { 
        	messageFinal = "Tu es sorti du Labyrinthe Mais sans former de mots!";
        	
        }else { //  il est sorti du Labyrinthe avec aux moins 1 mot  
        	String victoireMsg = "";
        	victoireMsg = "🏆 Bravo, tu es sorti du Labyrinthe !";
        	
        	  
        	// si on trouve plusierus mots dans un seul chemin on ajoute +10 Bonus Supplémentaire 
        if ( nbrMotParcourusChemin >= nbrMotsParChemin(niveau)) { 
        	score = score + 10;
        	victoireMsg = victoireMsg + " " + nbrMotParcourusChemin + " mots dans un seul chemin. Impressionnant !";
        }
        
        int distanceOptimalBFS = graphe.getLongueurPlusCourtChemin();
        distanceParcourue--;  
        String estOptimal;

        // Vérifier si le chemin est optimal
        if (distanceParcourue == distanceOptimalBFS) {
            score += 5;
            estOptimal = "\nTu as trouvé le plus court chemin ! (+5 points)";
        } else {
            estOptimal = "\nAttention : Ce n'était pas le plus court chemin.\nUn exemple de parcours optimal sera affiché en CYAN.";
            afficherCheminOptimal();
        }

        // Construire la liste des mots formés
        String motsListes = "";
        if (!listeMotFormes.isEmpty()) {
            for (String mot : listeMotFormes) {
                String foisDeux = "";
                if (motsRares.contains(mot)) {
                    foisDeux = " (x2) RARE !";
                    score += mot.length(); // Ajouter le score pour les mots rares
                }
                motsListes += mot + " (+" + mot.length() + ")" + foisDeux + "\n";
            }
        }
        // Message des mots formés
        String motsFormesMsg = (contientMot || !listeMotFormes.isEmpty())
                ? "Mots valides formés :\n" + motsListes
                : "Aucun mot formé";

        // Construire le message final
         messageFinal = victoireMsg + "\n" + estOptimal + "\n\n" + motsFormesMsg + "\n\nScore Final : " + score; 
        }
        
        InterfaceGraphique.afficherAlerte("Victoire !", messageFinal, Alert.AlertType.INFORMATION);
        reinitialiserGrille(theme, niveau);
    }
    
  
    public boolean contientMotValide(String motForme, ArrayList<String> listeMotFormes ) {
        boolean auMoinsUnMotValide = false;
        for (int i = 0; i < motForme.length(); i++) {
            for (int j = i + 1; j <= motForme.length(); j++) {
                String sousChaine = motForme.substring(i, j);
                if (dictionnaire.estMotValide(sousChaine) && !listeMotFormes.contains(sousChaine)) {
                	// ajouter le mot a la liste des mots formés
                    score += sousChaine.length();  
                    listeMotFormes.add(sousChaine);
                    auMoinsUnMotValide = true;
                    if (victoire && !multijoueurMode) {  
                    	 nbrMotParcourusChemin ++ ;
                    }                   
                }
            }
        }
        return auMoinsUnMotValide;
    }
    private void reinitialiserGrille(String theme, int niveau) {
    	if (timer != null) {
            timer.stop();
        }
    	this.theme =theme;
        this.niveau = niveau; 
    	// Réinitialiser les données du jeu
        this.graphe = new Graphe(theme, niveau); // Recréer le graphe avec le même thème et niveau
         initialiserVariables(); 
        configurerFenetre(stage);
        
    }
    private void reinitialiserMotEtChemin() {
        
    	motForme = "";
        cheminJoueur.clear();
        distanceParcourue = 0;  
        if (niveau == 1 || niveau == 2) {
            positionDepart = null ; 
            motFormeLabel.setText("Mot formé : ");
        }
        positionCourante = positionDepart;
        for (Noeud noeud : graphe.getNoeuds()) {
            Button btnCase = boutonsMap.get(noeud);
 
             if (niveau != 3) {  btnCase.setDisable(false);}
             boolean estNoeudRare = graphe.getNoeudsRares().containsValue(noeud);
             boolean estArrivee = noeud.equals(positionArrivee);
             boolean estDepart = noeud.equals(positionDepart);
             boolean estBloque = noeud.estBloque();
             InterfaceGraphique.configurerStyleBouton(btnCase, estArrivee, estDepart, estBloque, estNoeudRare);
            }
    }
     
    private void validerMot() { 
        if (listeMotFormes.contains(motForme)) { 
        	InterfaceGraphique.afficherAlerte(null,"Mot déjà trouvé", Alert.AlertType.INFORMATION);
           
        }else if (dictionnaire.estMotValide(motForme)) {  tentative -- ;
            motsValidesList.getItems().add(motForme);
            score += motForme.length();
            listeMotFormes.add(motForme);
            InterfaceGraphique.afficherAlerte(null,"Mot valide : " + motForme + "\nIl te reste " + tentative + " tentative(s) !", Alert.AlertType.INFORMATION);
        
        } else if (!motForme.isEmpty()) { tentative -- ;
            score--; // -1 pour un mot invalide
            InterfaceGraphique.afficherAlerte(null,"Mot invalide"+ "\nIl te reste " + tentative + " tentative(s) !", Alert.AlertType.ERROR);
        } // Gestion des tentatives 
        if (tentative == 0) {
         	btnValider.setDisable(true);
        }  
        reinitialiserMotEtChemin();
    }

    

}
