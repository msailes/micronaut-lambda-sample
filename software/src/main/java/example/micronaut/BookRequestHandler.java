package example.micronaut;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import jakarta.inject.Inject;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.Map;
import java.util.UUID;

@Introspected
public class BookRequestHandler extends MicronautRequestHandler<Book, BookSaved> {

    private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.EU_WEST_1)
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();

    @Override
    public BookSaved execute(Book book) {
        String id = UUID.randomUUID().toString();
        PutItemResponse putItemResponse = dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName("books")
                .item(Map.of("id", stringAttribute(id),
                        "name", stringAttribute(book.getName())))
                .build());

        BookSaved bookSaved = new BookSaved();
        bookSaved.setName(book.getName());
        bookSaved.setIsbn(id);
        return bookSaved;
    }

    private AttributeValue stringAttribute(String string) {
        return AttributeValue.builder().s(string).build();
    }
}
