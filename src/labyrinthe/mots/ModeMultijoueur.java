package labyrinthe.mots;

 import java.util.ArrayList;
import java.util.HashMap; 
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set; 
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform; 
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog; 
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox; 
import javafx.scene.layout.VBox; 
import javafx.scene.control.ButtonBar; 
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
 
public class ModeMultijoueur {
	private Graphe grapheJoueur1;
    private Graphe grapheJoueur2;
    private Noeud positionDepart1;
    private Noeud positionDepart2;
    private Noeud positionArrivee1;
    private Noeud positionArrivee2;
    private List<Noeud> cheminOptimal ;
    private GrilleJeu grilleJeu;
    private Timeline timer;
    private Label motLabel1 ;
    private Label motLabel2 ;
    private Noeud positionCouranteJoueur1;
    private Noeud positionCouranteJoueur2;
    private List<Noeud> cheminJoueur1 = new ArrayList<>();
    private List<Noeud> cheminJoueur2 = new ArrayList<>();
    private int scoreJoueur1;
    private int scoreJoueur2;
    private Map<Noeud, Button> boutonsMapJoueur1 = new HashMap<>();
    private Map<Noeud, Button> boutonsMapJoueur2 = new HashMap<>();
    private GridPane grilleJoueur1;
    private GridPane grilleJoueur2;
    private Label labelJoueur1;
    private Label labelJoueur2;
    private int tempsRestant = 120;
    private boolean victoire = false;
     private Label tempsLabel; 
    private int niveau ; 
    private Scene scene ; 
    private String motParcours1 ; 
    private String motParcours2 ; 
    private Stage primaryStage;
    private ArrayList<String> listeMotForme1;
    private ArrayList<String> listeMotForme2; 
    private InterfaceGraphique interfaceGraphique ;
     private Set<String> motRares ;
    private String motsListes1 ;
    private String motsListes2;
    private Dictionnaire dictionnaire;
    
 

