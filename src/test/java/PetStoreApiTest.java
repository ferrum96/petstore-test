import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

public class PetStoreApiTest {

    private static final String BASE_URL = "https://petstore.swagger.io/v2";
    private static final String ORDER_ENDPOINT = "/store/order";
    private static final String INVENTORY_ENDPOINT = "/store/inventory";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void testSuccessfulOrderPlacement() {
        String requestBody = "{ \"id\": 123, \"petId\": 789, \"quantity\": 1, \"shipDate\": \"2024-05-30T12:00:00Z\", \"status\": \"placed\", \"complete\": true }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(ORDER_ENDPOINT)
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Test
    public void testOrderPlacementWithAdditionalInformation() {
        String requestBody = "{ \"id\": 124, \"petId\": 790, \"quantity\": 2, \"shipDate\": \"2024-05-31T12:00:00Z\", \"status\": \"placed\", \"complete\": true, \"additionalInfo\": \"Express delivery\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(ORDER_ENDPOINT)
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Test
    public void testMultipleOrdersPlacement() {
        String requestBody1 = "{ \"id\": 125, \"petId\": 791, \"quantity\": 3, \"shipDate\": \"2024-06-01T12:00:00Z\", \"status\": \"placed\", \"complete\": true }";
        String requestBody2 = "{ \"id\": 126, \"petId\": 792, \"quantity\": 4, \"shipDate\": \"2024-06-02T12:00:00Z\", \"status\": \"placed\", \"complete\": true }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody1)
                .when()
                .post(ORDER_ENDPOINT);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody2)
                .when()
                .post(ORDER_ENDPOINT);

        when()
                .get(ORDER_ENDPOINT)
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }

    @Test
    public void testOrderPlacementWithEmptyBody() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post(ORDER_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    public void testSuccessfulOrderRetrieval() {
        // Создание нового заказа
        int orderId = given()
                .contentType(ContentType.JSON)
                .body("{ \"id\": 123, \"petId\": 789, \"quantity\": 1, \"status\": \"placed\", \"complete\": true }")
                .when()
                .post(ORDER_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        // Запрос на получение созданного заказа
        given()
                .pathParam("orderId", orderId)
                .when()
                .get(ORDER_ENDPOINT + "/{orderId}")
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId));
    }

    @Test
    public void testOrderRetrievalWithAdditionalInfo() {
        // Создание нового заказа
        int orderId = given()
                .contentType(ContentType.JSON)
                .body("{ \"id\": 124, \"petId\": 790, \"quantity\": 2, \"status\": \"placed\", \"complete\": true, \"additionalInfo\": \"Express delivery\" }")
                .when()
                .post(ORDER_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        // Запрос на получение созданного заказа с дополнительной информацией
        given()
                .pathParam("orderId", orderId)
                .when()
                .get(ORDER_ENDPOINT + "/{orderId}?additionalInfo=true")
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId))
                .body("additionalInfo", equalTo("Express delivery"));
    }

