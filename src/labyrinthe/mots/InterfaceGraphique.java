package labyrinthe.mots;

import javafx.util.Duration; 
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer; 
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class InterfaceGraphique extends Application {
    private Graphe graphe; 
    public int niveau; 
    private String theme; 
    private Stage stage ; 

    
    @Override
    public void start(Stage primaryStage) {
    	this.stage = primaryStage;
    	afficherInterfaceIntroductive(primaryStage);
    }
    
 
    
    private void afficherInterfaceIntroductive(Stage primaryStage) {
        Label logoLabel = new Label("🎮");
        logoLabel.setFont(Font.font(48));

        Label bienvenueLabel = new Label("Bienvenue dans le Labyrinthe de Mots !");
        bienvenueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), bienvenueLabel);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(1);
        fadeTransition.play();

        Label reglesLabel = new Label("Règles du jeu :\n" +
                "1. Formez des mots en parcourant le labyrinthe.\n" + 
                "2. Validez votre mot pour gagner des points.\n" + 
                "3. Vous avez 120 secondes pour terminer le jeu.");
        reglesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555;");
 
        Button soloButton = new Button("Solo");
        soloButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        soloButton.setOnAction(_ -> { 
        	primaryStage.close();
        	this.theme = choisirTheme();
        	if (this.theme == null) return;
        	this.niveau = choisirNiveau();
        	if (this.niveau == -1) return;
        	this.graphe = new Graphe(theme, niveau);
        	new GrilleJeu(graphe, primaryStage, theme, niveau);
            
 
         });

        Button multiButton = new Button("Multijoueur");
        multiButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 16px;");
        multiButton.setOnAction(_ -> {
            primaryStage.close();
             theme = choisirTheme();
            if (this.theme == null) return;
             niveau = choisirNiveau();
            if (this.niveau == -1) return;
            ModeMultijoueur modeMultijoueur = new ModeMultijoueur();
            modeMultijoueur.initialiserModeMultijoueur(primaryStage, theme, niveau);
            });
        
        Button reglesButton = new Button("Règles");
        reglesButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px;");
        reglesButton.setOnAction(_ -> afficherRegles(primaryStage));

 
        // VBox root = new VBox(20, logoLabel, bienvenueLabel, reglesLabel, commencerButton);
        VBox root = new VBox(20, logoLabel, bienvenueLabel, reglesLabel,  soloButton, multiButton, reglesButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f4f4;");

        Scene introScene = new Scene(root, 600, 500);
        stage.setScene(introScene);
        stage.setTitle("Labyrinthe de Mots - Introduction");
        stage.show();
    }   
    

    private String choisirTheme() {
        String[] themes = {"Nourriture", "Informatique", "Voiture"};
        ChoiceDialog<String> dialog = new ChoiceDialog<>(themes[0], themes);
        dialog.setTitle("Choix du thème");
        dialog.setHeaderText("Choisissez un thème");
        dialog.setContentText("Thème :");

        Optional<String> result = dialog.showAndWait();
        
        // Si l'utilisateur ferme la boîte de dialogue ou clique sur "Annuler"
        if (result.isEmpty()) {
            Platform.exit(); // Arrête l'application immédiatement
            return null; // Retourne null, mais le programme s'arrêtera déjà
        }

        return result.get(); // Retourne le thème choisi
    }

    private int choisirNiveau() {
        String[] niveaux = {"Facile", "Moyen", "Difficile"};
        ChoiceDialog<String> dialog = new ChoiceDialog<>(niveaux[0], niveaux);
        dialog.setTitle("Choix du niveau");
        dialog.setHeaderText("Choisissez le niveau de difficulté");
        dialog.setContentText("Niveau :");

        Optional<String> result = dialog.showAndWait();

        // Si l'utilisateur ferme la boîte de dialogue ou clique sur "Annuler"
        if (result.isEmpty()) {
            Platform.exit(); // Arrête l'application immédiatement
            return -1; // Retourne -1, mais le programme s'arrêtera déjà
        }

        return Arrays.asList(niveaux).indexOf(result.get()) + 1;
    }

 // Méthode pour afficher une alerte d'échec
    public void afficherAlerteEchec(Stage primaryStage, int joueurPerdant) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Échec");
            int joueurGagnant = (joueurPerdant == 1) ? 2 : 1;
            alert.setHeaderText("Tu es resté piégé dans le labyrinthe !\nJoueur " + joueurGagnant + " a gagné !");
            alert.setContentText("Retour au menu principal.");

            ButtonType boutonHome = new ButtonType("Menu Principal", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(boutonHome);

            // Gérer la fermeture de l'alerte pour rediriger vers l'interface principale
            alert.setOnCloseRequest(_ -> {
                new InterfaceGraphique().start(primaryStage);
            });

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == boutonHome) {
                new InterfaceGraphique().start(primaryStage);
            }
        });
    }

    public static Button creerBoutonCase(Noeud noeud, Graphe graphe, Noeud positionArrivee, Noeud positionDepart, Consumer<Button> onClick) {
        Button btnCase = new Button(noeud.estBloque() ? "" : String.valueOf(noeud.getLettre()));
        btnCase.setFont(Font.font("Arial", 24));
        btnCase.setPrefSize(60, 60);

        boolean estNoeudRare = graphe.getNoeudsRares().containsValue(noeud);
        boolean estArrivee = noeud.equals(positionArrivee);
        boolean estDepart = noeud.equals(positionDepart);
        boolean estBloque = noeud.estBloque();

        configurerStyleBouton(btnCase, estArrivee, estDepart, estBloque, estNoeudRare);

     // Ajouter la logique spécifique
        if (onClick != null) {
            btnCase.setOnAction(_ -> onClick.accept(btnCase));
        }
        return btnCase;
    }

    public static void configurerStyleBouton(Button btnCase, boolean estArrivee, boolean estDepart, boolean estBloque, boolean estNoeudRare) {
        if (estArrivee) {
            btnCase.setStyle("-fx-background-color: #78c06d; -fx-text-fill: #ffffff; -fx-opacity: 1;"); // Nœud d'arrivée
            btnCase.setDisable(true); // Désactiver la sélection du nœud d'arrivée
        } else if (estDepart) {
            btnCase.setStyle("-fx-background-color: #a0c0ff; -fx-text-fill: #ffffff; -fx-opacity: 1;"); // Nœud de départ
            btnCase.setText(""); // Forcer le nœud de départ à être vide
        } else if (estBloque) {
            btnCase.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-opacity: 1;"); // Nœud bloqué
            btnCase.setDisable(true); // Désactiver les nœuds bloqués
        } else if (estNoeudRare) {
            btnCase.setStyle("-fx-background-color: #00ff26; -fx-text-fill: #ffffff; -fx-opacity: 1;"); // Nœud rare
        } else {
            btnCase.setStyle("-fx-background-color: lightgray; -fx-text-fill: #333333; "); // Nœud normal
        }
    }
    
    public static void configurerGrille(GridPane gridPane, Graphe graphe, Map<Noeud, Button> boutonsMap, Noeud positionArrivee, Noeud positionDepart,int niveau, Consumer<Noeud> onNodeClick) {
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-padding: 20px;");

        for (Noeud noeud : graphe.getNoeuds()) {
            Button btnCase = creerBoutonCase(noeud, graphe, positionArrivee, positionDepart, _ -> {
                if (onNodeClick != null) {
                    onNodeClick.accept(noeud);
                }
            });
            int row = Integer.parseInt(noeud.getId().split("-")[0]);
            int col = Integer.parseInt(noeud.getId().split("-")[1]);
            gridPane.add(btnCase, col, row);
            boutonsMap.put(noeud, btnCase);
            // niveau 3 : desactiver tout les noeuds sauf depart
            if (niveau == 3 && btnCase!= boutonsMap.get(positionDepart)) {
            	btnCase.setDisable(true);
            }
        }
         
    }
   protected static void demarrerCompteARebours(Label tempsLabel, Stage primaryStage, Runnable onCompteAReboursEnd) {
        Label compteAReboursLabel = new Label("3");
        compteAReboursLabel.setStyle("-fx-font-size: 50px; -fx-font-weight: bold;");
        
        StackPane pane = new StackPane(compteAReboursLabel);
        pane.setStyle("-fx-background-color: white;"); // Fond blanc pour plus de lisibilité

        Scene sceneTemp = new Scene(pane, 300, 200);
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setScene(sceneTemp);
        popup.setTitle("Préparez-vous !");
        popup.show();

        Timeline compteARebours = new Timeline(
            new KeyFrame(Duration.seconds(1), _ -> compteAReboursLabel.setText("2")),
            new KeyFrame(Duration.seconds(2), _ -> compteAReboursLabel.setText("1")),
            new KeyFrame(Duration.seconds(3), _ -> {
                popup.close(); // Fermer la fenêtre après le compte à rebours
                 onCompteAReboursEnd.run(); // Exécuter le callback après la fin du compte à rebours
            })
        );
        compteARebours.setCycleCount(1);
        compteARebours.play();
    } 
   
