package net.digihippo.reflect;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.Assert.*;

public class ReflectiveDeepCopyTest
{
    private static final class ExampleOne
    {
        private final long firstField;

        private ExampleOne(long firstFieldValue)
        {
            firstField = firstFieldValue;
        }

        @Override
        public String toString()
        {
            return "ExampleOne(" + firstField + ")";
        }
    }

    @Test
    public void two_identical_objects_but_different_references_are_a_deep_copy_success()
    {
        final ExampleOne one = new ExampleOne(4563L);
        final ExampleOne two = new ExampleOne(4563L);

        assertDeepCopySuccess(one, two);
    }

    @Test
    public void an_object_cannot_be_a_deep_copy_of_itself()
    {
        final ExampleOne one = new ExampleOne(4563L);

        assertDeepCopyFailure(
            one,
            one,
            "root: The same instance cannot be a deep copy of itself");
    }

    @Test
    public void two_instances_with_fields_that_differ_cannot_be_deep_copies_of_each_other()
    {
        final ExampleOne one = new ExampleOne(4563L);
        final ExampleOne two = new ExampleOne(4564L);

        assertDeepCopyFailure(
            one,
            two,
            "root->firstField: 4563 != 4564");
    }

    @SuppressWarnings("unused")
    private static final class ExampleTwo
    {
        private final long firstField;
        private final long secondField;

        private ExampleTwo(long firstField, long secondField)
        {
            this.firstField = firstField;
            this.secondField = secondField;
        }
    }

    @Test
    public void two_instances_with_one_field_that_matches_and_another_that_does_not_cannot_be_deep_copies_of_each_other()
    {
        final ExampleTwo one = new ExampleTwo(34643L, 344L);
        final ExampleTwo two = new ExampleTwo(34643L, 346L);
        assertDeepCopyFailure(
            one,
            two,
            "root->secondField: 344 != 346");
    }

    @Test
    public void the_first_non_matching_field_is_enough_to_stop()
    {
        final ExampleTwo one = new ExampleTwo(34643L, 344L);
        final ExampleTwo two = new ExampleTwo(3461L, 346L);
        assertDeepCopyFailure(
            one,
            two,
            "root->firstField: 34643 != 3461");
    }

    @Test
    public void two_matching_fields()
    {
        final ExampleTwo one = new ExampleTwo(34643L, 344L);
        final ExampleTwo two = new ExampleTwo(34643L, 344L);
        assertDeepCopySuccess(
            one,
            two);
    }

    @SuppressWarnings("unused")
    private static final class ExampleThree
    {
        private final String firstField;

        private ExampleThree(String firstField)
        {
            this.firstField = firstField;
        }
    }

    @Test
    public void a_single_object_field_is_a_success_if_it_is_the_same_instance_of_a_string()
    {
        final String value = "foo";
        ExampleThree one = new ExampleThree(value);
        ExampleThree two = new ExampleThree(value);

        assertDeepCopySuccess(
            one, two);
    }

    @Test
    public void a_single_object_field_is_a_failure_if_that_string_field_differs()
    {
        ExampleThree one = new ExampleThree("foo");
        ExampleThree two = new ExampleThree("bar");

        assertDeepCopyFailure(
            one, two, "root->firstField: foo != bar");
    }

    @SuppressWarnings("unused")
    private static final class ExampleFour
    {
        private final ExampleOne exampleOne;

        private ExampleFour(ExampleOne exampleOne)
        {
            this.exampleOne = exampleOne;
        }
    }

    @Test
    public void same_nested_instance_is_a_deep_copy_failure()
    {
        ExampleOne exampleOne = new ExampleOne(25535345L);

        ExampleFour one = new ExampleFour(exampleOne);
        ExampleFour two = new ExampleFour(exampleOne);

        assertDeepCopyFailure(
            one, two,
            "root->exampleOne: The same instance cannot be a deep copy of itself");
    }

    @Test
    public void nested_deep_copy_is_deep_copy_success()
    {
        ExampleFour one = new ExampleFour(new ExampleOne(25535345L));
        ExampleFour two = new ExampleFour(new ExampleOne(25535345L));

        assertDeepCopySuccess(one, two);
    }

    @Test
    public void objects_of_different_class_cannot_be_deep_copies_of_each_other()
    {
        assertDeepCopyFailure(
            "foo",
            new ExampleOne(4535L),
            "root: objects are not the same type " +
                "(java.lang.String versus net.digihippo.reflect.ReflectiveDeepCopyTest$ExampleOne)"
        );
    }

