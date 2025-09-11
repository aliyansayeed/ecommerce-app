package uniblox.ai.orderservice.model;

import java.util.List;

public class AdminOrdersResponse {

    private String orderId;
    private Order orders;


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Order getOrders() {
        return orders;
    }

    public void setOrders(Order orders) {
        this.orders = orders;
    }
}
