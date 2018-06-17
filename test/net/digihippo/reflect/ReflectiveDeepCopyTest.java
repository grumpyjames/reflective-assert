package net.digihippo.reflect;

import org.junit.Test;

import java.time.LocalDate;
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

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ExampleOne that = (ExampleOne) o;

            return firstField == that.firstField;

        }

        @Override
        public int hashCode()
        {
            return (int) (firstField ^ (firstField >>> 32));
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
            "root->at(1): <absent> != ExampleOne(2455)");

        assertDeepCopyFailure(
            two, one,
            "root->at(1): ExampleOne(2455) != <absent>");
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
    public void two_lists_that_should_not_be_deep_copies_of_each_other()
    {
        final List<String> one = new ArrayList<>();
        one.add(null);
        final List<String> two = new ArrayList<>();
        assertDeepCopyFailure(
            one,
            two,
            "root->at(0): null != <absent>"
        );

        assertDeepCopyFailure(
            two,
            one,
            "root->at(0): <absent> != null"
        );
    }

    @Test
    public void two_arrays_of_different_longs_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new long[]{13L, 5L}, new long[]{13L, 6L},
            "root->[1]: 5 != 6");

        assertDeepCopyFailure(
            new long[]{13L, 5L}, new long[]{13L},
            "root->[1]: 5 != <absent>");

        assertDeepCopyFailure(
            new long[]{13L}, new long[]{13L, 5L},
            "root->[1]: <absent> != 5");
    }

    @Test
    public void two_arrays_of_different_ints_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new int[]{13, 5}, new int[]{13, 6},
            "root->[1]: 5 != 6");

        assertDeepCopyFailure(
            new int[]{13, 5}, new int[]{13},
            "root->[1]: 5 != <absent>");

        assertDeepCopyFailure(
            new int[]{13}, new int[]{13, 5},
            "root->[1]: <absent> != 5");
    }

    @Test
    public void two_arrays_of_different_bytes_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new byte[]{13, 5}, new byte[]{13, 6},
            "root->[1]: 5 != 6");

        assertDeepCopyFailure(
            new byte[]{13, 5}, new byte[]{13},
            "root->[1]: 5 != <absent>");

        assertDeepCopyFailure(
            new byte[]{13}, new byte[]{13, 5},
            "root->[1]: <absent> != 5");
    }

    @Test
    public void two_arrays_of_different_floats_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new float[]{13, 5}, new float[]{13, 6},
            "root->[1]: 5.0 != 6.0");

        assertDeepCopyFailure(
            new float[]{13, 5}, new float[]{13},
            "root->[1]: 5.0 != <absent>");

        assertDeepCopyFailure(
            new float[]{13}, new float[]{13, 5},
            "root->[1]: <absent> != 5.0");
    }

    @Test
    public void two_arrays_of_different_doubles_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new double[]{13, 5}, new double[]{13, 6},
            "root->[1]: 5.0 != 6.0");

        assertDeepCopyFailure(
            new double[]{13, 5}, new double[]{13},
            "root->[1]: 5.0 != <absent>");

        assertDeepCopyFailure(
            new double[]{13}, new double[]{13, 5},
            "root->[1]: <absent> != 5.0");
    }

    @Test
    public void two_arrays_of_different_booleans_is_deep_copy_failure()
    {
        assertDeepCopyFailure(
            new boolean[]{true, false}, new boolean[]{true, true},
            "root->[1]: false != true");

        assertDeepCopyFailure(
            new boolean[]{true, false}, new boolean[]{true},
            "root->[1]: false != <absent>");

        assertDeepCopyFailure(
            new boolean[]{true}, new boolean[]{true, false},
            "root->[1]: <absent> != false");
    }

    @Test
    public void do_not_confuse_null_with_absence_in_object_arrays()
    {
        final String[] one = new String[] {null};
        final String[] two = new String[0];

        assertDeepCopyFailure(one, two, "root->[0]: null != <absent>");
        assertDeepCopyFailure(two, one, "root->[0]: <absent> != null");
    }

    @Test
    public void sets_with_identical_value_content_are_deep_copies()
    {
        final HashSet<Long> one = new HashSet<>();
        final HashSet<Long> two = new HashSet<>();

        one.add(2L);
        one.add(4L);
        two.add(2L);
        two.add(4L);

        assertDeepCopySuccess(one, two);
    }

    @Test
    public void sets_with_identically_valued_content_but_different_references_are_deep_copies()
    {
        final HashSet<ExampleOne> one = new HashSet<>();
        final HashSet<ExampleOne> two = new HashSet<>();

        one.add(new ExampleOne(2L));
        one.add(new ExampleOne(4L));
        two.add(new ExampleOne(2L));
        two.add(new ExampleOne(4L));

        assertDeepCopySuccess(one, two);
    }

    @Test
    public void sets_with_identically_valued_content_and_same_references_are_deep_copies()
    {
        final HashSet<ExampleOne> one = new HashSet<>();
        final HashSet<ExampleOne> two = new HashSet<>();

        ExampleOne same = new ExampleOne(4L);
        one.add(new ExampleOne(2L));
        one.add(same);
        two.add(new ExampleOne(2L));
        two.add(same);

        assertDeepCopyFailure(
            one, two,
            "root->at(1): The same instance cannot be a deep copy of itself");
    }

    private static class ExampleFive
    {
        @SuppressWarnings("unused")
        private final LocalDate localDate;

        private ExampleFive(LocalDate localDate)
        {
            this.localDate = localDate;
        }
    }

    @Test
    public void sometimes_it_will_be_necessary_for_outside_users_to_inform_us_a_type_is_immutable()
    {
        LocalDate date = LocalDate.of(2018, 1, 9);
        ExampleFive one = new ExampleFive(date);
        ExampleFive two = new ExampleFive(date);

        assertDeepCopySuccess(one, two, LocalDate.class);
    }

    private enum ExampleSix
    {
        ONE,
        TWO
    }

    @Test
    public void enums_are_values_too()
    {
        assertDeepCopySuccess(ExampleSix.ONE, ExampleSix.ONE);
    }

    @Test
    public void different_enum_values_are_not_deep_copies()
    {
        assertDeepCopyFailure(ExampleSix.ONE, ExampleSix.TWO, "root: ONE != TWO");
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

    private void assertDeepCopySuccess(
        Object one,
        Object two,
        Class<?> ... additionalImmutableTypes)
    {
        DeepCopyMatchResult matches = new DeepCopyAssertion(additionalImmutableTypes).matches(one, two);
        assertTrue(matches.failureDescription, matches.isDeepCopy);
    }

}
