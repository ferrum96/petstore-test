import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetStoreApiTest {

    private static final String BASE_URL = "https://petstore.swagger.io/v2";
    private static final String ORDER_ENDPOINT = "/store/order";
    private static final String INVENTORY_ENDPOINT = "/store/inventory";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class PositiveStoreTests {

        @Tag("Positive")
        @DisplayName("Размещение заказа")
        @Test
        public void testOrderPlacement() {
            String requestBody = "{ \"id\": 123, \"petId\": 789, \"quantity\": 1, \"shipDate\": \"2024-05-30T12:00:00Z\", \"status\": \"placed\", \"complete\": true }";

            given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(ORDER_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .body("id", notNullValue());
        }

        @Tag("Positive")
        @DisplayName("Размещение заказа с дополнительной информацией")
        @Test
        public void testOrderPlacementWithAdditionalInformation() {
            String requestBody = "{ \"id\": 124, \"petId\": 790, \"quantity\": 2, \"shipDate\": \"2024-05-31T12:00:00Z\", \"status\": \"placed\", \"complete\": true, \"additionalInfo\": \"Express delivery\" }";

            given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(ORDER_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .body("id", notNullValue());
        }

        @Tag("Positive")
        @DisplayName("Получение заказа по id")
        @Test
        public void testOrderRetrievalById() {
            String requestBody = "{ \"id\": 123, \"petId\": 789, \"quantity\": 1, \"status\": \"placed\", \"complete\": true }";

            int orderId = given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(ORDER_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("id");

            given().pathParam("orderId", orderId).when().get(ORDER_ENDPOINT + "/{orderId}").then().statusCode(200).body("id", equalTo(orderId));
        }

        @Tag("Positive")
        @DisplayName("Удаление заказа")
        @Test
        public void testOrderDeletion() {
            String requestBody = "{ \"id\": 123, \"petId\": 789, \"quantity\": 1, \"status\": \"placed\", \"complete\": true }";

            int orderId = given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(ORDER_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("id");

            given().pathParam("orderId", orderId)
                    .when()
                    .delete(ORDER_ENDPOINT + "/{orderId}")
                    .then()
                    .statusCode(200);
        }

        @Tag("Positive")
        @DisplayName("Получение инвентаря")
        @Test
        public void testInventoryRetrieval() {
            given().when()
                    .get(INVENTORY_ENDPOINT)
                    .then()
                    .statusCode(200);
        }

        @Tag("Positive")
        @DisplayName("Проверка наличия позиций инвентаря")
        @Test
        public void testInventoryNotEmpty() {
            given().when()
                    .get(INVENTORY_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .body(not(empty()));
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class NegativeStoreTests {
        @Tag("Negative")
        @DisplayName("Размещение пустого заказа")
        @Test
        public void testOrderPlacementWithEmptyBody() {
            given().contentType(ContentType.JSON)
                    .when()
                    .post(ORDER_ENDPOINT)
                    .then()
                    .statusCode(400);
        }

        @Tag("Negative")
        @DisplayName("Получение заказа по несуществующему числовому id")
        @Test
        public void testOrderRetrievalWithNonexistentId() {
            given().pathParam("orderId", -1)
                    .when()
                    .get(ORDER_ENDPOINT + "/{orderId}")
                    .then()
                    .statusCode(404);
        }

        @Tag("Negative")
        @DisplayName("Получение заказа по несуществующему строковому id")
        @Test
        public void testOrderRetrievalWithInvalidIdFormat() {
            given().pathParam("orderId", "abc")
                    .when()
                    .get(ORDER_ENDPOINT + "/{orderId}")
                    .then()
                    .statusCode(404);
        }

        @Tag("Negative")
        @DisplayName("Удаление заказа по несуществующему числовому id")
        @Test
        public void testNonexistentOrderDeletion() {
            given().pathParam("orderId", -1)
                    .when()
                    .delete(ORDER_ENDPOINT + "/{orderId}")
                    .then()
                    .statusCode(404);
        }

        @Tag("Negative")
        @DisplayName("Удаление заказа по несуществующему строковому id")
        @Test
        public void testOrderDeletionWithInvalidIdFormat() {
            given().pathParam("orderId", "abc")
                    .when()
                    .delete(ORDER_ENDPOINT + "/{orderId}")
                    .then()
                    .statusCode(404);
        }

        @Tag("Negative")
        @DisplayName("Получение инвенторя методом post")
        @Test
        public void testGetInventoryByPostRequest() {
            String requestBody = "{ \"id\": 123, \"petId\": 789, \"quantity\": 1, \"status\": \"placed\", \"complete\": true }";

            given().body(requestBody)
                    .when()
                    .post(INVENTORY_ENDPOINT).then()
                    .statusCode(405);
        }
    }
}
