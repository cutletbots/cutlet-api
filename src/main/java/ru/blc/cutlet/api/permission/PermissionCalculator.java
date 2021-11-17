package ru.blc.cutlet.api.permission;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.blc.cutlet.api.Cutlet;

import java.util.function.BiFunction;

public class PermissionCalculator {

    private static final Logger logger = LoggerFactory.getLogger("Cutlet");

    /**
     * Custom permissions calculator.
     */
    private static BiFunction<String, String, Boolean> calculator = null;

    /**
     * Check if base permission allows specified permission <br>
     * Exluding permissions (with "-" at start) works as common (without "-" at start)<br>
     * Thus this calls are equal:<br>
     * <pre>
     *     isPermissionAllows("permission.example", "permission.example");
     *     isPermissionAllows("-permission.example", "permission.example");
     * </pre>
     * <p>
     * Supports super-permissions (*).<br>
     * <ul>
     *      <li>
     *          Super-permission at base permission allows any checked permissions.<br>
     *          i.e. for base permission <code>permission.base.*</code> would be allowed any permission starts with <code>permission.base.</code>
     *      </li>
     *      <li>
     *          Super-permission at checked permission requires any one permission at base<br>
     *          i.e. checked permission <code>permission.*.example</code> will be allowed with base permission <code>permission.anything.example</code>
     *      </li>
     * </ul>
     *
     * <ul>
     *     <li>
     *         Note that similar permissions where one is super-permission is very different. i.e
     *         <code>permission.example.*</code> and <code>permission.example</code> allows very different cases.<br>
     *         <code>permission.example.*</code> allow any permission starts with <code>permission.example.</code> (<code>permission.example.anything</code>),
     *         but not allows <code>permission.example</code><br>
     *         <code>permission.example</code> allows only itself
     *     </li>
     * </ul>
     *
     * <table style = "table-layout: fixed; width: 100%; border-collapse: collapse; border: 1px solid;">
     *   <caption>Return examples for some passed permissions</caption>
     *   <tr>
     *     <th style = "text-align: center;">base</th>
     *     <th style = "text-align: center;">check</th>
     *     <th style = "text-align: center;">result</th>
     *   </tr>
     *   <tr>
     *      <td>example.test</td> <td>example.test</td> <td>true</td>
     *   </tr>
     *   <tr>
     *      <td>example.*</td> <td>example.test</td> <td>true</td>
     *   </tr>
     *   <tr>
     *      <td>example.*</td> <td>example.test.permission</td> <td>true</td>
     *   </tr>
     *   <tr>
     *      <td>example.*</td> <td>example</td> <td>false</td>
     *   </tr>
     *   <tr>
     *      <td>example.test</td> <td>example.*</td> <td>true</td>
     *   </tr>
     *   <tr>
     *      <td>example.*.permission</td> <td>example.test.permission</td> <td>true</td>
     *   </tr>
     *   <tr>
     *      <td>example.test.permission</td> <td>example.*.permission</td> <td>true</td>
     *   </tr>
     *   <tr>
     *      <td>example.test.permission</td> <td>example.*</td> <td>false</td>
     *   </tr>
     * </table>
     * <br>
     * If anyone passed permission is null result is always false<br>
     * If checked permission is empty result is true<br>
     * <ul>
     *     <li>
     *     This documentation valid only for default permission calculator.
     *     This behavior can be changed by {@link PermissionCalculator#setCalculator(BiFunction)}
     *     </li>
     * </ul>
     *
     * @param base    base permission
     * @param toCheck permission to check. If null returns false, if empty returns true
     * @return true if base permissions allows checked permission, otherwise false
     */
    public static boolean isPermissionAllows(String base, String toCheck) {
        if (getCalculator() != null) {
            try {
                return getCalculator().apply(base, toCheck);
            } catch (Exception ex) {
                logger.error("Failed permission calculating by custom calculator. Calculating by default calculator. Exception was:", ex);
            }
        }
        return basePermissionsCheck(base, toCheck);
    }

    /**
     * Default permission calculator
     *
     * @param base    base permission
     * @param toCheck permission to check.
     * @return true if base permissions allows checked permission, otherwise false
     * @see PermissionCalculator#isPermissionAllows(String, String)
     */
    public static boolean basePermissionsCheck(String base, String toCheck) {
        if (Cutlet.instance() != null)
            Cutlet.instance().getLogger().debug("Calculating permission {} from base {}", toCheck, base);
        if (base == null) return false;
        if (toCheck == null) return false;
        if (toCheck.isEmpty()) return true;
        if (toCheck.equalsIgnoreCase(base)) return true;
        base = base.replaceAll("^-+", "");
        toCheck = toCheck.replaceAll("^-+", "");
        String[] baseS = base.split("\\."), toCheckS = toCheck.split("\\.");
        boolean sectionCompleted = true;
        for (int i = 0; sectionCompleted && i < Math.min(baseS.length, toCheckS.length); i++) {
            String baseSection = baseS[i];
            String checkSection = toCheckS[i];
            sectionCompleted = baseSection.equalsIgnoreCase("*")
                    || checkSection.equalsIgnoreCase("*")
                    || baseSection.equalsIgnoreCase(checkSection);
        }
        if (sectionCompleted && baseS.length != toCheckS.length) {
            return baseS.length < toCheckS.length && baseS[baseS.length - 1].equalsIgnoreCase("*");
        }
        return sectionCompleted;
    }

    /**
     * setups custom permission calculator
     *
     * @param calculator permission calculator. null for default
     */
    public static void setCalculator(@Nullable BiFunction<String, String, Boolean> calculator) {
        if (calculator != null)
            logger.warn("Permissions calculator changed. Permission system can be unstable or works wrong!");
        PermissionCalculator.calculator = calculator;
    }

    /**
     * @return Current permissions calculator. Null if default
     */
    @Nullable
    public static BiFunction<String, String, Boolean> getCalculator() {
        return calculator;
    }
}
