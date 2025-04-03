package model;
import javax.swing.JOptionPane;

public class Test {

	    public static void main(String[] args) {
	        // Simulate a print action
	        simulatePrintAction();
	    }

	    public static void simulatePrintAction() {
	        // Display a pop-up message
	        JOptionPane.showMessageDialog(null, "Printing document...", "Print Status", JOptionPane.INFORMATION_MESSAGE);
	        // Simulate printing process
	        System.out.println("Printing in progress...");
	        // Display a pop-up message when printing is complete
	        JOptionPane.showMessageDialog(null, "Document printed successfully!", "Print Status", JOptionPane.INFORMATION_MESSAGE);
	    }

}
