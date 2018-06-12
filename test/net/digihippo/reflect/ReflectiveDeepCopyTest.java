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

    @Test
    public void primitives_of_same_value_are_deep_copies()
    {
        assertDeepCopySuccess(1L, 1L);
        assertDeepCopySuccess(1, 1);
        assertDeepCopySuccess((byte)1, (byte)1);
        assertDeepCopySuccess(1D, 1D);
        assertDeepCopySuccess(1F, 1F);
        assertDeepCopySuccess(true, true);
    }

    @Test
    public void same_array_of_primitives_is_deep_copy_failure()
    {
        long[] one = {13L, 5L};
        assertDeepCopyFailure(one, one, "root: The same instance cannot be a deep copy of itself");
    }

    @Test
    public void two_arrays_of_same_primitives_is_deep_copy_success()
    {
        assertDeepCopySuccess(new long[]{13L, 5L}, new long[]{13L, 5L});
        assertDeepCopySuccess(new int[]{13, 5}, new int[]{13, 5});
        assertDeepCopySuccess(new float[]{13F, 5F}, new float[]{13F, 5F});
        assertDeepCopySuccess(new double[]{13D, 5D}, new double[]{13D, 5D});
        assertDeepCopySuccess(new byte[]{(byte)13, (byte)5}, new byte[]{(byte)13, (byte)5});
        assertDeepCopySuccess(new boolean[]{true, false}, new boolean[]{true, false});
    }

    @Test
    public void two_arrays_of_different_longs_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new long[]{13L, 5L}, new long[]{13L, 6L},
            "root->[1]: 5 != 6");

        assertDeepCopyFailure(
            new long[]{13L, 5L}, new long[]{13L},
            "root->[1]: 5 != null");

        assertDeepCopyFailure(
            new long[]{13L}, new long[]{13L, 5L},
            "root->[1]: null != 5");
    }

    @Test
    public void two_arrays_of_different_ints_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new int[]{13, 5}, new int[]{13, 6},
            "root->[1]: 5 != 6");

        assertDeepCopyFailure(
            new int[]{13, 5}, new int[]{13},
            "root->[1]: 5 != null");

        assertDeepCopyFailure(
            new int[]{13}, new int[]{13, 5},
            "root->[1]: null != 5");
    }

    @Test
    public void two_arrays_of_different_bytes_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new byte[]{13, 5}, new byte[]{13, 6},
            "root->[1]: 5 != 6");

        assertDeepCopyFailure(
            new byte[]{13, 5}, new byte[]{13},
            "root->[1]: 5 != null");

        assertDeepCopyFailure(
            new byte[]{13}, new byte[]{13, 5},
            "root->[1]: null != 5");
    }

    @Test
    public void two_arrays_of_different_floats_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new float[]{13, 5}, new float[]{13, 6},
            "root->[1]: 5.0 != 6.0");

        assertDeepCopyFailure(
            new float[]{13, 5}, new float[]{13},
            "root->[1]: 5.0 != null");

        assertDeepCopyFailure(
            new float[]{13}, new float[]{13, 5},
            "root->[1]: null != 5.0");
    }

    @Test
    public void two_arrays_of_different_doubles_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new double[]{13, 5}, new double[]{13, 6},
            "root->[1]: 5.0 != 6.0");

        assertDeepCopyFailure(
            new double[]{13, 5}, new double[]{13},
            "root->[1]: 5.0 != null");

        assertDeepCopyFailure(
            new double[]{13}, new double[]{13, 5},
            "root->[1]: null != 5.0");
    }

    @Test
    public void two_arrays_of_different_booleans_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new boolean[]{true, false}, new boolean[]{true, true},
            "root->[1]: false != true");

        assertDeepCopyFailure(
            new boolean[]{true, false}, new boolean[]{true},
            "root->[1]: false != null");

        assertDeepCopyFailure(
            new boolean[]{true}, new boolean[]{true, false},
            "root->[1]: null != false");
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
        DeepCopyMatchResult matches = new DeepCopyAssertion().matches(one, two);
        assertTrue(matches.failureDescription, matches.isDeepCopy);
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

                if (one == two)
                {
                    return fail("The same instance cannot be a deep copy of itself");
                }

                if (one.getClass().isArray())
                {
                    return performArrayMatch(one, two);
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

                    DeepCopyMatchResult result = matches(field.get(one), field.get(two));
                    if (!result.isDeepCopy)
                    {
                        return result;
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

        private DeepCopyMatchResult performArrayMatch(Object one, Object two)
        {
            Class<?> componentType = one.getClass().getComponentType();
            if (componentType.isPrimitive())
            {
                if (componentType == long.class)
                {
                    return runLongArrayCheck((long[]) one, (long[]) two);
                }
                else if (componentType == int.class)
                {
                    return runIntArrayCheck((int[]) one, (int[]) two);
                }
                else if (componentType == double.class)
                {
                    return runDoubleArrayCheck((double[]) one, (double[]) two);
                }
                else if (componentType == float.class)
                {
                    return runFloatArrayCheck((float[]) one, (float[]) two);
                }
                else if (componentType == boolean.class)
                {
                    return runBooleanArrayCheck((boolean[]) one, (boolean[]) two);
                }
                else if (componentType == byte.class)
                {
                    return runByteArrayCheck((byte[]) one, (byte[]) two);
                }
                throw new UnsupportedOperationException("I have no idea what " + componentType + " is.");
            }
            else
            {
                final Object[] arrayOne = (Object[]) one;
                final Object[] arrayTwo = (Object[]) two;
                for (int i = 0; i < arrayOne.length; i++)
                {
                    fieldPath.push("[" + i + "]");

                    final DeepCopyMatchResult result =
                        matches(arrayOne[i], i < arrayTwo.length ? arrayTwo[i] : null);
                    if (!result.isDeepCopy)
                    {
                        return result;
                    }

                    fieldPath.pop();
                }
                for (int i = 0; i < arrayTwo.length; i++)
                {
                    fieldPath.push("[" + i + "]");

                    final DeepCopyMatchResult result =
                        matches(i < arrayOne.length ? arrayOne[i] : null, arrayTwo[i]);
                    if (!result.isDeepCopy)
                    {
                        return result;
                    }

                    fieldPath.pop();
                }
            }

            return DeepCopyMatchResult.success();
        }

        private DeepCopyMatchResult runLongArrayCheck(long[] one, long[] two)
        {
            for (int i = 0; i < one.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(one[i], i < two.length ? two[i] : null);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            for (int i = 0; i < two.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(i < one.length ? one[i] : null, two[i]);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            return DeepCopyMatchResult.success();
        }

        private DeepCopyMatchResult runIntArrayCheck(int[] one, int[] two)
        {
            for (int i = 0; i < one.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(one[i], i < two.length ? two[i] : null);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            for (int i = 0; i < two.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(i < one.length ? one[i] : null, two[i]);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            return DeepCopyMatchResult.success();
        }

        private DeepCopyMatchResult runByteArrayCheck(byte[] one, byte[] two)
        {
            for (int i = 0; i < one.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(one[i], i < two.length ? two[i] : null);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            for (int i = 0; i < two.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(i < one.length ? one[i] : null, two[i]);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            return DeepCopyMatchResult.success();
        }

        private DeepCopyMatchResult runFloatArrayCheck(float[] one, float[] two)
        {
            for (int i = 0; i < one.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(one[i], i < two.length ? two[i] : null);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            for (int i = 0; i < two.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(i < one.length ? one[i] : null, two[i]);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            return DeepCopyMatchResult.success();
        }

        private DeepCopyMatchResult runDoubleArrayCheck(double[] one, double[] two)
        {
            for (int i = 0; i < one.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(one[i], i < two.length ? two[i] : null);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            for (int i = 0; i < two.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(i < one.length ? one[i] : null, two[i]);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            return DeepCopyMatchResult.success();
        }

        private DeepCopyMatchResult runBooleanArrayCheck(boolean[] one, boolean[] two)
        {
            for (int i = 0; i < one.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(one[i], i < two.length ? two[i] : null);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            for (int i = 0; i < two.length; i++)
            {
                fieldPath.push("[" + i + "]");

                final DeepCopyMatchResult result =
                    matches(i < one.length ? one[i] : null, two[i]);
                if (!result.isDeepCopy)
                {
                    return result;
                }

                fieldPath.pop();
            }
            return DeepCopyMatchResult.success();
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

        private final Set<Class<?>> valueTypes = new HashSet<>();
        {
            valueTypes.add(String.class);
            valueTypes.add(Long.class);
            valueTypes.add(Integer.class);
            valueTypes.add(Float.class);
            valueTypes.add(Double.class);
            valueTypes.add(Boolean.class);
            valueTypes.add(Byte.class);
        }

        private boolean isValueType(Object one)
        {
            return valueTypes.contains(one.getClass());
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
