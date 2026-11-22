package br.com.studyassistant.studyassistantai.service;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.CONTENT_BLANK;

@Service
public class ContentChunkingService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    private final Encoding encoding = Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    public List<String> split(String text) {
        if(text == null || text.isBlank()) {
            throw new IllegalArgumentException(CONTENT_BLANK);
        }

        List<Integer> tokens = encoding.encode(text.trim()).boxed();
        List<String> chunks = new ArrayList<>();

        int step = CHUNK_SIZE - CHUNK_OVERLAP;

        // Avança 450 tokens por iteração, enquanto cada chunk contém até 500.
        // Assim os 50 tokens finais de um chunk são reutilizados no próximo.
        // O Math.min impede que o último chunk ultrapasse o tamanho do texto.

        for(int start = 0; start < tokens.size(); start += step) {
            int end = Math.min(start + CHUNK_SIZE, tokens.size());

            IntArrayList chunkTokens = new IntArrayList(end - start);

            for(int i = start; i < end; i++) {
                chunkTokens.add(tokens.get(i));
            }

            String chunk = encoding.decode(chunkTokens).trim();

            if(!chunk.isBlank()){
                chunks.add(chunk);
            }

            if(end == tokens.size()) {
                break;
            }
        }

        return chunks;
    }
}