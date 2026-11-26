import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.example.vongtay.orders.OrderWorkflow;
import com.example.vongtay.people.PeopleValidation;
import com.example.vongtay.sales.SaleSupport;

/** Pure-JDK smoke tests: no Spring Boot or SQL Server connection. */
public class JavaSmoke {
    static int checks = 0;
    static void eq(Object actual,Object expected) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + " but got " + actual);
        checks++;
    }
    static void yes(boolean value) {if (!value) throw new AssertionError("expected true");checks++;}
    static void fail(Runnable f) {
        try {f.run();} catch (IllegalArgumentException expected) {checks++;return;}
        throw new AssertionError("Expected IllegalArgumentException");
    }
    public static void main(String[] args) {
        eq(OrderWorkflow.next(OrderWorkflow.PENDING),OrderWorkflow.PROCESSING);
        eq(OrderWorkflow.next(OrderWorkflow.PROCESSING),OrderWorkflow.PROCESSED);
        eq(OrderWorkflow.next(OrderWorkflow.PROCESSED),OrderWorkflow.COMPLETED);
        yes(OrderWorkflow.next(OrderWorkflow.COMPLETED)==null);
        yes(OrderWorkflow.next(OrderWorkflow.CANCELLED)==null);
        yes(!OrderWorkflow.mayCancel(OrderWorkflow.COMPLETED));
        yes(OrderWorkflow.mayCancel(OrderWorkflow.PROCESSED));
        eq(SaleSupport.code(" welcome10 "),"WELCOME10");
        fail(()->SaleSupport.code("welcome code"));
        eq(SaleSupport.utcFromForm("2026-10-24T11:30"),LocalDateTime.of(2026,10,24,4,30));
        eq(SaleSupport.discount(new BigDecimal("200000.00"),"PHAN_TRAM",new BigDecimal("10"),null),new BigDecimal("20000.00"));
        eq(SaleSupport.discount(new BigDecimal("200000.00"),"CO_DINH",new BigDecimal("300000"),null),new BigDecimal("200000.00"));
        eq(SaleSupport.discount(new BigDecimal("200000.00"),"PHAN_TRAM",new BigDecimal("20"),new BigDecimal("25000")),new BigDecimal("25000.00"));
        fail(()->SaleSupport.amount(new BigDecimal("10000000000000000"),"price",true));
        fail(()->SaleSupport.validateDiscount("PHAN_TRAM",new BigDecimal("101")));
        fail(()->SaleSupport.amount(new BigDecimal("-1"),"price",false));
        eq(PeopleValidation.email("  TEST@EXAMPLE.COM ",true),"test@example.com");
        fail(()->PeopleValidation.email("not-an-email",true));
        fail(()->PeopleValidation.password("short",true));
        eq(PeopleValidation.birthday(LocalDate.of(2001,2,3)),LocalDate.of(2001,2,3));
        fail(()->PeopleValidation.birthday(LocalDate.of(1890,1,1)));
        System.out.println("PASS: " + checks + " pure Java assertions (workflow, discount, inputs, time).");
    }
}
