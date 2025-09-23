package uniblox.ai.common.api.path;
//package uniblox.ai.common.api.path;

// TODO MOVE THIS TO COMMON AFTER TESTING -fixed already
public final class OrderApiPaths {

    private OrderApiPaths() {}

    public static final String API_BASE_PATH = "/api/v1/orders";

    // ✅ FIXED: create now expects userId
    public static final String CREATE_PATH = "/user/{userId}";
    public static final String BY_ID_PATH = "/{orderId}";
    public static final String ALL_PATH = "/all";
    public static final String BY_USER_PATH = "/user/{userId}";
    public static final String STATUS_UPDATE_PATH = "/{orderId}/status";
}

