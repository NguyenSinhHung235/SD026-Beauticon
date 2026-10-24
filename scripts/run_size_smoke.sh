#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
T=$(mktemp -d)
trap 'rm -rf "$T"' EXIT
cat > "$T/SizeSmoke.java" <<'SRC'
import com.example.vongtay.shop.SizeSelection;
import java.math.BigDecimal;
public class SizeSmoke {
 static int checks;
 static void check(boolean test) { if(!test) throw new AssertionError("Size rule failed"); checks++; }
 static void fails(Runnable r) {try{r.run();throw new AssertionError("Expected validation error");}catch(IllegalArgumentException expected){checks++;}}
 public static void main(String[] args) {
  check(SizeSelection.modeFor("16 cm").equals("CO_SAN"));
  check(SizeSelection.modeFor("Freesize").equals("FREESIZE"));
  check(SizeSelection.modeFor("Theo yêu cầu").equals("THEO_YEU_CAU"));
  var c=SizeSelection.validate("Theo yêu cầu","THEO_YEU_CAU","16,5","VUA","đeo thoải mái",null,null);
  check(c.wristCm().compareTo(new BigDecimal("16.5"))==0);
  check(c.description("x").contains("16.5 cm"));
  fails(()->SizeSelection.validate("Theo yêu cầu","THEO_YEU_CAU","0","VUA","",null,null));
  fails(()->SizeSelection.validate("Theo yêu cầu","THEO_YEU_CAU","31","VUA","",null,null));
  fails(()->SizeSelection.validate("Theo yêu cầu","THEO_YEU_CAU","16.51","VUA","",null,null));
  fails(()->SizeSelection.validate("Theo yêu cầu","THEO_YEU_CAU","16.5","INVALID","",null,null));
  fails(()->SizeSelection.validate("16 cm","THEO_YEU_CAU","16.5","VUA","",null,null));
  fails(()->SizeSelection.validate("Freesize","FREESIZE","","","",null,null));
  check(SizeSelection.validate("Freesize","FREESIZE","","","",new BigDecimal("14.0"),new BigDecimal("20.0")).mode().equals("FREESIZE"));
  check(SizeSelection.validate("16 cm","CO_SAN","16.5","VUA","",null,null).wristCm()==null);
  System.out.println("PASS: "+checks+" GD9 sizing and validation checks (pure Java).");
 }
}
SRC
javac --release 17 -encoding UTF-8 -d "$T" \
  src/main/java/com/example/vongtay/shop/SizeSelection.java "$T/SizeSmoke.java"
java -cp "$T" SizeSmoke
