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

        assertDeepCopyMatches(one, two);
    }

    @Test
    public void an_object_cannot_be_a_deep_copy_of_itself()
    {
        final ExampleOne one = new ExampleOne(4563L);

        assertDeepCopyFailure(
            one,
            one,
            "the same instance cannot be a deep copy of itself");
    }

    @Test
    public void two_instances_with_fields_that_differ_cannot_be_deep_copies_of_each_other()
    {
        final ExampleOne one = new ExampleOne(4563L);
        final ExampleOne two = new ExampleOne(4564L);

        assertDeepCopyFailure(
            one,
            two,
            "root->firstField 4563 != 4564");
    }

    private void assertDeepCopyFailure(
        Object one,
        Object two,
        String message)
    {
        DeepCopyMatchResult result = new DeepCopyAssertion().matches(one, two);
        assertEquals(message, result.failureDescription);
        assertFalse(result.isDeepCopy);
    }

    private void assertDeepCopyMatches(Object one, Object two)
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
                if (one == two)
                {
                    return DeepCopyMatchResult.failure("the same instance cannot be a deep copy of itself");
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
                                    continue;
                                }
                            default:
                                throw new UnsupportedOperationException();
                        }
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
            final StringBuilder builder = new StringBuilder();
            for (String piece : fieldPath)
            {
                builder.append(piece);
                builder.append("->");
            }
            builder.delete(builder.length() - 2, builder.length());

            String fieldPath = builder.toString();

            return DeepCopyMatchResult.failure(
                fieldPath + " " + first + " != " + second
            );
        }
    }
}
