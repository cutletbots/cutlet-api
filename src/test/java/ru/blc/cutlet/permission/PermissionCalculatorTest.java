package ru.blc.cutlet.permission;

import org.junit.Assert;
import org.junit.Test;
import ru.blc.cutlet.api.permission.PermissionCalculator;

public class PermissionCalculatorTest {


    @Test
    public void isPermissionAllows() {
        Assert.assertTrue("failed testing permission system", PermissionCalculator.isPermissionAllows("", ""));
        Assert.assertTrue("failed testing permission system",PermissionCalculator.isPermissionAllows("example.test", "Example.tEst"));
        Assert.assertTrue("failed testing permission system",PermissionCalculator.isPermissionAllows("example.*", "example.test"));
        Assert.assertTrue("failed testing permission system",PermissionCalculator.isPermissionAllows("example.*", "example.test.permission"));
        Assert.assertFalse("failed testing permission system",PermissionCalculator.isPermissionAllows("example.*", "example"));
        Assert.assertTrue("failed testing permission system",PermissionCalculator.isPermissionAllows("example.test", "example.*"));
        Assert.assertTrue("failed testing permission system",PermissionCalculator.isPermissionAllows("example.*.permission", "example.test.permission"));
        Assert.assertTrue("failed testing permission system",PermissionCalculator.isPermissionAllows("example.test.permission", "example.*.permission"));
        Assert.assertTrue("failed testing permission system",PermissionCalculator.isPermissionAllows("*", "permission.control"));
        Assert.assertFalse("failed testing permission system",PermissionCalculator.isPermissionAllows("example.test.permission", "example.*"));
    }

    @Test
    public void setCalculator() {
        PermissionCalculator.setCalculator((s1, s2)->true);
        Assert.assertTrue(PermissionCalculator.isPermissionAllows("p1", "p2"));
        PermissionCalculator.setCalculator(null);
    }
}
