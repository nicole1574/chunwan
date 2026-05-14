package com.chunwan.kg.dto;

public record PdfImportResult(
        int yearsCreated,
        int programsCreated,
        int personsCreated,
        int relationsCreated,
        int parsedProgramLines
) {
}
