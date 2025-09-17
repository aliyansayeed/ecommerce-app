package uniblox.ai.common.api.path;

/**
 * Centralized constants for Checkout module.
 */
public final class CheckoutApiPaths {

    private CheckoutApiPaths() {}

    public static final String API_BASE_PATH = "/api/v1/cart/{userId}";

    // --- Endpoints ---
    public static final String ITEMS_PATH = "/items";
    public static final String CHECKOUT_PATH = "/checkout";

}