    @Test
    public void testMultipleOrdersRetrieval() {
        // Запрос на получение всех заказов
        given()
                .when()
                .get(ORDER_ENDPOINT)
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    public void testOrderRetrievalWithEmptyBody() {
        // Запрос на получение заказа без указания ID
        given()
                .when()
                .get(ORDER_ENDPOINT + "/")
                .then()
                .statusCode(404);
    }

    @Test
    public void testOrderRetrievalWithNonexistentId() {
        // Запрос на получение заказа по несуществующему ID
        given()
                .pathParam("orderId", 9999)
                .when()
                .get(ORDER_ENDPOINT + "/{orderId}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testOrderRetrievalWithInvalidIdFormat() {
        // Запрос на получение заказа с невалидным форматом ID
        given()
                .pathParam("orderId", "abc")
                .when()
                .get(ORDER_ENDPOINT + "/{orderId}")
                .then()
                .statusCode(400);
    }

    @Test
    public void testOrderRetrievalFromRemoteStore() {
        // Изменяем базовый URL на недоступный магазин
        RestAssured.baseURI = "https://unavailable-store.swagger.io/v2";

        // Запрос на получение заказа из удаленного магазина
        given()
                .pathParam("orderId", 123)
                .when()
                .get(ORDER_ENDPOINT + "/{orderId}")
                .then()
                .statusCode(500);
    }


    @Test
    public void testSuccessfulOrderDeletion() {
        // Создание нового заказа для последующего удаления
        int orderId = given()
                .contentType(ContentType.JSON)
                .body("{ \"id\": 123, \"petId\": 789, \"quantity\": 1, \"status\": \"placed\", \"complete\": true }")
                .when()
                .post(ORDER_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        // Удаление заказа по корректному ID
        given()
                .pathParam("orderId", orderId)
                .when()
                .delete(ORDER_ENDPOINT + "/{orderId}")
                .then()
                .statusCode(200);
    }

    @Test
    public void testNonexistentOrderDeletion() {
        // Попытка удаления несуществующего заказа
        given()
                .pathParam("orderId", 9999)
                .when()
                .delete(ORDER_ENDPOINT + "/{orderId}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testSuccessfulAllOrdersDeletion() {
        // Удаление всех заказов в магазине
        given()
                .when()
                .delete(ORDER_ENDPOINT)
                .then()
                .statusCode(200);
    }

    @Test
    public void testOrderDeletionWithEmptyId() {
        // Попытка удаления заказа без указания ID
        given()
                .when()
                .delete(ORDER_ENDPOINT + "/")
                .then()
                .statusCode(404);
    }

    @Test
    public void testOrderDeletionWithInvalidIdFormat() {
        // Попытка удаления заказа с невалидным форматом ID
        given()
                .pathParam("orderId", "abc")
                .when()
                .delete(ORDER_ENDPOINT + "/{orderId}")
                .then()
                .statusCode(400);
    }

    @Test
    public void testOrderDeletionWithoutAccess() {
        // Попытка удаления заказа, на который нет доступа
        given()
                .pathParam("orderId", 123) // Заказ, созданный в другом тесте
                .when()
                .delete(ORDER_ENDPOINT + "/{orderId}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testOrderDeletionFromRemoteStore() {
        // Изменяем базовый URL на недоступный магазин
        RestAssured.baseURI = "https://unavailable-store.swagger.io/v2";

        // Попытка удаления заказа из удаленного магазина
        given()
                .pathParam("orderId", 123) // Заказ, созданный в другом тесте
                .when()
                .delete(ORDER_ENDPOINT + "/{orderId}")
                .then()
                .statusCode(500);
    }

    @Test
    public void testSuccessfulInventoryRetrieval() {
        // Получение инвентаря и проверка успешного ответа
        given()
                .when()
                .get(INVENTORY_ENDPOINT)
                .then()
                .statusCode(200);
    }

    @Test
    public void testInventoryNotEmpty() {
        // Получение инвентаря и проверка, что он не пустой
        given()
                .when()
                .get(INVENTORY_ENDPOINT)
                .then()
                .statusCode(200)
                .body(not(empty()));
    }

    @Test
    public void testInventoryContainsSpecificItems() {
        // Получение инвентаря и проверка наличия конкретных товаров
        given()
                .when()
                .get(INVENTORY_ENDPOINT)
                .then()
                .statusCode(200)
                .body("find { it.key == 'available' }.value", greaterThan(0))
                .body("find { it.key == 'pending' }.value", greaterThan(0))
                .body("find { it.key == 'sold' }.value", greaterThan(0));
    }

    @Test
    public void testInventoryWithEmptyBody() {
        // Попытка получения инвентаря с пустым телом запроса
        given()
                .when()
                .get(INVENTORY_ENDPOINT + "/")
                .then()
                .statusCode(404);
    }

    @Test
    public void testInventoryRetrievalFromRemoteStore() {
        // Изменяем базовый URL на недоступный магазин
        RestAssured.baseURI = "https://unavailable-store.swagger.io/v2";

        // Попытка получения инвентаря из удаленного магазина
        given()
                .when()
                .get(INVENTORY_ENDPOINT)
                .then()
                .statusCode(500);
    }
}
