package br.com.studyassistant.studyassistantai.service;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentChunkingServiceTest {

    private final ContentChunkingService service = new ContentChunkingService();

    private final Encoding encoding = Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    @Test
    void shouldKeepShortTextInSingleChunk(){
        List<String> chunks = service.split("Conteúdo curto para estudo.");

        assertEquals(1, chunks.size());
        assertEquals("Conteúdo curto para estudo.", chunks.getFirst());
    }

    @Test
    void shouldSplitLongTextIntoChunksOfAtMost500Tokens(){
        String text = generateLongText();
        List<String> chunks = service.split(text);
        assertTrue(chunks.size() > 1);
    }

    @Test
    void shouldOverlapConsecutiveChunksBy50Tokens() {
        String text = generateLongText();
        List<String> chunks = service.split(text);
        assertTrue(chunks.size() > 1);

        for(int i =0; i < chunks.size() - 1; i++){
            List<Integer> current = encoding.encode(chunks.get(i)).boxed();
            List<Integer> next = encoding.encode(chunks.get(i +1)).boxed();
            assertEquals(current.subList(current.size() - 50, current.size()), next.subList(0, 50));
        }
    }

    @Test
    void shouldRejectNullOrBlankText(){
        assertThrows(IllegalArgumentException.class, () -> service.split(null));
        assertThrows(IllegalArgumentException.class, () -> service.split(""));
        assertThrows(IllegalArgumentException.class, () -> service.split("   "));
    }

    private String generateLongText(){
        return IntStream.range(0, 1000)
                .mapToObj(i -> "Conceito" + i)
                .reduce((a,b) -> a + " " + b)
                .orElseThrow();
    }
}