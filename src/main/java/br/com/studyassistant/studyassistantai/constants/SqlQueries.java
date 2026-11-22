package br.com.studyassistant.studyassistantai.constants;

public final class SqlQueries {

    public static final String FIND_CONTENT_VECTORS = """
            SELECT id::text
            FROM vector_store
            WHERE metadata ->> 'contentId' = ?
            AND metadata ->> 'userId' = ?
            """;

    public static final String DELETE_USER_VECTORS = """
            DELETE FROM vector_store
            WHERE metadata ->> 'userId' = ?
            """;

    public static final String DELETE_CONTENT_VECTORS = """
            DELETE FROM vector_store
            WHERE metadata ->> 'contentId' = ?
            AND metadata ->> 'userId' = ?
            """;

    public static final String SEMANTIC_SEARCH_BASE = """
            SELECT
                v.id,
                v.content,
                v.metadata,
                1 - (v.embedding <=> CAST(? AS vector)) AS similarity,
                c.id AS content_id,
                c.title AS content_title,
                t.id AS topic_id,
                t.name AS topic_name,
                s.id AS subject_id,
                s.name AS subject_name,
                d.id AS document_id,
                d.original_filename AS document_filename
            FROM vector_store v
            JOIN content c
                ON c.id = (v.metadata ->> 'contentId')::bigint
            JOIN topic t
                ON t.id = c.topic_id
            JOIN subject s
                ON s.id = t.subject_id
            LEFT JOIN documents d
                ON d.id = c.document_id
            WHERE c.user_id = ?
            AND t.user_id = ?
            AND s.user_id = ?
            AND (v.metadata ->> 'userId')::bigint = ?
            """;

    public static final String SEMANTIC_SEARCH_SUFFIX = """
            AND 1 - (v.embedding <=> CAST(? AS vector)) >= ?
            ORDER BY similarity DESC, v.id
            LIMIT ?
            """;

    private SqlQueries() {
    }
}