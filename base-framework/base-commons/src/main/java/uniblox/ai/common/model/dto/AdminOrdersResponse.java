package uniblox.ai.common.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import uniblox.ai.common.model.entity.Order;
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AdminOrdersResponse {

    private String orderId;
    private Order orders;

    public AdminOrdersResponse() {}

    public AdminOrdersResponse(String orderId, Order orders) {
        this.orderId = orderId;
        this.orders = orders;
    }
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

   // private String orderId;
    //private Order orders;


    //public String getOrderId() {
      //  return orderId;
    //}

    //public void setOrderId(String orderId) {
      //  this.orderId = orderId;
    //}

   // public Order getOrders() {
     //   return orders;
    //}

  //  public void setOrders(Order orders) {
    //    this.orders = orders;
    //}
}