// Méthode utilitaire pour afficher une alerte
   protected static void afficherAlerte(String titre, String message, Alert.AlertType type) {
	   Alert alert = new Alert(type, message); 
	   alert.setTitle(titre);
	    alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #f4f4f4;"); 
	    alert.showAndWait();
   }
   
   
   protected void activerVoisins(Noeud noeud, Map<Noeud, Button> boutonsMap, Graphe graphe) {
       for (Button btn : boutonsMap.values()) {
           btn.setDisable(true);
       }
     for (Noeud voisin : graphe.getListeAdjacence().get(noeud)) {
           Button btnVoisin = boutonsMap.get(voisin);
           if (btnVoisin != null) {
               btnVoisin.setDisable(false); 
           }
       }
   } 
   private void afficherRegles(Stage primaryStage) {
       Stage reglesStage = new Stage();
       reglesStage.setTitle("Règles du Jeu");

       // Titre
       Label titreLabel = new Label("🏆 Labyrinthe de Mots – Règles du Jeu");
       titreLabel.setFont(new Font("Arial", 28));
       titreLabel.setTextFill(Color.WHITE);
       titreLabel.setStyle("-fx-font-weight: bold;");
       titreLabel.setPadding(new Insets(15));
       titreLabel.setAlignment(Pos.CENTER);
       titreLabel.setMaxWidth(Double.MAX_VALUE);
       titreLabel.setBackground(new Background(new BackgroundFill(Color.web("#4A90E2"), new CornerRadii(10), Insets.EMPTY)));

       // Contenu des règles
       Label reglesLabel = new Label(
    	        "🎯  OBJECTIF\n"
    	        + "  	  📌 Du nœud de départ jusqu'à l’arrivée, formez des mots pour maximiser votre score.\n"
    	        + "   	  📌 Sortez du labyrinthe avant la fin du temps avec au moins un mot valide.\n\n"

    	        + "🕹️  COMMENT JOUER ?\n\n"
    	        + "  	 	 ✅ Déplacez-vous en 8 directions pour sélectionner des lettres.\n"
    	        + "   		 ✅ En mode Facile et Moyen solo :\n"
    	        + "       			 - Formez un mot en cliquant sur des cases adjacentes.\n"
    	        + "      			 - Validez-le pour marquer des points.\n"
    	        + "      			 - Continuez à utiliser les cases pour d'autres mots.\n\n"

    	        + "⚔️  MODES DE JEU\n\n"
    	        + "    👤 SOLO : Jouez seul et obtenez le meilleur score possible.\n\n"

    	        + "    		🔹 FACILE & MOYEN :\n"
    	        + "       			- Nombre limité de tentatives pour valider des mots.\n"
    	        + "       			- Possibilité d'utiliser des indices (-2 points par indice).\n"
    	        + "       			- Les cases restent disponibles après validation d’un mot.\n\n"

    	        + "    		🔸 DIFFICILE :\n"
    	        + "       			- Départ et arrivée aléatoires.\n"
    	        + "       			- Le jeu commence uniquement à partir du nœud de départ.\n"
    	        + "       			- Le parcours se dévoile progressivement.\n\n"

    	        + "   		BONUS :\n"
    	        + "    				✔️ +3 points si vous trouvez le plus court chemin.\n"
    	        + "    				✔️ +10 points si vous formez plusieurs mots dans un seul parcours.\n\n"
    	        
    	        + "👥  MULTIJOUEUR\n"
    	        + "    🖱️ Joueur 1 → Souris\n"
    	        + "    ⌨️ Joueur 2 → Clavier (pavé numérique)\n"
    	        + "    📌 Formez des mots en avançant vers l'arrivée pour maximiser votre score.\n"
    	        + "    📌 Sortez du labyrinthe avant l’adversaire pour marquer +5 points et mettre fin à la partie.\n\n"

    	        + "🛑  \nFIN DU JEU\n\n"
    	        + "    ✔️ Le jeu se termine lorsque vous atteignez l’arrivée avec au moins un mot valide.\n"
    	        + "    ❌ Défaite si :\n"
    	        + "       - Le temps est écoulé sans être sorti du labyrinthe.\n"
    	        + "       - Aucun mot valide n’a été formé."
    	    );

       reglesLabel.setFont(new Font("Segoe UI", 14));
       reglesLabel.setTextFill(Color.web("#333333"));
       reglesLabel.setPadding(new Insets(20));
       reglesLabel.setWrapText(true);
       reglesLabel.setMaxWidth(650);
       reglesLabel.setBackground(new Background(new BackgroundFill(Color.web("#F5F5F5"), new CornerRadii(10), Insets.EMPTY)));

       // ScrollPane pour le texte long
       ScrollPane scrollPane = new ScrollPane();
       scrollPane.setContent(reglesLabel);
       scrollPane.setFitToWidth(true);
       scrollPane.setPrefHeight(400);
       scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

       // Bouton Retour
       Button retourButton = new Button("Retour");
       retourButton.setFont(new Font("Arial", 16));
       retourButton.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold;");
       retourButton.setPadding(new Insets(10, 20, 10, 20));
       retourButton.setOnAction(_ -> reglesStage.close());

       // Conteneur principal
       VBox root = new VBox(20, titreLabel, scrollPane, retourButton);
       root.setAlignment(Pos.CENTER);
       root.setPadding(new Insets(25));
       root.setStyle("-fx-background-color: white; -fx-border-color: #DDDDDD; -fx-border-width: 1px; -fx-border-radius: 15px;");

       // Créer la scène
       Scene scene = new Scene(root, 700, 600);
       reglesStage.setScene(scene);
       reglesStage.initModality(Modality.APPLICATION_MODAL);
       reglesStage.show();
   }
    public static void main(String[] args) {
        launch(args);
    }
}