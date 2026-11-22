package br.com.studyassistant.studyassistantai.service;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.exception.TikaException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.*;

@Service
public class DocumentExtractionService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private static final int MAX_EXTRACTED_CHARACTERS = 500_000;

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) {
        validateFile(file);

        try (InputStream inputStream = file.getInputStream()) {

            String text = tika.parseToString(inputStream, new Metadata(), MAX_EXTRACTED_CHARACTERS + 1);

            if (text == null || text.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NO_EXTRACTABLE_TEXT);
            }

            if (text.length() > MAX_EXTRACTED_CHARACTERS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEXT_LIMIT_EXCEEDED);
            }

            String normalizedText = normalizeText(text);

            if (normalizedText.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NO_EXTRACTABLE_TEXT);
            }

            return normalizedText;

        } catch (IOException | TikaException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEXT_EXTRACTION_FAILED, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FILE_REQUIRED);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FILE_LIMIT_EXCEEDED);
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FILE_REQUIRED);
        }

        String extension = filename.toLowerCase(Locale.ROOT);

        if (!extension.endsWith(".pdf") && !extension.endsWith(".txt")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, UNSUPPORTED_EXTENSION);
        }

        try (InputStream inputStream = file.getInputStream()) {

            String detectedType = tika.detect(inputStream, filename);

            boolean validPdf = extension.endsWith(".pdf") && detectedType.equals("application/pdf");
            boolean validTxt = extension.endsWith(".txt") && detectedType.equals("text/plain");

            if (!validPdf && !validTxt) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_FILE_FORMAT);
            }

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DOCUMENT_READ_FAILED, e);
        }
    }

    private String normalizeText(String text) {
        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[\\p{Cc}&&[^\\n\\t]]", "")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}