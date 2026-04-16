# AI Document Analyzer

A production-grade Spring Boot application for document analysis using Spring AI with vector-less RAG (Retrieval-Augmented Generation) and page indexing.

## Features

- **Document Upload**: Support for PDF, DOCX, and TXT files
- **Vector-less RAG**: Page-based indexing without vector embeddings
- **Intelligent Query**: AI-powered document querying using Spring AI
- **Async Processing**: Asynchronous document indexing
- **REST API**: Complete RESTful API for document management
- **Error Handling**: Comprehensive error handling and validation
- **Monitoring**: Actuator endpoints for health monitoring

## Architecture

The application uses a vector-less RAG approach where:
1. Documents are parsed page by page
2. Each page is stored as a separate entity in the database
3. Queries are answered by retrieving relevant pages using keyword search
4. Spring AI generates answers based on the retrieved context

## Technology Stack

- **Spring Boot 3.2.5**
- **Spring AI 1.0.0-M1**
- **H2 Database** (in-memory)
- **Apache PDFBox** (PDF processing)
- **Apache POI** (DOCX processing)
- **Lombok** (boilerplate reduction)
- **Maven** (build tool)

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- OpenAI API key

### Configuration

1. Set your OpenAI API key as an environment variable:
   ```bash
   export OPENAI_API_KEY=your-openai-api-key
   ```

2. Or update `application.yml`:
   ```yaml
   spring:
     ai:
       openai:
         api-key: your-openai-api-key
   ```

### Running the Application

```bash
# Clone the repository
git clone <repository-url>
cd ai-document-analyzer

# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## API Endpoints

### Document Management

#### Upload Document
```http
POST /api/documents/upload
Content-Type: multipart/form-data

file: <document-file>
```

#### Get All Documents
```http
GET /api/documents
```

#### Get Document by ID
```http
GET /api/documents/{id}
```

#### Download Document
```http
GET /api/documents/{id}/download
```

#### Delete Document
```http
DELETE /api/documents/{id}
```

#### Reindex Document
```http
POST /api/documents/{id}/reindex
Content-Type: multipart/form-data

file: <document-file>
```

### Query Operations

#### Query All Documents
```http
POST /api/query
Content-Type: application/json

{
  "query": "What is the main topic discussed in the documents?"
}
```

#### Query Specific Document
```http
POST /api/query/document/{documentId}
Content-Type: application/json

{
  "query": "What information is on page 3?"
}
```

## Configuration Options

### Application Properties

```yaml
app:
  document:
    storage:
      path: ./uploads                    # File storage path
      max-size: 52428800               # Max file size (50MB)
    allowed-types:                     # Supported content types
      - application/pdf
      - application/vnd.openxmlformats-officedocument.wordprocessingml.document
      - text/plain
    processing:
      chunk-size: 1000                 # Text chunk size
      chunk-overlap: 200               # Chunk overlap
      max-pages-per-chunk: 5           # Max pages per chunk
  
  query:
    max-retrieved-pages: 10            # Max pages to retrieve for queries
    answer-max-tokens: 1000           # Max tokens for AI answers
    similarity-threshold: 0.7          # Similarity threshold (future use)
```

## Database Schema

### Documents Table
- `id`: Primary key
- `filename`: Unique filename
- `original_filename`: Original filename
- `content_type`: MIME type
- `file_size`: File size in bytes
- `file_path`: Storage file path
- `total_pages`: Number of pages
- `processing_status`: Processing status (PENDING, PROCESSING, COMPLETED, FAILED)
- `error_message`: Error message if processing failed
- `created_at`: Creation timestamp
- `updated_at`: Last update timestamp

### Document Pages Table
- `id`: Primary key
- `document_id`: Foreign key to documents
- `page_number`: Page number (1-based)
- `page_content`: Text content of the page
- `word_count`: Word count
- `character_count`: Character count
- `created_at`: Creation timestamp

## Monitoring

### Health Check
```http
GET /api/actuator/health
```

### Metrics
```http
GET /api/actuator/metrics
GET /api/actuator/prometheus
```

### H2 Console
```http
GET /api/h2-console
```

## Development

### Project Structure

```
src/main/java/com/ai/document/
├── controller/          # REST controllers
├── dto/                 # Data transfer objects
├── entity/              # JPA entities
├── exception/           # Exception handlers
├── repository/          # Data repositories
├── service/             # Business logic
└── AIDocumentAnalyzerApplication.java  # Main application class
```

### Testing

Run the test suite:
```bash
mvn test
```

### Building for Production

```bash
mvn clean package -Pprod
```

This will create an executable JAR file in the `target` directory.

## Limitations

1. **Vector-less Approach**: Uses keyword-based search instead of semantic similarity
2. **In-memory Database**: H2 database loses data on restart (can be configured for persistence)
3. **Single Model**: Currently configured for OpenAI GPT-4
4. **File Size**: Limited by configured maximum file size (default 50MB)

## Future Enhancements

1. **Vector Database Integration**: Add support for vector databases like Pinecone or Weaviate
2. **Multiple AI Models**: Support for different AI providers
3. **Document Preprocessing**: Advanced document preprocessing and cleaning
4. **Batch Processing**: Support for batch document uploads
5. **User Management**: Multi-tenant support with user authentication
6. **Advanced Search**: Full-text search with ranking
7. **Document Summarization**: Automatic document summarization
8. **Export Options**: Export query results in different formats

## License

This project is licensed under the MIT License.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## Support

For issues and questions, please open an issue on the GitHub repository.
