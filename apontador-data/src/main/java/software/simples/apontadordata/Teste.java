package software.simples.apontadordata;

import java.text.Normalizer;

public class Teste {
	
	public static void main(String[] args) {
		String text = "This - word ! has \\ /allot # of % special % characters ação á ü í ê";
		
		text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		
		text = text.replaceAll("[^a-zA-Z0-9 ]", "")
				   .replaceAll("  ", " ");
		
		System.out.println(text);
	}

}
