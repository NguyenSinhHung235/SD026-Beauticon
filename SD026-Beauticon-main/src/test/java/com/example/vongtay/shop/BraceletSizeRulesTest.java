package com.example.vongtay.shop;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BraceletSizeRulesTest {
    @Test void modeAlwaysDerivedFromDatabaseVariantName() {
        assertEquals(SizeSelection.FIXED,SizeSelection.modeFor("16 cm"));
        assertEquals(SizeSelection.FREE,SizeSelection.modeFor(" Freesize "));
        assertEquals(SizeSelection.CUSTOM,SizeSelection.modeFor("Theo yêu cầu"));
    }
    @Test void customMeasurementHasOneDecimalAndFit() {
        SizeSelection.Choice choice=SizeSelection.validate("Theo yêu cầu","THEO_YEU_CAU",
                "16,5","VUA","Đeo vừa",null,null);
        assertEquals(new BigDecimal("16.5"),choice.wristCm());
        assertEquals("VUA",choice.fit());
        assertTrue(choice.description("Theo yêu cầu").contains("16.5 cm"));
        assertThrows(IllegalArgumentException.class,()->SizeSelection.validate("Theo yêu cầu","THEO_YEU_CAU",
                "16.55","VUA","",null,null));
        assertThrows(IllegalArgumentException.class,()->SizeSelection.validate("Theo yêu cầu","THEO_YEU_CAU",
                "9","VUA","",null,null));
        assertThrows(IllegalArgumentException.class,()->SizeSelection.validate("Theo yêu cầu","THEO_YEU_CAU",
                "16.5","SAI","",null,null));
    }
    @Test void freesizeNeedsRealAdjustmentRange() {
        assertThrows(IllegalArgumentException.class,()->SizeSelection.validate("Freesize","FREESIZE",
                "","","",null,null));
        assertEquals(SizeSelection.FREE,SizeSelection.validate("Freesize","FREESIZE",
                "","","",new BigDecimal("14.0"),new BigDecimal("20.0")).mode());
    }
    @Test void cannotPretendNormalVariantIsCustom() {
        assertThrows(IllegalArgumentException.class,()->SizeSelection.validate("16 cm","THEO_YEU_CAU",
                "16.5","VUA","",null,null));
    }
}
