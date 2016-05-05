/**
 * Copyright © 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.jsonschema2pojo.integration;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class ExtendsIT {
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithEmbeddedSchemaGeneratesParentType() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/extendsEmbeddedSchema.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.ExtendsEmbeddedSchema");
        Class supertype = resultsClassLoader.loadClass("com.example.ExtendsEmbeddedSchemaParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithRefToAnotherSchema() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfA.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfAParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithRefToAnotherSchemaThatIsAlreadyASubtype() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfAParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test(expected = ClassNotFoundException.class)
    public void extendsStringCausesNoNewTypeToBeGenerated() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/extendsString.json", "com.example");
        resultsClassLoader.loadClass("com.example.ExtendsString");

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsEquals() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example2");
        
        Class generatedType = resultsClassLoader.loadClass("com.example2.SubtypeOfSubtypeOfA");
        Object instance = generatedType.newInstance();
        Object instance2 = generatedType.newInstance();

        new PropertyDescriptor("parent", generatedType).getWriteMethod().invoke(instance, "1");
        new PropertyDescriptor("child", generatedType).getWriteMethod().invoke(instance, "2");        
        
        new PropertyDescriptor("parent", generatedType).getWriteMethod().invoke(instance2, "not-equal");
        new PropertyDescriptor("child", generatedType).getWriteMethod().invoke(instance2, "2");
        
        assertFalse(instance.equals(instance2));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsSchemaWithinDefinitions() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/extendsSchemaWithinDefinitions.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.Child");
        assertNotNull("no propertyOfChild field", subtype.getDeclaredField("propertyOfChild"));

        Class supertype = resultsClassLoader.loadClass("com.example.ChildParent");
        assertNotNull("no propertyOfParent field", supertype.getDeclaredField("propertyOfParent"));

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void constructorHasParentsProperties() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfB.json", "com.example", config("includeConstructors", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfB");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfBParent");

        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        assertNotNull("Parent constructor is missing", supertype.getConstructor(String.class));
        assertNotNull("Constructor is missing", type.getConstructor(String.class, String.class));

        Object typeInstance = type.getConstructor(String.class, String.class).newInstance("String1", "String2");

        Field chieldField = type.getDeclaredField("childProperty");
        chieldField.setAccessible(true);
        String childProp = (String)chieldField.get(typeInstance);
        Field parentField = supertype.getDeclaredField("parentProperty");
        parentField.setAccessible(true);
        String parentProp = (String)parentField.get(typeInstance);

        assertThat(childProp, is(equalTo("String1")));
        assertThat(parentProp, is(equalTo("String2")));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void constructorHasParentsParentProperties() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfB.json", "com.example", config("includeConstructors", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfB");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBParent");
        Class superSupertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBParentParent");

        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        assertNotNull("Parent Parent constructor is missing", superSupertype.getDeclaredConstructor(String.class));
        assertNotNull("Parent Constructor is missing", supertype.getDeclaredConstructor(String.class, String.class));
        assertNotNull("Constructor is missing", type.getDeclaredConstructor(String.class, String.class, String.class));

        Object typeInstance = type.getConstructor(String.class, String.class, String.class).newInstance("String1", "String2", "String3");

        Field chieldChildField = type.getDeclaredField("childChildProperty");
        chieldChildField.setAccessible(true);
        String childChildProp = (String)chieldChildField.get(typeInstance);
        Field chieldField = supertype.getDeclaredField("childProperty");
        chieldField.setAccessible(true);
        String childProp = (String)chieldField.get(typeInstance);
        Field parentField = superSupertype.getDeclaredField("parentProperty");
        parentField.setAccessible(true);
        String parentProp = (String)parentField.get(typeInstance);

        assertThat(childChildProp, is(equalTo("String1")));
        assertThat(childProp, is(equalTo("String2")));
        assertThat(parentProp, is(equalTo("String3")));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void constructorHasParentsParentPropertiesInCorrectOrder() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfBDifferentType.json", "com.example", config("includeConstructors", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentType");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentTypeParent");
        Class superSupertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentTypeParentParent");

        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        assertNotNull("Parent Parent constructor is missing", superSupertype.getDeclaredConstructor(String.class));
        assertNotNull("Parent Constructor is missing", supertype.getDeclaredConstructor(String.class, String.class));
        assertNotNull("Constructor is missing", type.getDeclaredConstructor(Integer.class, String.class, String.class));

        Object typeInstance = type.getConstructor(Integer.class, String.class, String.class).newInstance(5, "String2", "String3");

        checkThreeArgInstanceProperties(typeInstance, type, supertype, superSupertype, 5, "String2", "String3");
    }


    @Test
    @SuppressWarnings("rawtypes")
    public void copyConstructorCopiesParentParentsProperties() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfBDifferentType.json", "com.example", config("includeConstructors", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentType");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentTypeParent");
        Class superSupertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentTypeParentParent");

        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        Object typeInstance = type.getConstructor(Integer.class, String.class, String.class).newInstance(5, "String2", "String3");
        Object typeCopy = type.getConstructor(type).newInstance(typeInstance);

        checkThreeArgInstanceProperties(typeInstance, type, supertype, superSupertype, 5, "String2", "String3");
        checkThreeArgInstanceProperties(typeCopy, type, supertype, superSupertype, 5, "String2", "String3");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void copiedObjectDoesNotMutateCopiedProperties() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfBDifferentType.json", "com.example", config("includeConstructors", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentType");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentTypeParent");
        Class superSupertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentTypeParentParent");

        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        Object typeInstance = type.getConstructor(Integer.class, String.class, String.class).newInstance(5, "String2", "String3");
        Object typeCopy = type.getConstructor(type).newInstance(typeInstance);

        Field chieldChildField = type.getDeclaredField("childChildProperty");
        chieldChildField.setAccessible(true);
        Field chieldField = supertype.getDeclaredField("childProperty");
        chieldField.setAccessible(true);
        Field parentField = superSupertype.getDeclaredField("parentProperty");
        parentField.setAccessible(true);

        chieldChildField.set(typeCopy, 6);
        chieldField.set(typeCopy, "String3");
        parentField.set(typeCopy, "String4");

        checkThreeArgInstanceProperties(typeInstance, type, supertype, superSupertype, 5, "String2", "String3");
        checkThreeArgInstanceProperties(typeCopy, type, supertype, superSupertype, 6, "String3", "String4");
    }

    @SuppressWarnings("rawtypes")
    private static void checkThreeArgInstanceProperties(Object instance, Class type, Class supertype, Class superSupertype, int number, String string1, String string2) throws Exception {
        Field chieldChildField = type.getDeclaredField("childChildProperty");
        chieldChildField.setAccessible(true);
        int childChildProp = (int)chieldChildField.get(instance);
        Field chieldField = supertype.getDeclaredField("childProperty");
        chieldField.setAccessible(true);
        String childProp = (String)chieldField.get(instance);
        Field parentField = superSupertype.getDeclaredField("parentProperty");
        parentField.setAccessible(true);
        String parentProp = (String)parentField.get(instance);

        assertThat(childChildProp, is(equalTo(number)));
        assertThat(childProp, is(equalTo(string1)));
        assertThat(parentProp, is(equalTo(string2)));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void copiedObjectDoesNotMutateCopiedObjects() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/extendsSchemaWithinDefinitions.json", "com.example", config("includeConstructors", true));

        Class containerType = resultsClassLoader.loadClass("com.example.ExtendsSchemaWithinDefinitions");
        Field containerField = containerType.getDeclaredField("child");
        containerField.setAccessible(true);

        Class subtype = resultsClassLoader.loadClass("com.example.Child");
        Field childField = subtype.getDeclaredField("propertyOfChild");
        childField.setAccessible(true);

        Class supertype = resultsClassLoader.loadClass("com.example.ChildParent");
        Field parentField = supertype.getDeclaredField("propertyOfParent");
        parentField.setAccessible(true);

        Object childInstance = subtype.getConstructor(String.class, String.class).newInstance("String1", "String2");

        Object containerInstance = containerType.getConstructor(subtype).newInstance(childInstance);
        Object containerCopy = containerType.getConstructor(containerType).newInstance(containerInstance);

        Object childCopy = containerField.get(containerCopy);

        assertThat(childCopy, is(equalTo(childInstance)));
        assertNotSame(childCopy, childInstance);

        assertEquals(childField.get(childCopy), "String1");
        assertEquals(childField.get(childInstance), "String1");
        assertEquals(parentField.get(childCopy), "String2");
        assertEquals(parentField.get(childInstance), "String2");

        childField.set(childCopy, "String3");
        assertThat(childCopy, is(not(equalTo(childInstance))));
        assertEquals(childField.get(childCopy), "String3");
        assertEquals(childField.get(childInstance), "String1");
        assertEquals(parentField.get(childCopy), "String2");
        assertEquals(parentField.get(childInstance), "String2");

        parentField.set(childInstance, "String4");
        assertThat(childCopy, is(not(equalTo(childInstance))));
        assertEquals(childField.get(childCopy), "String3");
        assertEquals(childField.get(childInstance), "String1");
        assertEquals(parentField.get(childCopy), "String2");
        assertEquals(parentField.get(childInstance), "String4");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsBuilderMethods() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example", config("generateBuilders", true));

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfAParent");

        checkBuilderMethod(subtype, supertype, "withParent");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void builderMethodsOnChildWithProperties() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfB.json", "com.example", config("generateBuilders", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfB");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfBParent");

        checkBuilderMethod(type, supertype, "withParentProperty");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void builderMethodsOnChildWithNoProperties() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfBWithNoProperties.json", "com.example", config("generateBuilders", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfBWithNoProperties");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfBWithNoPropertiesParent");

        checkBuilderMethod(type, supertype, "withParentProperty");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static void checkBuilderMethod(Class type, Class supertype, String builderMethodName) throws Exception {
        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        Method builderMethod = supertype.getDeclaredMethod(builderMethodName, String.class);
        assertNotNull("Builder method not found on super type: " + builderMethodName, builderMethod);
        assertThat(builderMethod.getReturnType(), is(equalTo(supertype)));

        Method builderMethodOverride = type.getDeclaredMethod(builderMethodName, String.class);
        assertNotNull("Builder method not overridden on type: " + builderMethodName, builderMethodOverride);
        assertThat(builderMethodOverride.getReturnType(), is(equalTo(type)));
    }

}