    public ModeMultijoueur() { 
    	this.interfaceGraphique = new InterfaceGraphique();
     } 
    public void initialiserModeMultijoueur(Stage primaryStage, String theme, int niveau) {
         this.niveau = niveau;
         dictionnaire = new Dictionnaire();
        dictionnaire.chargerDictionnaire(theme);
    	 scoreJoueur1 = 0;
    	 scoreJoueur2 = 0; 
    	this.primaryStage = primaryStage;
    	 motParcours1 = "";
         motParcours2 = "";
        listeMotForme1 = new ArrayList<String>();
        listeMotForme2 = new ArrayList<String>();
      	 cheminOptimal = new ArrayList<Noeud>();
     	
        initialiserDepartArrivee(theme);
        afficherFenetre();
         configurerEvenementsJoueur1();
        if (niveau != 3 ) {
        	demanderPositionDepartJoueur2();
        } 
        configurerEvenementsJoueur2(); 
        if (niveau == 3) { //demarrer timer 
            demarrerTimer(true, tempsLabel, primaryStage);
        }
        
    }  
     public void initialiserDepartArrivee(String theme) {

     	try {
             this.grapheJoueur1 = new Graphe(theme, niveau);
             this.grapheJoueur2 =  (Graphe) grapheJoueur1.clone(); // Clone identique 
             grilleJeu = new GrilleJeu(theme);
             motRares = grapheJoueur1.getMotsRares();
             System.out.println("Les mot(s) Rare(s) : " + motRares);
  
         } catch (Exception e) { 
             e.printStackTrace();
             throw new IllegalStateException("Erreur lors de l'initialisation du graphe.", e);
         }  
     	this.positionArrivee1 = grapheJoueur1.getArrivee();  
     	this.positionArrivee2 = grapheJoueur2.getArrivee();  
     	 
     	if (niveau == 3) {
             this.positionDepart1 = grapheJoueur1.getDepart(); 
             this.positionDepart2 = grapheJoueur2.getDepart(); 
             cheminOptimal = grapheJoueur1.BFS(positionDepart1, positionArrivee1);
       // meme pour 1 et 2
         } else { 
         	this.positionDepart1 =  null;
         	 this.positionDepart2 = null; 
         	 
         }   
     	 
      	 this.positionCouranteJoueur1 = null;
          this.positionCouranteJoueur2 = positionDepart2;

     }
    public void afficherFenetre() {
    	grilleJoueur1 = new GridPane();
        grilleJoueur2 = new GridPane(); 

        InterfaceGraphique.configurerGrille(
        		grilleJoueur1, grapheJoueur1, boutonsMapJoueur1, positionArrivee1, positionDepart1, niveau, noeud -> {
                   gererClicSurCaseMultijoueur(noeud, boutonsMapJoueur1.get(noeud),1);
                }
            ); 
        InterfaceGraphique.configurerGrille(
        		grilleJoueur2, grapheJoueur2, boutonsMapJoueur2, positionArrivee2, positionDepart2, niveau, noeud -> {
                   gererClicSurCaseMultijoueur(noeud, boutonsMapJoueur2.get(noeud),2);
                }
            );   
    	labelJoueur1 = new Label("Joueur 1");
        labelJoueur2 = new Label("Joueur 2");
        tempsLabel = new Label("Temps restant : " + tempsRestant + "s");
       
        // Appliquer des styles aux labels
        labelJoueur1.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        labelJoueur2.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        tempsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        
        // Ajouter affichage du mot formé sous le label du joueur
        motLabel1 = new Label("Mot : ");
        motLabel2 = new Label("Mot : ");
        
        // Appliquer des styles aux labels de mot
        motLabel1.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        motLabel2.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

 
        VBox joueur1Box = new VBox(10, labelJoueur1, motLabel1, grilleJoueur1);
        VBox joueur2Box = new VBox(10, labelJoueur2, motLabel2, grilleJoueur2);
        joueur1Box.setAlignment(Pos.CENTER);
        joueur2Box.setAlignment(Pos.CENTER);

     // Conteneur principal des joueurs
        HBox joueursBox = new HBox(50, joueur1Box, joueur2Box);
        joueursBox.setAlignment(Pos.CENTER);

        // Ajouter le temps au-dessus
        VBox root = new VBox(20, tempsLabel, joueursBox);
        root.setAlignment(Pos.CENTER); // Centrer tous les éléments

         scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Mode Multijoueur");
        primaryStage.show();
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
                    Platform.runLater(() -> { // Exécuter sur le thread principal JavaFX
                 if ( tempsRestant == 0) { // on ne fait pa ici le trateiemnt du victoire 
                	  scoreJoueur1 = calculerListeMotsFormeEtScore(motParcours1, listeMotForme1, scoreJoueur1).getValue();
                	 scoreJoueur2 = calculerListeMotsFormeEtScore(motParcours2, listeMotForme2, scoreJoueur2).getValue(); 
                	// setScoreMotsForme();   
                	 String mssg = "";
                            if (scoreJoueur1 > scoreJoueur2) {
                                mssg = "Temps écoulé ! Joueur 1 gagne avec " + scoreJoueur1 + " points !";
                            } else if (scoreJoueur2 > scoreJoueur1) {
                                mssg = "Temps écoulé ! Joueur 2 gagne avec " + scoreJoueur2 + " points !";
                            } else {
                                mssg = "Temps écoulé ! Match nul !";
                            } 
                            InterfaceGraphique.afficherAlerte("Fin de la partie", mssg, Alert.AlertType.INFORMATION);
                            new InterfaceGraphique().start(stage); 
                            
                    }
                 });
             }
        }); 
        timer = new Timeline(keyFrame);
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play(); 	
    } 
 
 
    private void gererClicSurCaseMultijoueur(Noeud noeud, Button btnCase, int joueur) {
        btnCase.setStyle("-fx-background-color: #ffde75;");
        String mssg = "";
    	if (joueur == 1) {
            cheminJoueur1.add(noeud); 
            motParcours1 += noeud.getLettre(); 
            motLabel1.setText("Mot formé : " + motParcours1);
            if (noeud.equals(positionArrivee1)) {
                victoire = true;
                scoreJoueur1 = scoreJoueur1 +5;
                mssg = "Joueur 1 a sorti du labyrinthe (+5 points)\n"; 
               afficherGagnant(1,mssg);
            }
        } else if (joueur == 2) {
            cheminJoueur2.add(noeud); 
            motParcours2 += noeud.getLettre(); 
            motLabel2.setText("Mot formé : " + motParcours2);
             if (noeud.equals(positionArrivee2)) {
                victoire = true;
                scoreJoueur2 = scoreJoueur2 + 5;
                mssg = "Joueur 2 a sorti du labyrinthe (+5 points)\n"; 
                afficherGagnant(2,mssg);
            }
        }
    }  
    public void caclulerScoreListeMotsFormesJoueurX() {
    	 Pair<String, Integer> resultatJoueur1 = calculerListeMotsFormeEtScore(motParcours1, listeMotForme1, scoreJoueur1);
         Pair<String, Integer> resultatJoueur2 = calculerListeMotsFormeEtScore(motParcours2, listeMotForme2, scoreJoueur2);

          motsListes1 = resultatJoueur1.getKey();
          scoreJoueur1 = resultatJoueur1.getValue();
          motsListes2 = resultatJoueur2.getKey();
          scoreJoueur2 = resultatJoueur2.getValue();
    }
    
    private Pair<String, Integer> calculerListeMotsFormeEtScore(String motParcours, ArrayList<String> listeMotForme, int scoreJoueur) {
        boolean existeMotValide = grilleJeu.contientMotValide(motParcours, listeMotForme);
        String motsListe = "";

        if (existeMotValide) {
            for (String mot : listeMotForme) {
                String foisDeux = ""; // pour les mots rares
                if (motRares.contains(mot)) { // mot Rare
                    foisDeux = " (x2) RARE !";
                    scoreJoueur = scoreJoueur + ((mot.length()) * 2);
                } else {
                    scoreJoueur = scoreJoueur + mot.length();
                }
                motsListe = motsListe + mot + " (+" + mot.length() + ")" + foisDeux + "\n";
            }
        }
        return new Pair<>(motsListe, scoreJoueur);
    }
    private void afficherGagnant(int joueur, String mssg) {
    	caclulerScoreListeMotsFormesJoueurX();
        Platform.runLater(() -> { 
            String message = ""; 
            message = message + mssg + "\n";
            message += "---- Joueur 1 ----  SCORE  : " + scoreJoueur1 + "\n";
            if (!motsListes1.equals("")) {
            	message += "Mots formés :\n";
                message += motsListes1 + "\n"; 
            } 
            message += "---- Joueur 2 ----  SCORE  : " + scoreJoueur2 + "\n";
            if (!motsListes2.equals("")) {
            	message += "Mots formés :\n";
                message += motsListes2 + "\n"; 
            } //  qui a gagné ? 
            
            if (scoreJoueur1 > scoreJoueur2) {
                message += "🏆 Le Joueur 1 a gagné !\n\n";
            } else if (scoreJoueur1 < scoreJoueur2) {
                message += "🏆 Le Joueur 2 a gagné !\n\n";
            } else {
                message += "Match nul !\n\n"; 
            }
            InterfaceGraphique.afficherAlerte("Fin de la partie", message,Alert.AlertType.INFORMATION);
            interfaceGraphique.start(primaryStage);  
        });
    }
    private void configurerEvenementsJoueur1() {
     for (Map.Entry<Noeud, Button> entry : boutonsMapJoueur1.entrySet()) {
            Noeud noeud = entry.getKey();
            Button btnCase = entry.getValue();

            btnCase.setOnMouseClicked(_ -> { 
            	if (niveau == 3) {  
                    if (positionCouranteJoueur1 == null) { 
                        positionCouranteJoueur1 = noeud;
                        interfaceGraphique.activerVoisins(positionCouranteJoueur1, boutonsMapJoueur1, grapheJoueur1);
 
                    } else if (grapheJoueur1.getListeAdjacence().get(positionCouranteJoueur1).contains(noeud)) {
                        positionCouranteJoueur1 = noeud; 
                        interfaceGraphique.activerVoisins(positionCouranteJoueur1, boutonsMapJoueur1, grapheJoueur1);
                    } 
                } else { // niv 1/2
                    if (positionCouranteJoueur1 == null) {
                        if (noeud.estBloque() || noeud.equals(positionArrivee1)) {
                            return;
                        }
                        else if (grapheJoueur1.getListeAdjacence().get(positionArrivee1).contains(noeud)) { 
                        	InterfaceGraphique.afficherAlerte(null,"Too close!", Alert.AlertType.WARNING);  
                        } else { 
                        	positionCouranteJoueur1 = noeud; 
                        cheminOptimal = grapheJoueur1.BFS(noeud, positionArrivee1);
                        if (!cheminOptimal.isEmpty()) {  
                        	interfaceGraphique.activerVoisins(positionCouranteJoueur1, boutonsMapJoueur1, grapheJoueur1);
                        } else { 
                        	interfaceGraphique.afficherAlerteEchec(primaryStage,1);
                        }
                        }
                    } else if (grapheJoueur1.getListeAdjacence().get(positionCouranteJoueur1).contains(noeud)) {
                    	if (!cheminOptimal.isEmpty()) {
                    	positionCouranteJoueur1 = noeud; 
                        interfaceGraphique.activerVoisins(positionCouranteJoueur1, boutonsMapJoueur1, grapheJoueur1);
                    }}
                }
            });
        }
    }
    private void configurerEvenementsJoueur2() {
        scene.setOnKeyPressed(event -> {
        	if ( niveau !=3) {
        		if (positionCouranteJoueur2 == null) {
                demanderPositionDepartJoueur2(); // Demander la position avant tout
                return;  } 
         } 
            Noeud nouvellePosition = null;
            switch (event.getCode()) {
                case NUMPAD8: nouvellePosition = getNoeudVoisin(grapheJoueur2, positionCouranteJoueur2, -1, 0); break;
                case NUMPAD2: nouvellePosition = getNoeudVoisin(grapheJoueur2, positionCouranteJoueur2, 1, 0); break;
                case NUMPAD4: nouvellePosition = getNoeudVoisin(grapheJoueur2, positionCouranteJoueur2, 0, -1); break;
                case NUMPAD6: nouvellePosition = getNoeudVoisin(grapheJoueur2, positionCouranteJoueur2, 0, 1); break;
                case NUMPAD7: nouvellePosition = getNoeudVoisin(grapheJoueur2, positionCouranteJoueur2, -1, -1); break;
                case NUMPAD9: nouvellePosition = getNoeudVoisin(grapheJoueur2, positionCouranteJoueur2, -1, 1); break;
                case NUMPAD1: nouvellePosition = getNoeudVoisin(grapheJoueur2, positionCouranteJoueur2, 1, -1); break;
                case NUMPAD3: nouvellePosition = getNoeudVoisin(grapheJoueur2, positionCouranteJoueur2, 1, 1); break;
                default: break; // Ignorer les autres touches
            } 
            // chemin et noeud valides
            if (nouvellePosition != null && !cheminOptimal.isEmpty()) { 
                positionCouranteJoueur2 = nouvellePosition; 
                Button btnCase = boutonsMapJoueur2.get(nouvellePosition);
                if (btnCase != null) { 
                    gererClicSurCaseMultijoueur(nouvellePosition, btnCase, 2);
                    interfaceGraphique.activerVoisins(positionCouranteJoueur2, boutonsMapJoueur2, grapheJoueur2);
                }
            } else if (nouvellePosition == null) { 
            	// noeud bloqué , espace , num 5 ...
                return;
            }else{
            	//  cas bloqué dans labyrinthe pas de chemin 
                interfaceGraphique.afficherAlerteEchec(primaryStage,2);
            }
        });
    }  
    private void demanderPositionDepartJoueur2() {
        while (true) { // Boucle jusqu'à ce qu'une position valide soit saisie
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Choix de la position de départ, Ne sois pas piégé !");
            dialog.setHeaderText("Entrez les coordonnées du nœud de départ (X-Y) :");
            dialog.setContentText("Exemple : 3-5");

            Optional<String> result = dialog.showAndWait();

            // Si l'utilisateur ferme ou annule le popup, redirection vers InterfaceGraphique
            if (result.isEmpty()) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirmation de sortie");
                confirmation.setHeaderText("Voulez-vous vraiment quitter ?"); 

                ButtonType oui = new ButtonType("Oui");
                ButtonType non = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmation.getButtonTypes().setAll(oui, non);

                Optional<ButtonType> confirmationResult = confirmation.showAndWait();

                // Si l'utilisateur confirme, redirection vers InterfaceGraphique
                if (confirmationResult.isPresent() && confirmationResult.get() == oui) {
                    try {
                        new InterfaceGraphique().start(primaryStage); // Redirection
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break; // Sortir de la boucle
                } else {
                    continue; // Revenir à la boucle si l'utilisateur refuse
                }
            }

            try {
                String input = result.get().trim();
                if (!input.matches("\\d+-\\d+")) throw new NumberFormatException(); //  format x-y
                String[] coords = input.split("-");
                int x = Integer.parseInt(coords[0]) - 1;   // ligne de grille 
                int y = Integer.parseInt(coords[1]) - 1; // colonne de grille 
                String id = x + "-" + y;

                Noeud noeudChoisi = grapheJoueur2.getNoeudParId(id);

                // Vérification si le nœud est valide
                if (noeudChoisi == null || noeudChoisi.estBloque() || noeudChoisi.equals(grapheJoueur2.getArrivee())) {
                    InterfaceGraphique.afficherAlerte(null,"Impossible de choisir ce nœud !", Alert.AlertType.ERROR);
                    continue; // Relancer si position invalide
                }

                // Vérifier si un chemin existe
                cheminOptimal = grapheJoueur2.BFS(noeudChoisi, positionArrivee2);
                if (cheminOptimal == null || cheminOptimal.isEmpty()) {
                	interfaceGraphique.afficherAlerteEchec(primaryStage,2); break; 
                }

                // OK : On valide la position
                positionDepart2 = noeudChoisi;
                positionCouranteJoueur2 = noeudChoisi;
                

                // Afficher en jaune la case sélectionnée
                Button btnCase = boutonsMapJoueur2.get(positionDepart2);
                if (btnCase != null) {
                    btnCase.setStyle("-fx-background-color: #ffde75;");
                }
                motParcours2 += positionDepart2.getLettre();
                if (niveau == 1 || niveau == 2) {
                	InterfaceGraphique.demarrerCompteARebours(tempsLabel, primaryStage, () -> {
                	    // Ce code sera exécuté après la fin du compte à rebours
                	    demarrerTimer(true, tempsLabel, primaryStage);
                	});
                }
                break; // Sortir de la boucle une fois une position correcte choisie
            } catch (NumberFormatException e) {
                InterfaceGraphique.afficherAlerte(null,"Impossible de choisir ce nœud !", Alert.AlertType.ERROR);
            }
        }
    } 
    private Noeud getNoeudVoisin(Graphe graphe, Noeud noeud, int deltaRow, int deltaCol) {
        int row = Integer.parseInt(noeud.getId().split("-")[0]) + deltaRow;
        int col = Integer.parseInt(noeud.getId().split("-")[1]) + deltaCol;
        for (Noeud n : graphe.getNoeuds()) {
            if (n.getId().equals(row + "-" + col) && !n.estBloque()) {
                return n;
            }
        }
        return null;
    }    
}