import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;


public class APItest {

    private static String baseURL = "https://automationintesting.online";
    private static String brandingURL = baseURL + "/branding/";
    private static String bookingURL = baseURL + "/booking/";
    private static String messageURL = baseURL + "/message/";
    private static String loginURL = baseURL + "/auth/login";
    private static String logOutURL = baseURL + "/auth/logout";
    private static String roomURL = baseURL + "/room/";
    private static String createdRoomId;
    private static String reportURL = baseURL + "/report/";

    private static String token;

    private static String bookingPayload = "{\"bookingdates\":{\"checkin\":\"2020-11-10\",\"checkout\":\"2020-11-11\"},\"depositpaid\":false,\"firstname\":\"Mark\",\"lastname\":\"Bridger\",\"roomid\":1,\"email\":\"mark@mark.com\",\"phone\":\"0167653897646\"}";
    private static String roomPayload = "{\"roomNumber\":\"102\",\"type\":\"Family\",\"accessible\":false,\"description\":\"Please enter a description for this room\",\"image\":\"https://www.mwtestconsultancy.co.uk/img/room1.jpg\",\"roomPrice\":\"68\",\"features\":[\"WiFi\"]}";
    private static String messagePayload = "{\"name\":\"Mark\",\"email\":\"mark@mark.com\",\"phone\":\"01676543212\",\"subject\":\"hello\",\"description\":\"hi there, i love automation yo\"}";

    private static Headers commonHeaders;

    //this is bug branch

    public Headers getCommonHeaders() {
        Header header1 = new Header("authority", "automationintesting.online");
        Header header2 = new Header("accept", "application/json");
        Header header3 = new Header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
        Header header4 = new Header("content-type", "application/json");
        Header header5 = new Header("origin", "https://automationintesting.online");
        Header header6 = new Header("sec-fetch-site", "same-origin");
        Header header7 = new Header("sec-fetch-mode", "cors");
        Header header8 = new Header("sec-fetch-dest", "empty");
        Header header9 = new Header("referer", "https://automationintesting.online/");
        Header header10 = new Header("content-type", "application/json");
        Header header11 = new Header("accept-language", "en-GB,en;q=0.9");

        commonHeaders = new Headers(header1,header2, header3, header4, header5, header6, header7, header8, header9, header10, header11);
        return commonHeaders;
    }

//    @Ignore
    @Test
    public void brandingAPI(){
        RestAssured.get(
                brandingURL)
                .then()
                .assertThat()
                .body("name", equalTo("Shady Meadows B&B"))
                // map: works in other method as string, how do it here? convert to int?
//                .body("map.latitude", equalTo("52.63512").)
//                .body("map.longitude", equalTo("1.2733774"))
                .body("logoUrl", equalTo("https://www.mwtestconsultancy.co.uk/img/rbp-logo.png"))
// Description:     unable to use    //.body("[3]", hasSize(1))   JSON path [0] doesn't match
                .body("contact.name", equalTo("Shady Meadows B&B"))
                .body("contact.address", equalTo("The Old Farmhouse, Shady Street, Newfordburyshire, NE1 410S"))
                .body("contact.phone", equalTo("012345678901"))
                .body("contact.email", equalTo("fake@fakeemail.com"));
    }

//    @Ignore
    @Test
    public void brandingAPIUsingPath(){
        Response response = RestAssured.get(
                brandingURL).
                andReturn();
        String json = response.getBody().asString();

        int statusCode = response.getStatusCode();
        JsonPath jsonPath = new JsonPath(json);

        Assert.assertEquals(200, statusCode);
        Assert.assertEquals("Shady Meadows B&B", jsonPath.getString("name"));
        Assert.assertEquals("https://www.mwtestconsultancy.co.uk/img/rbp-logo.png", jsonPath.getString("logoUrl"));
        Assert.assertEquals("Shady Meadows B&B", jsonPath.getString("contact.name"));
        Assert.assertEquals("The Old Farmhouse, Shady Street, Newfordburyshire, NE1 410S", jsonPath.getString("contact.address"));
        Assert.assertEquals("012345678901", jsonPath.getString("contact.phone"));
        Assert.assertEquals("fake@fakeemail.com", jsonPath.getString("contact.email"));
        Assert.assertEquals("52.63512", jsonPath.getString("map.latitude"));
        Assert.assertEquals("1.2733774", jsonPath.getString("map.longitude"));
        Assert.assertNotNull(jsonPath.getString("description"));
    }

//    @Ignore
    @Test
    public void bookingAPI(){
        getCommonHeaders();
        Response response = given().headers(commonHeaders)
                .header("cookie", "__cfduid=dd0d27d252e52a9e64f94f6b92dc730b91602667088; _ga=GA1.2.1496861748.1602667090; _gid=GA1.2.362298640.1602667090; banner=true; token=o65BSHf4O5Blv3WY")
                .body(bookingPayload)
                .post(bookingURL);

        int statusCode = response.getStatusCode();
        String json = response.getBody().asString();

        Assert.assertEquals(201, statusCode);
        assertThat(json, containsString("bookingid"));
    }

