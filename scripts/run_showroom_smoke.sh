#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
T=$(mktemp -d)
trap 'rm -rf "$T"' EXIT
mkdir -p "$T/org/springframework/stereotype" "$T/org/springframework/http" "$T/org/springframework/web/server" \
  "$T/org/springframework/jdbc/core" "$T/org/springframework/jdbc/core/namedparam" \
  "$T/org/springframework/ui" "$T/org/springframework/web/bind/annotation" \
  "$T/org/springframework/beans/factory/annotation" "$T/org/springframework/context/annotation" \
  "$T/org/springframework/web/servlet/config/annotation"
cat > "$T/org/springframework/stereotype/Service.java" <<'SRC'
package org.springframework.stereotype; public @interface Service {}
SRC
cat > "$T/org/springframework/stereotype/Controller.java" <<'SRC'
package org.springframework.stereotype; public @interface Controller {}
SRC
cat > "$T/org/springframework/http/HttpStatus.java" <<'SRC'
package org.springframework.http; public enum HttpStatus { NOT_FOUND }
SRC
cat > "$T/org/springframework/web/server/ResponseStatusException.java" <<'SRC'
package org.springframework.web.server;
import org.springframework.http.HttpStatus;
public class ResponseStatusException extends RuntimeException { public ResponseStatusException(HttpStatus code) {} }
SRC
cat > "$T/org/springframework/jdbc/core/RowMapper.java" <<'SRC'
package org.springframework.jdbc.core;
import java.sql.ResultSet; import java.sql.SQLException;
public interface RowMapper<T> { T mapRow(ResultSet row, int number) throws SQLException; }
SRC
cat > "$T/org/springframework/jdbc/core/namedparam/MapSqlParameterSource.java" <<'SRC'
package org.springframework.jdbc.core.namedparam;
public class MapSqlParameterSource { public MapSqlParameterSource addValue(String key,Object value) { return this; } }
SRC
cat > "$T/org/springframework/jdbc/core/namedparam/NamedParameterJdbcTemplate.java" <<'SRC'
package org.springframework.jdbc.core.namedparam;
import java.util.List; import java.util.Map; import org.springframework.jdbc.core.RowMapper;
public class NamedParameterJdbcTemplate {
 public <T> T queryForObject(String sql,MapSqlParameterSource params,Class<T> type) {return null;}
 public <T> List<T> query(String sql,MapSqlParameterSource params,RowMapper<T> map) {return List.of();}
 public <T> List<T> query(String sql,Map<String,?> params,RowMapper<T> map) {return List.of();}
}
SRC
cat > "$T/org/springframework/ui/Model.java" <<'SRC'
package org.springframework.ui; public interface Model { Model addAttribute(String key,Object value); }
SRC
cat > "$T/org/springframework/web/bind/annotation/GetMapping.java" <<'SRC'
package org.springframework.web.bind.annotation; public @interface GetMapping { String[] value() default {}; }
SRC
cat > "$T/org/springframework/web/bind/annotation/PathVariable.java" <<'SRC'
package org.springframework.web.bind.annotation; public @interface PathVariable {String value() default ""; }
SRC
cat > "$T/org/springframework/web/bind/annotation/RequestParam.java" <<'SRC'
package org.springframework.web.bind.annotation; public @interface RequestParam { String value() default ""; boolean required() default true; }
SRC
cat > "$T/org/springframework/beans/factory/annotation/Value.java" <<'SRC'
package org.springframework.beans.factory.annotation; public @interface Value {String value();}
SRC
cat > "$T/org/springframework/context/annotation/Configuration.java" <<'SRC'
package org.springframework.context.annotation; public @interface Configuration {}
SRC
cat > "$T/org/springframework/web/servlet/config/annotation/ResourceHandlerRegistry.java" <<'SRC'
package org.springframework.web.servlet.config.annotation;
public class ResourceHandlerRegistry { public ResourceHandlerRegistration addResourceHandler(String... patterns){return new ResourceHandlerRegistration();} }
SRC
cat > "$T/org/springframework/web/servlet/config/annotation/ResourceHandlerRegistration.java" <<'SRC'
package org.springframework.web.servlet.config.annotation;
public class ResourceHandlerRegistration { public ResourceHandlerRegistration addResourceLocations(String... locations){return this;} }
SRC
cat > "$T/org/springframework/web/servlet/config/annotation/WebMvcConfigurer.java" <<'SRC'
package org.springframework.web.servlet.config.annotation;
public interface WebMvcConfigurer { default void addResourceHandlers(ResourceHandlerRegistry registry) {} }
SRC
cat > "$T/ShowroomSmoke.java" <<'SRC'
import com.example.vongtay.showroom.ShowroomService;
public class ShowroomSmoke {
 static int checks=0;
 static void eq(Object a,Object b){if(!a.equals(b))throw new AssertionError(a+" != "+b);checks++;}
 public static void main(String[] args){
   eq(ShowroomService.cleanKeyword(null),"");
   eq(ShowroomService.cleanKeyword("  Dawn  "),"Dawn");
   eq(ShowroomService.cleanKeyword("A\r\nB"),"AB");
   eq(ShowroomService.cleanKeyword("x".repeat(120)).length(),80);
   eq(ShowroomService.cleanPage(null),1);
   eq(ShowroomService.cleanPage(-3),1);
   eq(ShowroomService.cleanPage(4),4);
   eq(ShowroomService.cleanPage(10000),1000);
   eq(ShowroomService.PAGE_SIZE,12);
   System.out.println("PASS: "+checks+" showroom sanitizer/paging assertions; Java compiled with dependency STUBS only.");
 }
}
SRC
javac --release 17 -encoding UTF-8 -d "$T/out" \
  $(find "$T/org" -type f -name '*.java') \
  src/main/java/com/example/vongtay/shop/SizeSelection.java \
  src/main/java/com/example/vongtay/showroom/ShowroomService.java \
  src/main/java/com/example/vongtay/showroom/ShowroomController.java \
  src/main/java/com/example/vongtay/config/LocalImageConfig.java \
  "$T/ShowroomSmoke.java"
java -cp "$T/out" ShowroomSmoke
