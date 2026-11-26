#!/bin/sh
# Compile Stage 8 code against small API STUBS; does not test actual Spring framework.
set -eu
cd "$(dirname "$0")/.."
T=$(mktemp -d)
trap 'rm -rf "$T"' EXIT
mkdir -p "$T/src"
cat > "$T/src/Stubs.java" <<'SRC'
// This is deliberately unused: one file per public type is generated below.
SRC
python3 - "$T/src" <<'PY'
import sys
from pathlib import Path
root=Path(sys.argv[1])
def put(pkg,name,code):
 p=root/Path(pkg.replace('.','/'))/(name+'.java');p.parent.mkdir(parents=True,exist_ok=True)
 p.write_text('package '+pkg+';\n'+code+'\n')
put('org.springframework.stereotype','Service','public @interface Service {}')
put('org.springframework.stereotype','Controller','public @interface Controller {}')
put('org.springframework.stereotype','Component','public @interface Component {}')
put('org.springframework.web.bind.annotation','ControllerAdvice','public @interface ControllerAdvice { String[] basePackages() default {}; }')
put('org.springframework.web.bind.annotation','GetMapping','public @interface GetMapping {String[] value() default {}; }')
put('org.springframework.web.bind.annotation','PostMapping','public @interface PostMapping {String[] value() default {}; }')
put('org.springframework.web.bind.annotation','PathVariable','public @interface PathVariable {String value() default "";}')
put('org.springframework.web.bind.annotation','RequestParam','public @interface RequestParam {String value() default ""; String defaultValue() default ""; boolean required() default true;}')
put('org.springframework.web.bind.annotation','ModelAttribute','public @interface ModelAttribute {String value() default "";}')
put('org.springframework.transaction.annotation','Transactional','public @interface Transactional {}')
put('org.springframework.http','HttpStatus','public enum HttpStatus {NOT_FOUND}')
put('org.springframework.web.server','ResponseStatusException','public class ResponseStatusException extends RuntimeException {public ResponseStatusException(org.springframework.http.HttpStatus s,String m){} }')
put('org.springframework.dao','DataIntegrityViolationException','public class DataIntegrityViolationException extends RuntimeException {}')
put('org.springframework.dao','DataAccessException','public class DataAccessException extends RuntimeException {}')
put('org.springframework.dao','EmptyResultDataAccessException','public class EmptyResultDataAccessException extends RuntimeException {}')
put('com.example.vongtay.security','CurrentStaff','public class CurrentStaff { public long id(){return 1;} }')
put('org.springframework.security.crypto.password','PasswordEncoder','public interface PasswordEncoder { String encode(CharSequence pass); boolean matches(CharSequence raw, String encoded); }')
put('org.springframework.jdbc.core','RowMapper','public interface RowMapper<T> {T mapRow(java.sql.ResultSet rs,int row) throws java.sql.SQLException;}')
put('org.springframework.jdbc.core.namedparam','MapSqlParameterSource','public class MapSqlParameterSource { public MapSqlParameterSource(){} public MapSqlParameterSource(String k,Object v){} public MapSqlParameterSource addValue(String k,Object v){return this;} }')
put('org.springframework.jdbc.core.namedparam','NamedParameterJdbcTemplate','''
import java.util.*; import org.springframework.jdbc.core.RowMapper;
public class NamedParameterJdbcTemplate {
 public <T> T queryForObject(String s,MapSqlParameterSource p,Class<T> type){return null;}
 public <T> List<T> queryForList(String s,MapSqlParameterSource p,Class<T> type){return null;}
 public List<Map<String,Object>> queryForList(String s,MapSqlParameterSource p){return null;}
 public <T> List<T> query(String s,MapSqlParameterSource p,RowMapper<T> map){return null;}
 public Map<String,Object> queryForMap(String s,MapSqlParameterSource p){return Map.of();}
 public int update(String s,MapSqlParameterSource p){return 0;}
}''')
put('org.springframework.ui','Model','public interface Model {Model addAttribute(String n,Object v); }')
put('org.springframework.web.servlet.mvc.support','RedirectAttributes','public interface RedirectAttributes {RedirectAttributes addFlashAttribute(String n,Object v); RedirectAttributes addAttribute(String n,Object v);}')
put('jakarta.servlet.http','HttpSession','public interface HttpSession {Object getAttribute(String a); void setAttribute(String a,Object o);void removeAttribute(String a);void invalidate();}')
put('jakarta.servlet.http','HttpServletRequest','public interface HttpServletRequest {HttpSession getSession(boolean c);HttpSession getSession();String changeSessionId();}')
put('com.example.vongtay.sales','SaleSupport','''
import java.math.*;
public class SaleSupport {
 public static BigDecimal amount(BigDecimal v,String label,boolean positive){return v;}
 public static String toDisplay(Object time){return "";}
}''')
put('com.example.vongtay.sales','PromotionService','''
import java.math.*; import java.time.*;import java.util.*;
public class PromotionService {public record Rule(String type,BigDecimal value){}
 public Map<Long,List<Rule>> activeRules(java.util.Collection<Long> ids,LocalDateTime now){return Map.of();}
 public static BigDecimal effectivePrice(BigDecimal price,List<Rule> r){return price;}
}''')
put('com.example.vongtay.sales','VoucherService','''
import java.math.*;import java.time.*;
public class VoucherService {public record Redemption(Long voucherId,BigDecimal discount){}
 public Redemption preview(String code,BigDecimal subtotal,LocalDateTime now){return new Redemption(null,BigDecimal.ZERO);}
 public Redemption consume(String code,BigDecimal subtotal,LocalDateTime now){return new Redemption(null,BigDecimal.ZERO);}
}''')
PY
cat > "$T/src/ShopSmoke.java" <<'SRC'
import com.example.vongtay.shop.*;
import java.math.BigDecimal;
import java.util.UUID;
public class ShopSmoke {
 static int checks=0;
 static void check(boolean b){if(!b)throw new AssertionError();checks++;}
 static void fails(Runnable r){try{r.run();throw new AssertionError();}catch(IllegalArgumentException ok){checks++;}}
 public static void main(String[] args){
   check(ShopAuthService.email("  HELLO@Example.COM ").equals("hello@example.com"));
   fails(()->ShopAuthService.email("bogus"));
   check(ShopAuthService.fullName(" Van  A ").equals("Van A"));
   check(ShopAuthService.phone("0912345678",true).equals("0912345678"));
   fails(()->ShopAuthService.phone("no",true));
   ShopAuthService.password("a_secure_password_123");checks++;
   fails(()->ShopAuthService.password("short"));
   ShopCartService.validateQuantity(1);checks++;
   ShopCartService.validateQuantity(20);checks++;
   fails(()->ShopCartService.validateQuantity(0));
   fails(()->ShopCartService.validateQuantity(21));
   check(ShopMoney.shipping(new BigDecimal("499999")).compareTo(new BigDecimal("30000"))==0);
   check(ShopMoney.shipping(new BigDecimal("500000")).signum()==0);
   String token=UUID.randomUUID().toString();
   check(ShopCheckoutService.canonicalToken(token).equals(token));
   fails(()->ShopCheckoutService.canonicalToken("not-a-token"));
   System.out.println("PASS: "+checks+" GD9 checkout / POS / order services compiled, plus pure Java checks; stage code compiled against API stubs (NOT full Spring integration).");
 }
}
SRC
javac --release 17 -encoding UTF-8 -d "$T/out" \
  $(find "$T/src" -name '*.java') \
  src/main/java/com/example/vongtay/shop/*.java \
  src/main/java/com/example/vongtay/orders/OrderWorkflow.java \
  src/main/java/com/example/vongtay/orders/OrderService.java \
  src/main/java/com/example/vongtay/orders/OrderController.java \
  src/main/java/com/example/vongtay/sales/PosForm.java \
  src/main/java/com/example/vongtay/sales/PosService.java
java -cp "$T/out" ShopSmoke
