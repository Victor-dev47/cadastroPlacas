import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.sun.net.httpserver.*;

public class Server {

    static List<Map<String, String>> placas = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Servir HTML
        server.createContext("/", exchange -> {
            File file = new File("index.html");

            byte[] response = java.nio.file.Files.readAllBytes(file.toPath());

            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);

            exchange.getResponseBody().write(response);
            exchange.close();
        });

        // Servir CSS
        server.createContext("/style.css", exchange -> {
            File file = new File("style.css");

            byte[] response = java.nio.file.Files.readAllBytes(file.toPath());

            exchange.getResponseHeaders().add("Content-Type", "text/css");
            exchange.sendResponseHeaders(200, response.length);

            exchange.getResponseBody().write(response);
            exchange.close();
        });

        // Salvar placa
        server.createContext("/salvar", exchange -> {

            String query = exchange.getRequestURI().getQuery();

            Map<String, String> dados = new HashMap<>();

            if (query != null) {
                String[] partes = query.split("&");

                for (String parte : partes) {
                    String[] chaveValor = parte.split("=");
                    if (chaveValor.length == 2) {
                        dados.put(chaveValor[0], chaveValor[1]);
                    }
                }
            }

            placas.add(dados);

            // Redireciona de volta para a página
            exchange.getResponseHeaders().add("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.start();
        System.out.println("Servidor rodando em http://localhost:8080");
    }
}