    @Test
    public void map_values_of_the_same_instance_means_deep_copy_failure()
    {
        final Map<String, ExampleOne> one = new HashMap<>();
        final Map<String, ExampleOne> two = new HashMap<>();

        final ExampleOne value = new ExampleOne(24232L);
        one.put("one", value);
        two.put("one", value);

        assertDeepCopyFailure(
            one, two, "root->get(one): The same instance cannot be a deep copy of itself");
    }

    @Test
    public void map_values_that_are_deep_copies_means_deep_copy_success()
    {
        final Map<String, ExampleOne> one = new HashMap<>();
        final Map<String, ExampleOne> two = new HashMap<>();

        one.put("one", new ExampleOne(24232L));
        two.put("one", new ExampleOne(24232L));

        assertDeepCopySuccess(one, two);
    }

    @Test
    public void maps_with_different_key_value_pairs_are_deep_copy_failures()
    {
        final Map<String, ExampleOne> one = new HashMap<>();
        final Map<String, ExampleOne> two = new HashMap<>();

        one.put("one", new ExampleOne(24232L));
        two.put("two", new ExampleOne(24232L));

        assertDeepCopyFailure(
            one, two,
            "root->get(one): ExampleOne(24232) != null");
    }

    @Test
    public void maps_with_different_value_at_same_key_are_deep_copy_failures()
    {
        final Map<String, ExampleOne> one = new HashMap<>();
        final Map<String, ExampleOne> two = new HashMap<>();

        one.put("one", new ExampleOne(24232L));
        two.put("one", new ExampleOne(3636L));

        assertDeepCopyFailure(
            one, two,
            "root->get(one)->firstField: 24232 != 3636");
    }

    @Test
    public void maps_that_have_extra_entries_are_deep_copy_failures()
    {
        final Map<String, ExampleOne> one = new HashMap<>();
        final Map<String, ExampleOne> two = new HashMap<>();

        one.put("one", new ExampleOne(24232L));
        two.put("one", new ExampleOne(24232L));
        two.put("two", new ExampleOne(24232L));

        assertDeepCopyFailure(
            one, two,
            "root->get(two): null != ExampleOne(24232)");
    }

    @Test
    public void list_values_that_contain_same_instance_at_same_index_are_deep_copy_failures()
    {
        final List<ExampleOne> one = new ArrayList<>();
        final List<ExampleOne> two = new ArrayList<>();

        ExampleOne instance = new ExampleOne(24232L);
        one.add(instance);
        two.add(instance);

        assertDeepCopyFailure(
            one, two,
            "root->at(0): The same instance cannot be a deep copy of itself");
    }

    @Test
    public void list_values_that_contain_same_value_but_different_instance_at_same_index_are_deep_copy_successes()
    {
        final List<ExampleOne> one = new ArrayList<>();
        final List<ExampleOne> two = new ArrayList<>();

        one.add(new ExampleOne(24232L));
        two.add(new ExampleOne(24232L));

        assertDeepCopySuccess(one, two);
    }

    @Test
    public void list_values_that_contain_different_value_at_same_index_are_deep_copy_successes()
    {
        final List<ExampleOne> one = new ArrayList<>();
        final List<ExampleOne> two = new ArrayList<>();

        one.add(new ExampleOne(24232L));
        two.add(new ExampleOne(34L));

        assertDeepCopyFailure(
            one, two,
            "root->at(0)->firstField: 24232 != 34");
    }

    @Test
    public void list_values_of_different_sizes_are_not_deep_copy_successes()
    {
        final List<ExampleOne> one = new ArrayList<>();
        final List<ExampleOne> two = new ArrayList<>();

        one.add(new ExampleOne(24232L));
        two.add(new ExampleOne(24232L));
        two.add(new ExampleOne(2455L));

        assertDeepCopyFailure(
            one, two,
            "root->at(1): null != ExampleOne(2455)");

        assertDeepCopyFailure(
            two, one,
            "root->at(1): ExampleOne(2455) != null");
    }

    private void assertDeepCopyFailure(
        Object one,
        Object two,
        String message)
    {
        DeepCopyMatchResult result = new DeepCopyAssertion().matches(one, two);
        assertFalse(result.isDeepCopy);
        assertEquals(message, result.failureDescription);
    }

    private void assertDeepCopySuccess(Object one, Object two)
    {
        assertTrue(new DeepCopyAssertion().matches(one, two).isDeepCopy);
    }

    private static class DeepCopyMatchResult
    {
        private final boolean isDeepCopy;
        private final String failureDescription;

        private DeepCopyMatchResult(boolean isDeepCopy, String failureDescription)
        {
            this.isDeepCopy = isDeepCopy;
            this.failureDescription = failureDescription;
        }

        public static DeepCopyMatchResult failure(String failureMessage)
        {
            return new DeepCopyMatchResult(false, failureMessage);
        }

