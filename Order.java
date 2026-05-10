package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ═══════════════════════════════════════════════════════════════
//  OrderDetail  — one line item inside an order
// ═══════════════════════════════════════════════════════════════
class OrderDetail {
    private int    itemId;
    private String itemName;
    private int    quantity;
    private double unitPrice;   // price snapshot at time of order

    public OrderDetail() {}

    public OrderDetail(int itemId, String itemName, int quantity, double unitPrice) {
        this.itemId    = itemId;
        this.itemName  = itemName;
        this.quantity  = quantity;
        this.unitPrice = unitPrice;
    }

    public double getSubtotal() { return quantity * unitPrice; }

    // Getters & Setters
    public int    getItemId()    { return itemId;    }
    public String getItemName()  { return itemName;  }
    public int    getQuantity()  { return quantity;  }
    public double getUnitPrice() { return unitPrice; }

    public void setItemId(int itemId)          { this.itemId    = itemId;    }
    public void setItemName(String itemName)   { this.itemName  = itemName;  }
    public void setQuantity(int quantity)      { this.quantity  = quantity;  }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    @Override
    public String toString() {
        return itemName + " x" + quantity + " @ Rs." + unitPrice
                + " = Rs." + getSubtotal();
    }
}

// ═══════════════════════════════════════════════════════════════
//  Order  — the parent order record
// ═══════════════════════════════════════════════════════════════
public class Order {
    private int               orderId;
    private int               userId;
    private String            userName;
    private LocalDateTime     orderDate;
    private double            totalAmount;
    private String            status;
    private List<OrderDetail> details;

    public Order() {
        this.details = new ArrayList<>();
    }

    public Order(int userId, double totalAmount) {
        this.userId      = userId;
        this.totalAmount = totalAmount;
        this.status      = "pending";
        this.details     = new ArrayList<>();
    }

    // ── Getters & Setters ─────────────────────────────────────
    public int               getOrderId()     { return orderId;     }
    public int               getUserId()      { return userId;      }
    public String            getUserName()    { return userName;    }
    public LocalDateTime     getOrderDate()   { return orderDate;   }
    public double            getTotalAmount() { return totalAmount; }
    public String            getStatus()      { return status;      }
    public List<OrderDetail> getDetails()     { return details;     }

    public void setOrderId(int orderId)              { this.orderId     = orderId;     }
    public void setUserId(int userId)                { this.userId      = userId;      }
    public void setUserName(String userName)         { this.userName    = userName;    }
    public void setOrderDate(LocalDateTime orderDate){ this.orderDate   = orderDate;   }
    public void setTotalAmount(double totalAmount)   { this.totalAmount = totalAmount; }
    public void setStatus(String status)             { this.status      = status;      }
    public void setDetails(List<OrderDetail> details){ this.details     = details;     }

    public void addDetail(OrderDetail d) { this.details.add(d); }

    @Override
    public String toString() {
        return "Order{id=" + orderId + ", user=" + userId
                + ", total=Rs." + totalAmount + ", status='" + status + "'}";
    }
}
