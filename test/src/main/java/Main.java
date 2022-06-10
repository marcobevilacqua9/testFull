
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;



public class Main {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/pokemon", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            // CORS
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
            t.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");

            // get pokemon name from url
            String pokemon;

            try{
                pokemon =  t.getRequestURI().toString().split("/")[2];

                try{
                    // check if it is a number
                    Integer.parseInt(pokemon);
                    throw new Exception();
                } catch (NumberFormatException e){
                    // not a number
                }

            } catch (Exception e){
                // it's a number
                String badRequest = "Not Found";
                t.sendResponseHeaders(200, badRequest.getBytes().length);
                OutputStream os = t.getResponseBody();
                os.write(badRequest.getBytes(), 0, badRequest.getBytes().length);
                os.flush();
                os.close();
                return;
            }

            URL url = new URL("https://pokeapi.co/api/v2/pokemon-species/" + pokemon.toLowerCase(Locale.ROOT));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // pokemon api responds Forbidden to requests without browser info
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:221.0) Gecko/20100101 Firefox/31.0");
            connection.connect();

            if(!Objects.equals(connection.getResponseMessage(), "OK")){
                // pokemon api did not find the pokemon or had other issues
                t.sendResponseHeaders(200, connection.getResponseMessage().getBytes().length);
                OutputStream os = t.getResponseBody();
                os.write(connection.getResponseMessage().getBytes(), 0, connection.getResponseMessage().getBytes().length);
                os.flush();
                os.close();
                return;
            }

            InputStream inputStreamObject = connection.getInputStream();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStreamObject, StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            // parse into generic json object from gson library
            JsonObject jsonObject = new JsonParser().parse(responseStrBuilder.toString()).getAsJsonObject();

            // find a description index for a description which has english language
            int i = 0;
            try {
                while (!Objects.equals(jsonObject.getAsJsonArray("flavor_text_entries").get(i).getAsJsonObject().get("language").getAsJsonObject().get("name").getAsString(), "en")) {
                    i += 1;
                }
            } catch (Exception e){
                // no english language descriptions
                final String languageNotFound = "Eng language description not found";
                t.sendResponseHeaders(200, languageNotFound.getBytes().length);
                OutputStream os = t.getResponseBody();
                os.write(languageNotFound.getBytes(), 0, languageNotFound.getBytes().length);
                os.flush();
                os.close();
                return;
            }

            // get pokemon description and if it is legendary
            final String description = jsonObject.getAsJsonArray("flavor_text_entries").get(i).getAsJsonObject().get("flavor_text").getAsString();
            final String isLegendary = jsonObject.get("is_legendary").getAsString();

            // build gui response json
            JsonObject jsonObject1 = new JsonObject();
            jsonObject1.addProperty("name", pokemon.substring(0, 1).toUpperCase() + pokemon.substring(1));
            jsonObject1.addProperty("description", description.replaceAll("\f", " "));
            jsonObject1.addProperty("is_legendary", isLegendary);

            // send gui response
            t.sendResponseHeaders(200, jsonObject1.toString().getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(jsonObject1.toString().getBytes(), 0, jsonObject1.toString().getBytes().length);
            os.flush();
            os.close();

        }
    }

}