package com.healthsport.controller;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InternalControllerTest {

    @Test
    void allowsLocalhostAddressesOnly() throws Exception {
        Method method = InternalController.class.getDeclaredMethod("assertLocalRequest", String.class);
        method.setAccessible(true);

        assertDoesNotThrow(() -> method.invoke(null, "127.0.0.1"));
        assertDoesNotThrow(() -> method.invoke(null, "0:0:0:0:0:0:0:1"));
        assertDoesNotThrow(() -> method.invoke(null, "::1"));
    }

    @Test
    void rejectsNonLocalAddresses() throws Exception {
        Method method = InternalController.class.getDeclaredMethod("assertLocalRequest", String.class);
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> method.invoke(null, "192.168.1.9"));

        assertThrows(SecurityException.class, () -> {
            throw exception.getTargetException();
        });
    }
}
