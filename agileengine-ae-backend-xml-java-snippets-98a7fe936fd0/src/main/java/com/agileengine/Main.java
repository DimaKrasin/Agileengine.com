package com.agileengine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static Logger LOGGER = LoggerFactory.getLogger(JsoupFindByIdSnippet.class);
    private static String CHARSET_NAME = "utf8";

    public static void main(String[] args) {

        //Стартовые значения
        String pathToOriginalFile =args[0];// "sample-0-origin.html";
        String idOriginal = "make-everything-ok-button";
        String pathToOtherFile = args[1];//"sample-1-evil-gemini.html";
        String tagToFound = "a";
        double conformityRate = 0.6; //Минимальный процент на который должен совпадать оригинал и кандидат

        //Получаем map атрибутов оригинала
        Map mapWithOriginalAttributes =
                findByID(pathToOriginalFile, idOriginal);


        //Получаем колекцию с map-ами
        //В каждой map атрибуты одного кандидата
        List<Map> allA = Main.findByTagName(Main.class.getResourceAsStream(pathToOtherFile), tagToFound);

        //Сравниваем каждого кандидата с оригиналом
        //И при совпадении добавляем кандидату +1 в поле conformity
        for (Map candidateAttributes : Objects.requireNonNull(allA)) {

            mapWithOriginalAttributes.forEach(
                    (originalKey, originalValue) -> {
                        if (candidateAttributes.containsKey(originalKey)) {
                            if (originalValue.toString().equals((String) candidateAttributes.get(originalKey))) {
                                String currentlyCandidateConformity = (String) candidateAttributes.get("conformity");
                                candidateAttributes.put("conformity", String.valueOf(Integer.parseInt(currentlyCandidateConformity) + 1));
                            }
                        }
                    });
        }

        //Проходи по всем елементам и находи елемент с максимальним conformity
        int maxConformity = 0;
        int maxConformityMapHashCode = allA.get(0).hashCode();
        for (Map<String, String> map : allA) {
            int tmp = Integer.valueOf(map.get("conformity"));
            if (tmp > maxConformity) {
                maxConformity = tmp;
                maxConformityMapHashCode = map.hashCode();
            }
        }


        //Проверяем его на уровень conformity и выводи если  подходят
        if (mapWithOriginalAttributes.size() / maxConformity >= conformityRate) {

            for (Map map : allA) {
                if (map.hashCode() == maxConformityMapHashCode) {

                    StringBuilder css = new StringBuilder();
                    StringBuilder linktext = new StringBuilder();

                    map.forEach((k, v) -> {
                        if (!k.equals("conformity") & !k.equals("linktext")) {
                            css.append("[").append(k).append("=\"").append(v).append("\"]");
                        }

                        if (k.equals("linktext")) {
                            linktext.append(v);
                        }
                    });

                    Optional<Elements> elementsOpt =
                            JsoupCssSelectSnippet.findElementsByQuery(Main.class.getResourceAsStream(pathToOtherFile),
                                    tagToFound + ":contains(" + linktext + ")" + css);

                    System.out.println(elementsOpt.get().first().cssSelector());
                }
            }
        }

    }


    //Находим елемент по айди
    private static Map findByID(String resourcePath, String targetElementId) {

        Optional<Element> buttonOpt = JsoupFindByIdSnippet.findElementById(Main.class.getResourceAsStream(resourcePath), targetElementId);

        Map<String, String> map = new HashMap<String, String>();

        Optional<String> stringifiedAttributesOpt = buttonOpt.map(button ->
                button.attributes().asList().stream()
                        .map(attr -> map.put(attr.getKey(), attr.getValue()))
                        .collect(Collectors.joining(", "))
        );

        //Добавляем к елементу поле с его ссылкой
        if (buttonOpt.get().hasText()) {
            map.put("linktext", (buttonOpt.get().ownText()));
        }

        return map;
    }


    private static List<Map> findByTagName(InputStream htmlFile, String tag) {

        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    "http://example.com/");

            //Находим все елементы с заданым тегом
            Optional<Elements> allElementsByTag = Optional.of(doc.getElementsByTag(tag));

            //Коллекция для хранения Map с атрибутами каждого елемента
            //Одна Map для одного елемента
            //Ключ в Map - название атририбута, значение - значение атрибута
            List<Map> listWithMaps = new ArrayList<>();

            //Заполнаем listWithMaps
            for (Element e : allElementsByTag.get()) {
                Map<String, String> mapWithOneElementAttributes = new HashMap<>();

                e.attributes().asList().forEach((oneAttribute) ->
                        mapWithOneElementAttributes.put(oneAttribute.getKey(), oneAttribute.getValue()));

                if (e.hasText()) {
                    mapWithOneElementAttributes.put("linktext", (e.ownText()));
                }

                mapWithOneElementAttributes.put("conformity", "0");

                listWithMaps.add(mapWithOneElementAttributes);
            }

            return listWithMaps;

        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", "http://example.com/", e);
            return null;
        }
    }
}
