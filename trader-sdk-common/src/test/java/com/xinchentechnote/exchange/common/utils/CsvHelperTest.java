package com.xinchentechnote.exchange.common.utils;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CsvHelperTest {

    public static class CsvOrder {
        private String orderId;
        private int price;
        private long quantity;
        private boolean active;
        private double weight;
        private char code;
        private Integer boxSize;

        public CsvOrder() {
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public void setQuantity(long quantity) {
            this.quantity = quantity;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public void setCode(char code) {
            this.code = code;
        }

        public void setBoxSize(Integer boxSize) {
            this.boxSize = boxSize;
        }

        public String getOrderId() {
            return orderId;
        }

        public int getPrice() {
            return price;
        }

        public long getQuantity() {
            return quantity;
        }

        public boolean isActive() {
            return active;
        }

        public double getWeight() {
            return weight;
        }

        public char getCode() {
            return code;
        }

        public Integer getBoxSize() {
            return boxSize;
        }
    }

    @Test
    public void testParseBasic() {
        String csv = "OrderId,Price,Quantity,Active,Weight,Code,BoxSize\n"
                + "A001,10,100,true,1.5,X,20\n"
                + "A002,20,200,false,2.5,Y,25\n";

        List<CsvOrder> orders = CsvHelper.parse(csv, CsvOrder.class);

        assertEquals(2, orders.size());
        CsvOrder first = orders.get(0);
        assertEquals("A001", first.getOrderId());
        assertEquals(10, first.getPrice());
        assertEquals(100L, first.getQuantity());
        assertTrue(first.isActive());
        assertEquals(1.5, first.getWeight(), 0.0001);
        assertEquals('X', first.getCode());
        assertEquals(Integer.valueOf(20), first.getBoxSize());

        CsvOrder second = orders.get(1);
        assertEquals("A002", second.getOrderId());
        assertEquals(20, second.getPrice());
        assertFalse(second.isActive());
    }

    @Test
    public void testParseEmptyContent() {
        assertTrue(CsvHelper.parse("", CsvOrder.class).isEmpty());
        assertTrue(CsvHelper.parse(null, CsvOrder.class).isEmpty());
    }

    @Test
    public void testParseHeaderOnly() {
        assertTrue(CsvHelper.parse("OrderId,Price\n", CsvOrder.class).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseColumnMismatchThrows() {
        String csv = "OrderId,Price,Quantity\n"
                + "A001,10\n";
        CsvHelper.parse(csv, CsvOrder.class);
    }

    @Test
    public void testParseCaseInsensitiveHeader() {
        String csv = "orderid,price\n"
                + "A001,10\n";

        List<CsvOrder> orders = CsvHelper.parse(csv, CsvOrder.class);

        assertEquals(1, orders.size());
        assertEquals("A001", orders.get(0).getOrderId());
        assertEquals(10, orders.get(0).getPrice());
    }

    @Test
    public void testParseUnknownHeaderIsSkipped() {
        String csv = "OrderId,UnknownField,Price\n"
                + "A001,zzz,10\n";

        List<CsvOrder> orders = CsvHelper.parse(csv, CsvOrder.class);

        assertEquals(1, orders.size());
        assertEquals("A001", orders.get(0).getOrderId());
        assertEquals(10, orders.get(0).getPrice());
    }

    @Test
    public void testParseEmptyValueUsesDefault() {
        String csv = "OrderId,Price,Quantity,Active,Code\n"
                + ",,,,X\n";

        List<CsvOrder> orders = CsvHelper.parse(csv, CsvOrder.class);

        assertEquals(1, orders.size());
        assertNull(orders.get(0).getOrderId());
        assertEquals(0, orders.get(0).getPrice());
        assertEquals(0L, orders.get(0).getQuantity());
        assertFalse(orders.get(0).isActive());
        assertEquals('X', orders.get(0).getCode());
    }

    @Test
    public void testParseInvalidNumberFallsBackToDefault() {
        String csv = "OrderId,Price\n"
                + "A001,not-a-number\n";

        List<CsvOrder> orders = CsvHelper.parse(csv, CsvOrder.class);

        assertEquals(1, orders.size());
        assertEquals(0, orders.get(0).getPrice());
    }
}
