[![Actions Status](https://github.com/VasilyevPS/java-project-72/workflows/hexlet-check/badge.svg)](https://github.com/VasilyevPS/java-project-72/actions)
[![JavaCI](https://github.com/VasilyevPS/java-project-72/actions/workflows/main.yml/badge.svg)](https://github.com/VasilyevPS/java-project-72/actions)
[![Maintainability](https://api.codeclimate.com/v1/badges/641bb5f3769001e7c18f/maintainability)](https://codeclimate.com/github/VasilyevPS/java-project-72/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/641bb5f3769001e7c18f/test_coverage)](https://codeclimate.com/github/VasilyevPS/java-project-72/test_coverage)

# Page analyzer
## Description
[Page analyzer](https://vasilyevps-page-analyzer.onrender.com) – a website that analyzes the specified pages for SEO suitability.

The main page (tab *Главная*) allows to add website site, that needs to be checked, to the database. The *Сайты* tab contains a list of all sites and brief information about the last check.
Click on the site shows information about it: a list of all previous checks with statuses, as well as necessary for SEO information. It also allows to start new check.

## How to run locally
The site can be opened locally at `http://localhost:8080/` 
* Use `make build` to set up the project 
* Use `make start` to run it
* Use `make test` to test it

## Stack
Javalin, Ebean, H2, PostgreSQL, JUnit, MockWebServer, Thymeleaf, Bootstrap.
