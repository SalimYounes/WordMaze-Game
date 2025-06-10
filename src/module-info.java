/**
 * 
 */
/**
 * 
 */
module essai2 {
	requires java.desktop;
	requires org.jgrapht.core;
	requires javafx.controls;
    requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.base;
    
    opens labyrinthe.mots to javafx.graphics;
}