        public static DeepCopyMatchResult success()
        {
            return new DeepCopyMatchResult(true, null);
        }
    }

    private class DeepCopyAssertion
    {
        private final Stack<String> fieldPath = new Stack<>();

        DeepCopyAssertion()
        {
            fieldPath.push("root");
        }

        public DeepCopyMatchResult matches(Object one, Object two)
        {
            try
            {
                if (one == null && two == null)
                {
                    return DeepCopyMatchResult.success();
                }

                if (one == null || two == null)
                {
                    return fail("" + one + " != " + two);
                }

                if (one == two)
                {
                    return fail("The same instance cannot be a deep copy of itself");
                }

                if (!one.getClass().equals(two.getClass()))
                {
                    return fail(
                        "objects are not the same type ("+ one.getClass().getName() +
                        " versus " + two.getClass().getName() + ")");
                }

                if (isValueType(one))
                {
                    return performValueTypeMatch(one, two);
                }

                if (one instanceof Map)
                {
                    return performMapTypeMatch(one, two);
                }

                if (one instanceof List)
                {
                    return performListTypeMatch(one, two);
                }

                final Field[] fields = one.getClass().getDeclaredFields();
                for (Field field : fields)
                {
                    if (field.isSynthetic() || Modifier.isStatic(field.getModifiers()))
                    {
                        continue;
                    }

                    fieldPath.push(field.getName());
                    field.setAccessible(true);

                    if (field.getType().isPrimitive())
                    {
                        switch (field.getType().getName())
                        {
                            case "long":
                                long first = field.getLong(one);
                                long second = field.getLong(two);
                                if (first != second)
                                {
                                    return unequalField(first, second);
                                }
                                break;
                        }
                    }
                    else
                    {
                        return matches(field.get(one), field.get(two));
                    }

                    fieldPath.pop();
                }

                return DeepCopyMatchResult.success();
            }
            catch (IllegalAccessException e)
            {
                return DeepCopyMatchResult.failure(e.getMessage());
            }
        }

        private DeepCopyMatchResult performListTypeMatch(Object one, Object two)
        {
            final List listOne = (List) one;
            final List listTwo = (List) two;

            int index = 0;
            Iterator iterator = listTwo.iterator();
            for (Object fromListOne : listOne)
            {
                fieldPath.push("at(" + index + ")");

                final Object fromListTwo = iterator.hasNext() ? iterator.next() : null;

                final DeepCopyMatchResult match = matches(fromListOne, fromListTwo);

                if (!match.isDeepCopy)
                {
                    return match;
                }

                fieldPath.pop();
                ++index;
            }

            iterator = listOne.iterator();
            index = 0;
            for (Object fromListTwo : listTwo)
            {
                fieldPath.push("at(" + index + ")");

                final Object fromListOne = iterator.hasNext() ? iterator.next() : null;

                final DeepCopyMatchResult match = matches(fromListOne, fromListTwo);

                if (!match.isDeepCopy)
                {
                    return match;
                }

                fieldPath.pop();
                ++index;
            }

            return DeepCopyMatchResult.success();
        }

        private DeepCopyMatchResult performMapTypeMatch(Object one, Object two)
        {
            final Map mapOne = (Map) one;
            final Map mapTwo = (Map) two;
            for (Object o : mapOne.entrySet())
            {
                final Object key = ((Map.Entry) o).getKey();
                fieldPath.push("get(" + key.toString() + ")");

                final DeepCopyMatchResult result = matches(mapOne.get(key), mapTwo.get(key));
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }

            for (Object o : mapTwo.entrySet())
            {
                final Object key = ((Map.Entry) o).getKey();
                fieldPath.push("get(" + key.toString() + ")");

                final DeepCopyMatchResult result = matches(mapOne.get(key), mapTwo.get(key));
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }

            return DeepCopyMatchResult.success();
        }

        private DeepCopyMatchResult performValueTypeMatch(Object one, Object two)
        {
            if (!one.equals(two))
            {
                return unequalField(one, two);
            }
            else
            {
                return DeepCopyMatchResult.success();
            }
        }

        private boolean isValueType(Object one)
        {
            return one.getClass().equals(String.class);
        }

        private DeepCopyMatchResult unequalField(Object first, Object second)
        {
            return fail(first + " != " + second);
        }

        private DeepCopyMatchResult fail(String message)
        {
            final StringBuilder builder = new StringBuilder();
            for (String piece : fieldPath)
            {
                builder.append(piece);
                builder.append("->");
            }
            builder.delete(builder.length() - 2, builder.length());

            String fieldPath = builder.toString();

            return DeepCopyMatchResult.failure(
                fieldPath + ": " + message
            );
        }
    }
}
