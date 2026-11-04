/**
 * 
 */
package battleship;

public class Main
{
	/**
	 * Main.
	 *
	 * @param args the args
	 */
	public static void main(String[] args)
    {
		System.out.println("***  Battleship  ***");
		if (java.util.Arrays.asList(args).contains("--gui")) {
			if (java.util.Arrays.asList(args).contains("--en")) GestorIdioma.setIdioma("en");
			GUI.play();
		} else {
			Tasks.menu();
		}
    }
}
