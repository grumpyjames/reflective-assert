package net.digihippo.reflect;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReflectiveDeepCopyTest
{
    private static final class ExampleOne
    {
        private final long firstField;

        private ExampleOne(long firstFieldValue)
        {
            firstField = firstFieldValue;
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
                if (!one.getClass().equals(two.getClass()))
                {
                    return fail(
                        "objects are not the same type ("+ one.getClass().getName() +
                        " versus " + two.getClass().getName() + ")");
                }

                if (one.getClass().equals(String.class))
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

                if (one == two)
                {
                    return fail("The same instance cannot be a deep copy of itself");
                }

                Field[] fields = one.getClass().getDeclaredFields();
                for (Field field : fields)
                {
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
                                else
                                {
                                    break;
                                }
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
