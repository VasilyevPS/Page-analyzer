package hexlet.code.controllers;

import hexlet.code.domain.Url;
import io.javalin.http.Handler;
import java.net.URL;
import java.util.List;

public class UrlsController {
    public static Handler listUrl = ctx -> {
        
    };

    public static Handler createUrl = ctx -> {
        String inputUrl = ctx.formParam("url");

        URL parsedUrl;
        try {
            parsedUrl = new URL(inputUrl);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        String port = parsedUrl.getPort() != -1 ? ":" + parsedUrl.getPort() : "";
        String normalizedUrl = (parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + port)
                .toLowerCase();

        Url newUrl = new Url(normalizedUrl);
        newUrl.save();
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");

        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {

    };
}
