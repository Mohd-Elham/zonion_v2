    package com.example.demo.models;


    import org.springframework.data.annotation.Id;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Map;

    public class Orders {

        @Id
        private String id;
        private String userId;
        private List<OrderItem> items;
        private double totalPrice;
        private double offerDiscount;
        private Map<String, String> appliedOffers;

        private LocalDateTime deliveryDate;
        private String shippingAddress;
        private PaymentType paymentMethod;
        private String transactionId;
        private String razorpayId;
        private String paymentStatus;

        private double couponDiscount;
        private String status;
        private LocalDateTime orderDate;

        public String getRazorpayId() {
            return razorpayId;
        }

        public void setRazorpayId(String razorpayId) {
            this.razorpayId = razorpayId;
        }

        public double getOfferDiscount() {
            return offerDiscount;
        }

        public void setOfferDiscount(double offerDiscount) {
            this.offerDiscount = offerDiscount;
        }

        public Map<String, String> getAppliedOffers() {
            return appliedOffers;
        }

        public void setAppliedOffers(Map<String, String> appliedOffers) {
            this.appliedOffers = appliedOffers;
        }



        public double getCouponDiscount() {
            return couponDiscount;
        }

        public void setCouponDiscount(double discountedValue) {
            this.couponDiscount = discountedValue;
        }


        public Orders() {
        }

        @Override
        public String toString() {
            return "Orders{" +
                    "id='" + id + '\'' +
                    ", userId='" + userId + '\'' +
                    ", items=" + items +
                    ", totalPrice=" + totalPrice +
                    ", discountedValue=" + couponDiscount +
                    ", status='" + status + '\'' +
                    ", deliveryDate=" + deliveryDate +
                    ", shippingAddress='" + shippingAddress + '\'' +
                    ", paymentMethod=" + paymentMethod +
                    ", transactionId='" + transactionId + '\'' +
                    '}';
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public void setItems(List<OrderItem> items) {
            this.items = items;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getDeliveryDate() {
            return deliveryDate;
        }

        public void setDeliveryDate(LocalDateTime deliveryDate) {
            this.deliveryDate = deliveryDate;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public PaymentType getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(PaymentType paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public LocalDateTime getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(LocalDateTime orderDate) {
            this.orderDate = orderDate;
        }

        public Orders(String id, String userId, List<OrderItem> items, double totalPrice,
                      double discountedValue,String status, LocalDateTime orderDate,
                      LocalDateTime deliveryDate,String shippingAddress,
                      PaymentType paymentMethod,String transactionId) {
            this.id = id;
            this.userId = userId;
            this.items = items;
            this.totalPrice = totalPrice;
            this.couponDiscount = discountedValue;
            this.status = status;
            this.deliveryDate = deliveryDate;
            this.shippingAddress = shippingAddress;
            this.paymentMethod = paymentMethod;
            this.transactionId = transactionId;
        }

        public void setRazorpayId(Object id) {
        }

        public static class OrderItem {
            private String productId;
            private int quantity;
            private double price;
            private double discountValue;

            public OrderItem(String productId, int quantity, double price) {
                this.productId = productId;
                this.quantity = quantity;
                this.price = price;

            }

            @Override
            public String toString() {
                return "OrderItem{" +
                        "productId='" + productId + '\'' +
                        ", quantity='" + quantity + '\'' +
                        ", price='" + price + '\'' +
                        '}';
            }

            public String getProductId() {
                return productId;
            }

            public void setProductId(String productId) {
                this.productId = productId;
            }

            public int getQuantity() {
                return quantity;
            }

            public void setQuantity(int quantity) {
                this.quantity = quantity;
            }

            public double getPrice() {
                return price;
            }

            public void setPrice(double price) {
                this.price = price;
            }
        }


    }
