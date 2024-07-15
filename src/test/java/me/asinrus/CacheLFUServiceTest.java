package me.asinrus;

import org.jetbrains.kotlinx.lincheck.LinChecker;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class CacheLFUServiceTest {
    CacheLFUService cacheService = new CacheLFUService(10);

    @Operation
    public void put(Integer key, Integer value) {
        cacheService.put(key.toString(), value);
    }

    @Operation
    public Object get(Integer key) {
        return cacheService.get(key.toString());
    }


    @Test
    public void modelCheckingTest2threads() {
        ModelCheckingOptions options = new ModelCheckingOptions()
                .actorsBefore(2)
                .threads(2);

        LinChecker.check(this.getClass(), options);
    }

    @Test
    public void modelCheckingTest3threads() {
        ModelCheckingOptions options = new ModelCheckingOptions()
                .actorsBefore(2)
                .threads(3);

        assertThrows(Exception.class,
                () -> LinChecker.check(this.getClass(), options)) ;
    }
}