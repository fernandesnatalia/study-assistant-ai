package br.com.studyassistant.studyassistantai.constants;

public final class ErrorMessages {

    public static final String USER_NOT_FOUND = "User not found";
    public static final String AUTH_USER_NOT_FOUND = "Authenticated user not found";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String EMAIL_REGISTERED = "Email already registered";

    public static final String USER_ID_REQUIRED = "User ID is required";
    public static final String SUBJECT_ID_REQUIRED = "Subject ID is required";
    public static final String TOPIC_ID_REQUIRED = "Topic ID is required";
    public static final String CONTENT_ID_REQUIRED = "Content ID is required";
    public static final String DOCUMENT_ID_REQUIRED = "Document ID is required";
    public static final String FILE_REQUIRED = "File is required";
    public static final String TITLE_BODY_REQUIRED = "Title and body are required";
    public static final String QUERY_REQUIRED = "Query is required";
    public static final String SEARCH_REQUEST_REQUIRED = "Search request is required";

    public static final String NOTE_NOT_FOUND = "Note not found";
    public static final String DECK_NOT_FOUND = "Deck not found";
    public static final String FLASHCARD_NOT_FOUND = "Flashcard not found";
    public static final String SUBJECT_NOT_FOUND = "Subject not found";
    public static final String TOPIC_NOT_FOUND = "Topic not found";
    public static final String CONTENT_NOT_FOUND = "Content not found";
    public static final String DOCUMENT_NOT_FOUND = "Document not found";
    public static final String PERFORMANCE_NOT_FOUND = "Performance history not found";

    public static final String INVALID_DOCUMENT_REFERENCE = "Only document content can reference a document";
    public static final String NO_EXTRACTABLE_TEXT = "Document has no extractable text";
    public static final String TEXT_LIMIT_EXCEEDED = "Document exceeds the extracted text limit";
    public static final String TEXT_EXTRACTION_FAILED = "Could not extract document text";
    public static final String DOCUMENT_READ_FAILED = "Could not read document";
    public static final String FILE_LIMIT_EXCEEDED = "File exceeds the 10 MB limit";
    public static final String TITLE_TOO_LONG = "Title must not exceed 255 characters";
    public static final String UNSUPPORTED_EXTENSION = "Only PDF and TXT files are supported";
    public static final String INVALID_FILE_FORMAT = "File format does not match its extension";

    public static final String INVALID_EVALUATION = "AI returned an invalid answer evaluation";
    public static final String INVALID_CONTENT_FORMAT = "AI returned an invalid content format";
    public static final String INVALID_FLASHCARD_FORMAT = "AI returned an invalid flashcard format";
    public static final String NO_FLASHCARDS_GENERATED = "AI did not generate any flashcards";
    
    public static final String MISSING_EXPECTED_ANSWER = "Flashcard does not have an expected answer";
    public static final String ANSWER_ALREADY_EXISTS = "Flashcard already has an answer";
    public static final String DECK_NOT_TRAINING = "Deck is not in TRAINING mode";
    public static final String MANUAL_DECK_STUDY = "Manually created decks must use STUDY mode";
    public static final String FLASHCARD_MODIFICATION_FORBIDDEN = "Flashcards cannot be manually modified in TRAINING mode";
    public static final String DECK_CONVERSION_FORBIDDEN = "Only TRAINING decks can be converted to STUDY";
    public static final String PERFORMANCE_DELETE_FORBIDDEN = "Cannot delete performance history of an active TRAINING deck";
    public static final String PERFORMANCE_CONSOLIDATED = "Deck performance has already been consolidated";

    public static final String NOTE_EXISTS_SUBJECT = "Note already exists for this subject";
    public static final String NOTE_EXISTS_TOPIC = "Note already exists for this topic";
    public static final String NOTE_EXISTS_CONTENT = "Note already exists for this content";
    public static final String NOTE_SINGLE_CONTEXT = "Note must have exactly one context";

    public static final String CONTENT_BLANK = "Content text must not be blank";
    public static final String CONTENT_NOT_PERSISTED = "Content must be persisted before indexing";
    public static final String INVALID_THRESHOLD = "Similarity threshold must be between 0 and 1";
    public static final String INVALID_TOPK = "Top K must be positive";

    public static final String INVALID_SOURCE = "Invalid or incompatible source selection";
    public static final String INSUFFICIENT_SOURCES = "Insufficient sources for exclusive generation";
    public static final String EMPTY_EMBEDDING = "Embedding is empty";
    public static final String INVALID_METADATA = "Invalid vector metadata";

    private ErrorMessages() {
    }
}