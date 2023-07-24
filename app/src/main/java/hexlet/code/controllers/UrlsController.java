package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.URL;
import java.util.List;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class UrlsController {
    public static Handler listUrl = ctx -> {
        List<Url> urls = new QUrl().findList();

        ctx.attribute("urls", urls);
        ctx.render("urls/index.html");
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

        Url url = new QUrl()
                .name.equalTo(normalizedUrl)
                .findOne();

        if (url == null) {
            Url newUrl = new Url(normalizedUrl);
            newUrl.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
        } else {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
        }

        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .urlChecks.fetch()
                .orderBy()
                .urlChecks.createdAt.desc()
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", url);
        ctx.render("urls/url.html");
    };

    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            String body = response.getBody();
            Document document = Jsoup.parse(body);

            int statusCode = response.getStatus();
            String title = document.title();
            Element elementH1 = document.selectFirst("h1");
            String h1 = elementH1 != null ? elementH1.text() : "";
            Element elementDescription = document.selectFirst("meta[name=description]");
            String description = elementDescription != null ? elementDescription.attr("content") : "";

            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url);
            urlCheck.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
            if (e.getMessage().contains("UnknownHostException")) {
                ctx.sessionAttribute("flash", "Некорректный адрес");
            }
        }

        ctx.redirect("/urls/" + url.getId());
    };


}
