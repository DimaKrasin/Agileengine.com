package com.agileengine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsoupCssSelectSnippet {

    private static Logger LOGGER = LoggerFactory.getLogger(JsoupCssSelectSnippet.class);

    private static String CHARSET_NAME = "utf8";

    public static void main(String[] args) {

        // Jsoup requires an absolute file path to resolve possible relative paths in HTML,
        // so providing InputStream through classpath resources is not a case
        String resourcePath = "./samples/startbootstrap-freelancer-gh-pages-cut.html";

        String cssQuery = "div[id=\"success\"] button[class*=\"btn-primary\"]";

        Optional<Elements> elementsOpt = findElementsByQuery(Main.class.getResourceAsStream(resourcePath), cssQuery);

        Optional<List<String>> elementsAttrsOpts = elementsOpt.map(buttons ->
                {
                    List<String> stringifiedAttrs = new ArrayList<>();

                    buttons.iterator().forEachRemaining(button ->
                            stringifiedAttrs.add(
                                    button.attributes().asList().stream()
                                            .map(attr -> attr.getKey() + " = " + attr.getValue())
                                            .collect(Collectors.joining(", "))));

                    return stringifiedAttrs;
                }
        );


        elementsAttrsOpts.ifPresent(attrsList ->
                attrsList.forEach(attrs ->
                        LOGGER.info("Target element attrs: [{}]", attrs)
                )
        );
    }

    public static Optional<Elements> findElementsByQuery(InputStream htmlFile, String cssQuery) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    "http://example.com/");

            return Optional.of(doc.select(cssQuery));

        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", "http://example.com/", e);
            return Optional.empty();
        }
    }

}