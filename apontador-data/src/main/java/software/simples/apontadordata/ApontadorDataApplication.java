package software.simples.apontadordata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApontadorDataApplication {

	private static final String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";
	private static final String ACESS_TOKEN = "ff4f27c9-ae1e-4394-97ab-e665d68bd273";
	
	private static String URL = "https://api.apontador.com.br:443/v2/places";

	public static void main(String[] args) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.add("user-agent", AGENT);
		headers.setBearerAuth(ACESS_TOKEN);
		
		HttpEntity<String> request = new HttpEntity<>(headers);
		
		URL = UriComponentsBuilder.fromHttpUrl(URL)
				//.queryParam("q", "Vila Olímpia")
				//.queryParam("fq", "categories.category.id:51")//categoria Endereços Empresariais
				.queryParam("fq", "categories.category.id:137") // 
				.queryParam("fq", "address.state:SP")
				.queryParam("fl", "*,description")
				.queryParam("rows", "50")
				.build()
				.toUriString();
		
		RestTemplate rest = new RestTemplate();
		
		int totalImportado = 0;
		
		for (int i = 0; i < 100; i++) {
			ResponseEntity<String> response = rest.exchange(URL, HttpMethod.GET, request, String.class);
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.getBody());
			
			JsonNode results = root.path("results");
			System.out.println(results);
			URL = results.path("header")
					.get("next")
					.asText("");
			
			JsonNode places = results.withArray("places");
			
			Iterator<JsonNode> it = places.elements();
			while (it.hasNext()) {
				JsonNode place = it.next();
				
				String nome = place.get("name").asText();
				
				String descricao = Optional.ofNullable(place.findValue("description"))
								.map(JsonNode::asText)
								.orElse("");
				
				String tag = Optional.ofNullable(place.withArray("tags").get(0))
								.map((j) -> j.get("value").asText())
								.orElse("");
				
				String categoria = Optional.ofNullable(place.withArray("categories").get(0))
								.map((j) -> j.get("name").asText())
								.orElse("");
								
				escreveNoArquivo("moda.txt", nome + " " + descricao + " " + tag);
				escreveNoArquivo("moda-labels.txt", categoria);
				
				totalImportado++;
			}
			
			System.out.println("Importados: " + totalImportado);
			System.out.println("Next URL: " + URL);
			
			if (URL.isEmpty()) {
				break;
			}
			
			
		}
		
	}
	
	private static void escreveNoArquivo(String arquivo, String linha) {
		Path path = Paths.get("C:\\desenvolvimento\\projetos\\apontador-data\\src\\main\\resources\\" + arquivo);
		
		try {
			if (!Files.exists(path)) {
				System.out.println("criando arquivo " + arquivo);
				Files.createFile(path);
			}
			
			//remove acentos
			linha = Normalizer.normalize(linha, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
			
			//remove caracteres especiais
			linha = linha.replaceAll("[^a-zA-Z0-9 ]", " ")
					     .replaceAll("  ", " ");
			linha += "\r";
			linha = linha.toLowerCase();
			Files.write(path, linha.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
