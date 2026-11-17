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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document has no extractable text");
            }

            if (text.length() > MAX_EXTRACTED_CHARACTERS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document exceeds the extracted text limit");
            }

            String normalizedText = normalizeText(text);

            if (normalizedText.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document has no extractable text");
            }

            return normalizedText;

        } catch (IOException | TikaException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not extract document text", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds the 10 MB limit");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename is required");
        }

        String extension = filename.toLowerCase(Locale.ROOT);

        if (!extension.endsWith(".pdf") && !extension.endsWith(".txt")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF and TXT files are supported");
        }

        try (InputStream inputStream = file.getInputStream()) {

            String detectedType = tika.detect(inputStream, filename);

            boolean validPdf = extension.endsWith(".pdf") && detectedType.equals("application/pdf");
            boolean validTxt = extension.endsWith(".txt") && detectedType.equals("text/plain");

            if (!validPdf && !validTxt) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File format does not match its extension");
            }

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read document", e);
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