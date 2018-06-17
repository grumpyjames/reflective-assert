package net.digihippo.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

final class DeepCopyAssertion
{
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

    private final Stack<String> fieldPath = new Stack<>();

    DeepCopyAssertion(Class<?>... additionalImmutableTypes)
    {
        fieldPath.push("root");
        Collections.addAll(valueTypes, additionalImmutableTypes);
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
                return valueNotEqual(one, two);
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

            if (one.getClass().isEnum())
            {
                if (one == two)
                {
                    return DeepCopyMatchResult.success();
                }
                else
                {
                    return valueNotEqual(one, two);
                }
            }

            if (one == two)
            {
                return fail("The same instance cannot be a deep copy of itself");
            }

            if (one.getClass().isArray())
            {
                return arrayMatch(one, two);
            }

            if (one instanceof Map)
            {
                return mapTypeMatch(one, two);
            }

            if (one instanceof Collection)
            {
                return collectionTypeMatch(one, two);
            }

            return fieldByFieldMatch(one, two);
        }
        catch (IllegalAccessException e)
        {
            return DeepCopyMatchResult.failure(e.getMessage());
        }
    }

    private DeepCopyMatchResult fieldByFieldMatch(Object one, Object two) throws IllegalAccessException
    {
        final Field[] fields = one.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers()))
            {
                continue;
            }

            field.setAccessible(true);

            fieldPath.push(field.getName());

            DeepCopyMatchResult result = matches(field.get(one), field.get(two));
            if (!result.isDeepCopy)
            {
                return result;
            }

            fieldPath.pop();
        }

        return DeepCopyMatchResult.success();
    }

    private DeepCopyMatchResult arrayMatch(Object one, Object two)
    {
        Class<?> componentType = one.getClass().getComponentType();
        if (componentType.isPrimitive())
        {
            if (componentType == long.class)
            {
                return longArrayMatch((long[]) one, (long[]) two);
            }
            else if (componentType == int.class)
            {
                return intArrayMatch((int[]) one, (int[]) two);
            }
            else if (componentType == double.class)
            {
                return doubleArrayMatch((double[]) one, (double[]) two);
            }
            else if (componentType == float.class)
            {
                return floatArrayMatch((float[]) one, (float[]) two);
            }
            else if (componentType == boolean.class)
            {
                return booleanArrayMatch((boolean[]) one, (boolean[]) two);
            }
            else if (componentType == byte.class)
            {
                return byteArrayMatch((byte[]) one, (byte[]) two);
            }
            throw new UnsupportedOperationException("I have no idea what " + componentType + " is.");
        }
        else
        {
            return objectArrayMatch((Object[]) one, (Object[]) two);
        }
    }

    private DeepCopyMatchResult objectArrayMatch(Object[] one, Object[] two)
    {
        for (int i = 0; i < one.length; i++)
        {
            fieldPath.push("[" + i + "]");

            if (i >= two.length)
            {
                return valueNotEqual(one[i], "<absent>");
            }
            final DeepCopyMatchResult result = matches(one[i], two[i]);
            if (!result.isDeepCopy)
            {
                return result;
            }

            fieldPath.pop();
        }
        for (int i = 0; i < two.length; i++)
        {
            fieldPath.push("[" + i + "]");

            if (i >= one.length)
            {
                return valueNotEqual("<absent>", two[i]);
            }
            final DeepCopyMatchResult result = matches(one[i], two[i]);
            if (!result.isDeepCopy)
            {
                return result;
            }

            fieldPath.pop();
        }
        return DeepCopyMatchResult.success();
    }

    private DeepCopyMatchResult longArrayMatch(long[] one, long[] two)
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

    private DeepCopyMatchResult intArrayMatch(int[] one, int[] two)
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

    private DeepCopyMatchResult byteArrayMatch(byte[] one, byte[] two)
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

    private DeepCopyMatchResult floatArrayMatch(float[] one, float[] two)
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

    private DeepCopyMatchResult doubleArrayMatch(double[] one, double[] two)
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

    private DeepCopyMatchResult booleanArrayMatch(boolean[] one, boolean[] two)
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

    private DeepCopyMatchResult collectionTypeMatch(Object one, Object two)
    {
        final Collection listOne = (Collection) one;
        final Collection listTwo = (Collection) two;

        int index = 0;
        Iterator primaryIterator = listOne.iterator();
        Iterator secondaryIterator = listTwo.iterator();
        while (primaryIterator.hasNext())
        {
            final Object fromListOne = primaryIterator.next();
            fieldPath.push("at(" + index + ")");

            if (!secondaryIterator.hasNext())
            {
                return valueNotEqual(fromListOne, "<absent>");
            }

            final DeepCopyMatchResult match = matches(fromListOne, secondaryIterator.next());

            if (!match.isDeepCopy)
            {
                return match;
            }

            fieldPath.pop();
            ++index;
        }

        primaryIterator = listTwo.iterator();
        secondaryIterator = listOne.iterator();
        index = 0;
        while (primaryIterator.hasNext())
        {
            final Object fromListTwo = primaryIterator.next();
            fieldPath.push("at(" + index + ")");

            if (!secondaryIterator.hasNext())
            {
                return valueNotEqual("<absent>", fromListTwo);
            }

            final DeepCopyMatchResult match = matches(secondaryIterator.next(), fromListTwo);

            if (!match.isDeepCopy)
            {
                return match;
            }

            fieldPath.pop();
            ++index;
        }

        return DeepCopyMatchResult.success();
    }

    private DeepCopyMatchResult mapTypeMatch(Object one, Object two)
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
            return valueNotEqual(one, two);
        }
        else
        {
            return DeepCopyMatchResult.success();
        }
    }

    private boolean isValueType(Object one)
    {
        return valueTypes.contains(one.getClass());
    }

    private DeepCopyMatchResult valueNotEqual(Object first, Object second)
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
