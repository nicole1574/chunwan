package com.chunwan.kg.service;

import com.chunwan.kg.entity.Person;
import com.chunwan.kg.repository.PersonRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BaikeCrawlerService {

    private final PersonRepository personRepository;

    public BaikeCrawlerService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person crawlAndSave(String name) {
        try {
            String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8);
            String url = "https://baike.baidu.com/item/" + encoded;
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            String fullText = doc.text();
            String summary = doc.select("meta[name=description]").attr("content");

            Person person = personRepository.findByName(name).orElseGet(Person::new);
            person.setId(person.getId() == null ? name : person.getId());
            person.setName(name);
            person.setGender(extractNearKeyword(fullText, "性别", "(男|女)"));
            person.setEthnicity(extractNearKeyword(fullText, "民族", "([\\p{IsHan}]{1,4}族)"));
            person.setRegion(firstNonBlank(
                    extractNearKeyword(fullText, "国籍", "([\\p{IsHan}A-Za-z·]{2,20})"),
                    extractNearKeyword(fullText, "地区", "([\\p{IsHan}A-Za-z·]{2,20})")
            ));
            person.setBio(summary == null ? "" : summary);
            person.setBaikeUrl(url);
            if (person.getType() == null) {
                person.setType("个人");
            }
            if (person.getOverseas() == null) {
                person.setOverseas(false);
            }
            return personRepository.save(person);
        } catch (Exception ex) {
            Person fallback = personRepository.findByName(name).orElseGet(Person::new);
            fallback.setId(fallback.getId() == null ? name : fallback.getId());
            fallback.setName(name);
            if (fallback.getType() == null) {
                fallback.setType("个人");
            }
            if (fallback.getOverseas() == null) {
                fallback.setOverseas(false);
            }
            return personRepository.save(fallback);
        }
    }

    private String firstNonBlank(String v1, String v2) {
        if (v1 != null && !v1.isBlank()) {
            return v1;
        }
        return v2;
    }

    private String extractNearKeyword(String text, String keyword, String pattern) {
        int index = text.indexOf(keyword);
        if (index < 0) {
            return null;
        }
        int end = Math.min(index + 80, text.length());
        String scope = text.substring(index, end);
        Matcher matcher = Pattern.compile(pattern).matcher(scope);
        return matcher.find() ? matcher.group(1) : null;
    }
}
