package se.kry.codetest;

import io.vertx.core.Future;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BackgroundPoller {

  public Future<List<String>> pollServices(Map<Service, String> services) {
    services.entrySet()
        .forEach(service -> {
            try {
                URL url = new URL(service.getKey().getUrl());
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                int responseCode = huc.getResponseCode();
                // Service URL found
                if (responseCode == 200) {
                    service.setValue("OK");
                } else {
                    service.setValue("FAIL");
                }
            } catch (MalformedURLException e) {
                // Service URL not found
                service.setValue("FAIL");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    return Future.succeededFuture(services.entrySet().stream()
            .map(e -> e.getKey() + " : " + e.getValue())
            .collect(Collectors.toList()));
  }
}
