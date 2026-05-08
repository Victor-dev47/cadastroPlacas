import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import com.sun.net.httpserver.*;

public class Server {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // =========================
        // SERVIR HTML COM DADOS DO BANCO
        // =========================
        server.createContext("/", exchange -> {

            StringBuilder html = new StringBuilder();

            // Carregar HTML base
            File file = new File("index.html");
            String pagina = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            // Montar tabela
            StringBuilder tabela = new StringBuilder();

            try {
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/placas_db",
                        "root",
                        "1234" // SUA SENHA
                );

                String sql = "SELECT * FROM veiculos";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    tabela.append("<tr>");
                    tabela.append("<td>").append(rs.getString("placa")).append("</td>");
                    tabela.append("<td>").append(rs.getString("modelo")).append("</td>");
                    tabela.append("<td>").append(rs.getString("cor")).append("</td>");
                    tabela.append("</tr>");
                }

                conn.close();

            } catch (Exception e) {
                tabela.append("<tr><td colspan='3'>Erro ao carregar dados</td></tr>");
            }

            // Substituir no HTML
            pagina = pagina.replace("{{TABELA}}", tabela.toString());

            byte[] response = pagina.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);

            exchange.getResponseBody().write(response);
            exchange.close();
        });

        // =========================
        // SERVIR CSS
        // =========================
        server.createContext("/style.css", exchange -> {

            File file = new File("style.css");

            byte[] response = java.nio.file.Files.readAllBytes(file.toPath());

            exchange.getResponseHeaders().add("Content-Type", "text/css");
            exchange.sendResponseHeaders(200, response.length);

            exchange.getResponseBody().write(response);
            exchange.close();
        });

        // =========================
        // SALVAR NO BANCO
        // =========================
        server.createContext("/salvar", exchange -> {

            String query = exchange.getRequestURI().getQuery();

            String placa = "", modelo = "", cor = "";

            if (query != null) {
                String[] partes = query.split("&");

                for (String parte : partes) {
                    String[] chaveValor = parte.split("=");

                    if (chaveValor.length == 2) {
                        if (chaveValor[0].equals("placa")) placa = chaveValor[1];
                        if (chaveValor[0].equals("modelo")) modelo = chaveValor[1];
                        if (chaveValor[0].equals("cor")) cor = chaveValor[1];
                    }
                }
            }

            try {
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/placas_db",
                        "root",
                        "1234"
                );

                String sql = "INSERT INTO veiculos (placa, modelo, cor) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setString(1, placa);
                stmt.setString(2, modelo);
                stmt.setString(3, cor);

                stmt.executeUpdate();

                conn.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Redireciona
            exchange.getResponseHeaders().add("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.start();
        System.out.println("Servidor rodando em http://localhost:8080");
    }
}