package com.chunwan.kg.service;

import com.chunwan.kg.dto.PdfImportResult;
import com.chunwan.kg.dto.RelationRequest;
import com.chunwan.kg.entity.Person;
import com.chunwan.kg.entity.Program;
import com.chunwan.kg.entity.YearNode;
import com.chunwan.kg.repository.PersonRepository;
import com.chunwan.kg.repository.ProgramRepository;
import com.chunwan.kg.repository.YearRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfProgramImportService {

    private static final Pattern YEAR_HEADER_PATTERN = Pattern.compile("(?m)^(\\d{4})\\s*央视春晚节目单\\s*$");
    private static final Pattern PROGRAM_LINE_PATTERN = Pattern.compile("^\\s*([0-9]{1,3}[A-Za-z]?)\\s*[、,，.．]\\s*(.+)\\s*$");
    private static final Pattern TITLE_PATTERN = Pattern.compile("《([^》]{1,120})》");
    private static final Pattern PERFORMER_PATTERN = Pattern.compile("(?:演员|演唱者|表演者|演出者|演出单位|表演|演出|主持人|解说)[:：]\\s*(.+)$");

    private final PersonRepository personRepository;
    private final ProgramRepository programRepository;
    private final YearRepository yearRepository;
    private final GraphService graphService;

    public PdfProgramImportService(PersonRepository personRepository,
                                   ProgramRepository programRepository,
                                   YearRepository yearRepository,
                                   GraphService graphService) {
        this.personRepository = personRepository;
        this.programRepository = programRepository;
        this.yearRepository = yearRepository;
        this.graphService = graphService;
    }

    public PdfImportResult importFromPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF 文件不能为空");
        }
        String text = extractText(file);
        return parseAndImport(text);
    }

    private String extractText(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new IllegalArgumentException("PDF 读取失败", e);
        }
    }

    private PdfImportResult parseAndImport(String allText) {
        if (allText == null || allText.isBlank()) {
            return new PdfImportResult(0, 0, 0, 0, 0);
        }

        Matcher matcher = YEAR_HEADER_PATTERN.matcher(allText);
        List<YearSection> sections = new ArrayList<>();
        while (matcher.find()) {
            sections.add(new YearSection(Integer.parseInt(matcher.group(1)), matcher.start(), matcher.end()));
        }
        if (sections.isEmpty()) {
            return new PdfImportResult(0, 0, 0, 0, 0);
        }

        int yearsCreated = 0;
        int programsCreated = 0;
        int personsCreated = 0;
        int relationsCreated = 0;
        int parsedProgramLines = 0;

        for (int i = 0; i < sections.size(); i++) {
            YearSection current = sections.get(i);
            int nextStart = (i + 1 < sections.size()) ? sections.get(i + 1).start : allText.length();
            String sectionText = allText.substring(current.end, nextStart);

            boolean sectionCreatedYear = false;
            YearNode yearNode;
            var existingYear = yearRepository.findByYear(current.year);
            if (existingYear.isEmpty()) {
                YearNode node = new YearNode();
                node.setId(String.valueOf(current.year));
                node.setYear(current.year);
                yearNode = yearRepository.save(node);
                sectionCreatedYear = true;
            } else {
                yearNode = existingYear.get();
            }
            if (sectionCreatedYear) yearsCreated++;

            for (String rawLine : sectionText.split("\\R")) {
                String line = normalizeLine(rawLine);
                if (line.isBlank()) continue;
                Matcher lineMatcher = PROGRAM_LINE_PATTERN.matcher(line);
                if (!lineMatcher.matches()) continue;

                String body = lineMatcher.group(2).trim();
                if (body.isBlank()) continue;
                parsedProgramLines++;

                String title = extractTitle(body);
                if (title.isBlank()) continue;

                String type = extractType(body);
                String storedProgramName = current.year + "-" + title;
                Optional<Program> existingProgram = programRepository.findByName(storedProgramName);
                boolean programExisted = existingProgram.isPresent();
                Program program = existingProgram.orElseGet(() -> {
                    Program created = new Program();
                    created.setId(UUID.randomUUID().toString());
                    created.setName(storedProgramName);
                    return created;
                });
                program.setType(type);
                program.setContent(body);
                program = programRepository.save(program);
                if (!programExisted) programsCreated++;

                // 直接进行年份关系绑定（幂等）
                graphService.linkProgramYear(program.getId(), yearNode.getId());

                List<String> performers = extractPerformers(body);
                for (String performerName : performers) {
                    Optional<Person> existingPerson = personRepository.findByName(performerName);
                    boolean personExisted = existingPerson.isPresent();
                    Person person = existingPerson.orElseGet(() -> {
                        Person created = new Person();
                        created.setId(UUID.randomUUID().toString());
                        created.setName(performerName);
                        created.setType(isGroupName(performerName) ? "团体" : "个人");
                        return personRepository.save(created);
                    });
                    if (!personExisted) personsCreated++;

                    graphService.upsertRelation(new RelationRequest(
                            person.getId(),
                            program.getId(),
                            "参演",
                            null,
                            null,
                            yearNode.getId()
                    ));
                    relationsCreated++;
                }

            }
        }

        return new PdfImportResult(yearsCreated, programsCreated, personsCreated, relationsCreated, parsedProgramLines);
    }

    private String normalizeLine(String line) {
        return line == null ? "" : line
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String extractType(String body) {
        int zhColon = body.indexOf('：');
        int enColon = body.indexOf(':');
        int idx = zhColon >= 0 ? zhColon : enColon;
        if (idx > 0 && idx <= 12) {
            return body.substring(0, idx).trim();
        }
        int quoteIdx = body.indexOf('《');
        if (quoteIdx > 0 && quoteIdx <= 10) {
            return body.substring(0, quoteIdx).trim();
        }
        return "节目";
    }

    private String extractTitle(String body) {
        List<String> titles = new ArrayList<>();
        Matcher titleMatcher = TITLE_PATTERN.matcher(body);
        while (titleMatcher.find()) {
            String t = titleMatcher.group(1).trim();
            if (!t.isBlank()) titles.add(t);
        }
        if (!titles.isEmpty()) {
            String joined = String.join(" / ", titles);
            return joined.length() > 180 ? joined.substring(0, 180) : joined;
        }
        String simplified = body.replaceAll("(?:演员|演唱者|表演者|演出者|演出单位|表演|演出|主持人|解说)[:：].*$", "").trim();
        if (simplified.length() > 180) simplified = simplified.substring(0, 180);
        return simplified;
    }

    private List<String> extractPerformers(String body) {
        Matcher performerMatcher = PERFORMER_PATTERN.matcher(body);
        if (!performerMatcher.find()) return List.of();
        String performerPart = performerMatcher.group(1);
        String cleaned = performerPart
                .replaceAll("（[^）]*）", " ")
                .replaceAll("\\([^)]*\\)", " ")
                .replace("和", " ")
                .replace("与", " ")
                .replace("及", " ");
        String[] parts = cleaned.split("[、，,;/\\s]+");
        Set<String> names = new LinkedHashSet<>();
        for (String part : parts) {
            String name = part.trim();
            if (name.length() < 2 || name.length() > 20) continue;
            if (name.matches(".*\\d.*")) continue;
            if (isNoiseWord(name)) continue;
            names.add(name);
        }
        return new ArrayList<>(names);
    }

    private boolean isNoiseWord(String value) {
        Set<String> noise = Set.of(
                "已故", "中国", "香港", "台湾", "现场观众", "集体演唱", "串联词", "本人", "特此说明", "补充", "暂缺名称"
        );
        return noise.contains(value);
    }

    private boolean isGroupName(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("团")
                || n.contains("队")
                || n.contains("公司")
                || n.contains("学院")
                || n.contains("剧院")
                || n.contains("电视台")
                || n.contains("乐团")
                || n.contains("总政")
                || n.contains("武警");
    }

    private record YearSection(int year, int start, int end) {
    }
}