    @Test
    public void messageAPI() {
        getCommonHeaders();
        Response response = given().headers(commonHeaders)
                .header("cookie", "__cfduid=dd0d27d252e52a9e64f94f6b92dc730b91602667088; _ga=GA1.2.1496861748.1602667090; _gid=GA1.2.362298640.1602667090; banner=true; token=o65BSHf4O5Blv3WY")
                .body(messagePayload)
                .post(messageURL);

        int statusCode = response.getStatusCode();
        Assert.assertEquals(201, statusCode);

        String json = response.getBody().asString();
        JsonPath jsonPath = new JsonPath(json);
        Assert.assertEquals("Mark", jsonPath.getString("name"));
        Assert.assertNotNull(jsonPath.getString("messageid"));
    }


//    @Ignore
    @Test
    public void authAndRoomAPI(){
        logIn();

        //Create a room
        Response createRoomResponse = createRoom();
        String json = createRoomResponse.getBody().asString();
        int statusCode = createRoomResponse.getStatusCode();
        JsonPath jsonPath = new JsonPath(json);
        createdRoomId = jsonPath.getString("roomid");

        Assert.assertEquals(201, statusCode);
        Assert.assertEquals("102", jsonPath.getString("roomNumber"));
        Assert.assertEquals("Family", jsonPath.getString("type"));
        Assert.assertEquals("false", jsonPath.getString("accessible"));
        Assert.assertEquals("https://www.mwtestconsultancy.co.uk/img/room1.jpg", jsonPath.getString("image"));
        Assert.assertEquals("[WiFi]", jsonPath.getString("features"));
        Assert.assertEquals("68", jsonPath.getString("roomPrice"));
        Assert.assertNotNull(jsonPath.getString("description"));
        Assert.assertNotNull(jsonPath.getString("roomid"));

        //Delete room
        Response deleteRoomResponse = deleteRoom(createdRoomId);

        //Log Out
        logOut();
    }

    public void logIn() {
        getCommonHeaders();
        Response response = (Response) given().headers(commonHeaders)
                .header("cookie", "__cfduid=dd0d27d252e52a9e64f94f6b92dc730b91602667088; _ga=GA1.2.1496861748.1602667090; _gid=GA1.2.362298640.1602667090; banner=true; _gat=1")
                .body("{\"username\":\"admin\",\"password\":\"password\"}")
                .post(loginURL);


        int statusCode = response.getStatusCode();
        String json = response.getBody().asString();

        Assert.assertEquals(200, statusCode);
        assertThat(json, containsString("token"));

        JsonPath jsonPath = new JsonPath(json);
        token = jsonPath.getString("token");
        System.out.println("token is: " + token);
    }

    public void logOut() {
        getCommonHeaders();
        Response response2 = (Response) given().headers(commonHeaders)
                .header("cookie", "__cfduid=dd0d27d252e52a9e64f94f6b92dc730b91602667088; _ga=GA1.2.1496861748.1602667090; _gid=GA1.2.362298640.1602667090; banner=true; token=" + token)
                .body("{\"token\":\"" + token + "\"}")
                .post(logOutURL);

        int statusCode2 = response2.getStatusCode();
        Assert.assertEquals(200, statusCode2);
    }

    public Response createRoom() {
        Response createRoomResponse = (Response) given().headers(commonHeaders)
                .header("cookie", "__cfduid=dd0d27d252e52a9e64f94f6b92dc730b91602667088; _ga=GA1.2.1496861748.1602667090; _gid=GA1.2.362298640.1602667090; banner=true; token=" + token)
                .body(roomPayload)
                .post(roomURL);

        return createRoomResponse;
    }

    public Response deleteRoom(String createdRoomId) {
        Response deleteRoomResponse = (Response) given().header("authority", "automationintesting.online")
                .header("accept", "*/*")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36")
                .header("content-type", "application/json")
                .header("origin", "https://automationintesting.online")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-dest", "empty")
                .header("referer", "https://automationintesting.online/")
                .header("accept-language", "en-GB,en;q=0.9")
                .header("cookie", "__cfduid=dd0d27d252e52a9e64f94f6b92dc730b91602667088; _ga=GA1.2.1496861748.1602667090; _gid=GA1.2.362298640.1602667090; banner=true; token=" + token)
                .delete(roomURL + createdRoomId);

        System.out.println("delete Room postUrl = " + roomURL + createdRoomId);
        System.out.println("delete Room token: " + token);

        int statusCode = deleteRoomResponse.getStatusCode();
        Assert.assertEquals(202, statusCode);

        return deleteRoomResponse;
    }
}
