package uniblox.ai.common.api.path;

public final class CartApiPaths {

    private CartApiPaths() {
        // utility class, no instantiation
    }

    // --- API base path ---
    public static final String API_BASE_PATH = "/api/v1/cart";

    // --- Endpoints ---
    public static final String ITEMS_PATH = "/{userId}/items";
    public static final String CART_PATH = "/{userId}";
    public static final String CHECKOUT_PATH = "/{userId}/checkout";
}

