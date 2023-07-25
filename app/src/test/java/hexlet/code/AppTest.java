package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static MockWebServer mockServer;
    private static final String TEST_PAGE_PATH = "./src/test/resources/fixtures/index.html";

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start();
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();

        Path filePath = Paths.get(TEST_PAGE_PATH).toAbsolutePath().normalize();
        String body = Files.readString(filePath).trim();
        mockServer = new MockWebServer();
        mockServer.enqueue(new MockResponse().setBody(body));
        mockServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockServer.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Test
    void testRoot() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Nested
    class UrlTest {

        @Test
        void testListUrl() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("https://github.com");
        }

        @Test
        void testCreateUrl() {
            String inputUrl = "https://www.google.ru";
            HttpResponse responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asString();

            assertThat(responsePost.getStatus()).isEqualTo(302);

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Страница успешно добавлена");

            Url url = new QUrl()
                    .name.equalTo(inputUrl)
                    .findOne();

            assertThat(url).isNotNull();
            assertThat(url.getName()).isEqualTo(inputUrl);
        }

        @Test
        void testCreateUrlExisted() {
            String inputUrl = "https://github.com";
            HttpResponse responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asString();

            assertThat(responsePost.getStatus()).isEqualTo(302);

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Страница уже существует");
        }

        @Test
        void testCreateUrlFail() {
            String inputUrl = "google.ru";
            HttpResponse responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asString();

            assertThat(responsePost.getStatus()).isEqualTo(302);

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Некорректный URL");
        }

        @Test
        void testShowUrl() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/1")
                    .asString();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("https://github.com");
        }

        @Test
        void testShowUrlFail() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/1000")
                    .asString();

            assertThat(response.getStatus()).isEqualTo(404);
        }
    }

    @Test
    void testUrlCheck() {
        String mockServerUrl = mockServer.url("/").toString().replaceAll("/$", "");

        Unirest.post(baseUrl + "/urls")
                .field("url", mockServerUrl)
                .asString();

        Url url = new QUrl()
                .name.equalTo(mockServerUrl)
                .findOne();

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls/" + url.getId() + "/checks")
                .field("url", mockServerUrl)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/" + url.getId())
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);

        UrlCheck urlcheck = new QUrlCheck()
                .url.equalTo(url)
                .orderBy()
                .createdAt.desc()
                .findOne();

        assertThat(urlcheck).isNotNull();
        assertThat(urlcheck.getStatusCode()).isEqualTo(200);
        assertThat(urlcheck.getTitle()).isEqualTo("Test page");
        assertThat(urlcheck.getH1()).isEqualTo("Test header");
        assertThat(urlcheck.getDescription()).isEqualTo("This page was created for tests.");
    }

    @Test
    void testUrlCheckFail() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls/2/checks")
                .field("url", "https://github.commm")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/2")
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Exception");
    }
}
